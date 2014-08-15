package fiber.db;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

import fiber.io.Log;

public class BDBStorage {
	final String root;
	Map<Integer, Database> databases;
	Map<Integer, ReentrantLock> locks;
	final EnvironmentConfig envConf;
	final Environment env;
	public BDBStorage(BDBConfig conf) {
		// 如果数据库根目录不存在,则创建之
		this.root = conf.getEnvRoot();
		File fd = new File(this.root);
		if (!fd.exists()) {
			fd.mkdirs();
		}
	        
		this.envConf = new EnvironmentConfig();
		this.envConf.setAllowCreate(true);
		this.envConf.setTransactional(true);
		if(conf.getCacheSize() != 0) {
			this.envConf.setCacheSize(conf.getCacheSize());
		}
		if(conf.getDurability() != null) {
			this.envConf.setDurability(conf.getDurability());
		}
		this.env = new Environment(new File(root), envConf);
		
		this.locks = new HashMap<Integer, ReentrantLock>();
		this.databases = new HashMap<Integer, Database>();
		for(Map.Entry<Integer, String> e : conf.getDatabases().entrySet()) {
			addDB(e.getKey(), e.getValue(), false);
		}
		
		Runtime.getRuntime().addShutdownHook(
			new Thread("JVMShutDown.Storage") {
				@Override
				public void run() {
					try {
						BDBStorage.shutdown();
					} catch (Exception e) {
						Log.fatal("JVMShutDown.Storage: close error. exception:%s", e);
					}
				}
			});
	}
	
	/**
	 * 
	 * @param dbid
	 * @param dbname
	 * @param immutable databases与locks是hashmap类型,不支持并发读写.
	 *  	如果数据库初始化完毕后再加新表需要指定此标记为true, 
	 *  	通过复制添加的方式替换databases. 
	 */
	public void addDB(int dbid, String dbname, boolean immutable) {
		synchronized(this.env) { 
			DatabaseConfig dbConf = new DatabaseConfig();
			dbConf.setAllowCreate(true);
			dbConf.setTransactional(true);
			Database db = this.env.openDatabase(null, dbname, dbConf);
			Log.trace("database open. id:%d name:%s", dbid, dbname);
			if(!immutable) {
				this.databases.put(dbid, db);
				this.locks.put(dbid, new ReentrantLock());	
			} else {
				HashMap<Integer, Database> newdbs = new HashMap<Integer, Database>();
				newdbs.putAll(this.databases);
				newdbs.put(dbid, db);
				
				HashMap<Integer, ReentrantLock> newlocks = new HashMap<Integer, ReentrantLock>();
				newlocks.putAll(this.locks);
				newlocks.put(dbid, new ReentrantLock());
				
				this.locks = newlocks;
				this.databases = newdbs;
			}
		}
	}
	
	public Database getDB(int tableid) {
		return this.databases.get(tableid);
	}
	
	public Transaction getTxn() {
		TransactionConfig txnConfig = new TransactionConfig();
		txnConfig.setDurability(Durability.COMMIT_NO_SYNC);
		return this.env.beginTransaction(null, txnConfig);
	}
	
	public static class DBException extends Exception {
		public DBException(String msg) {
			super(msg);
		}
		private static final long serialVersionUID = 4069726660134621379L;
		
	}
	
	public void lockDB(int tableid) throws DBException {
		ReentrantLock lock = this.locks.get(tableid);
		if(lock == null) {
			throw new DBException("unknown db id:" + tableid);
		}
		lock.lock();
	}
	
	public void unlockDB(int tableid) {
		this.locks.get(tableid).unlock();
	}
	
	public void lockDBs(TreeSet<Integer> tolocks) throws DBException {
		for(Integer dbid : tolocks) {
			ReentrantLock lock = this.locks.get(dbid);
			if(lock != null) {
				lock.lock();
			} else {
				// 释放已获得的锁
				for(int lockedId : tolocks) {
					if(lockedId == dbid) break;
					this.locks.get(lockedId).unlock();
				}
				throw new DBException("unknown db id:" + dbid);
			}
		}
	}
	
	public void unlockDBs(TreeSet<Integer> tounlocks) {
		for(Integer dbid : tounlocks) {
			ReentrantLock lock = this.locks.get(dbid);
			lock.unlock();
		}
	}
	
	private static BDBStorage instance;
	
	public static void init(BDBConfig conf) {
		instance = new BDBStorage(conf);
	}
	
	public static void shutdown() {
		if(instance != null) {
			instance.close();
		}
	}
	
	public void close() {
		synchronized(this.env) {
			Log.trace("BDBStorage. shutdown. begin.");
			for(Map.Entry<Integer, Database> e : this.databases.entrySet()) {
				Database db = e.getValue();
				int tableid = e.getKey();
				ReentrantLock lock = this.locks.get(tableid);
				lock.lock();
				try {
					Log.trace("db:%d close begin.", tableid);
					db.close();
					Log.trace("db:%d close finish.", tableid);
				} finally {
					lock.unlock();
				}
			}
			this.env.close();
			Log.trace("BDBStorage. shutdown finish.");
		}
	}
	
	public static BDBStorage getInstance() {
		return instance;
	}

}
