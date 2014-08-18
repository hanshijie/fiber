package fiber.db;

import java.io.File;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

import fiber.io.Log;

public class BDBStorage {

	final EnvironmentConfig envConf;
	final DatabaseConfig dbConf;
	final TransactionConfig txnConf;
	
	final Environment env;
	
	final String root;
	Map<Integer, Database> databases;
	Map<Integer, ReentrantLock> locks;
	
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
		if(conf.getEnvDurability() != null) {
			this.envConf.setDurability(conf.getEnvDurability());
		}
		
		this.dbConf = new DatabaseConfig();
		this.dbConf.setAllowCreate(true);
		this.dbConf.setTransactional(true);
		
		this.txnConf = new TransactionConfig();
		if(conf.getTxnDurability() != null) {
			this.txnConf.setDurability(conf.getTxnDurability());
		}
		
		this.env = new Environment(new File(root), envConf);
		
		this.locks = new ConcurrentHashMap<Integer, ReentrantLock>();
		this.databases = new ConcurrentHashMap<Integer, Database>();
		for(Map.Entry<Integer, String> e : conf.getDatabases().entrySet()) {
			addDB(e.getKey(), e.getValue());
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
	 * 重新加载数据库配置.实际上只能做添加新表的操作.
	 */
	public void reloadConfig(BDBConfig conf) {
		for(Map.Entry<Integer, String> e : conf.getDatabases().entrySet()) {
			if(!this.databases.containsKey(e.getKey())) {
				addDB(e.getKey(), e.getValue());
			}
		}
	}
	
	public void addDB(int dbid, String dbname) {
		synchronized(this.env) { 
			Database db = this.env.openDatabase(null, dbname, this.dbConf);
			Log.trace("database open. id:%d name:%s", dbid, dbname);
			this.databases.put(dbid, db);
			this.locks.put(dbid, new ReentrantLock());	

		}
	}
	
	public Database getDB(int tableid) {
		return this.databases.get(tableid);
	}
	
	public Transaction getTxn() {
		return this.env.beginTransaction(null, this.txnConf);
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
