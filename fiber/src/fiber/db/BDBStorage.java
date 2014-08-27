package fiber.db;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.je.util.DbBackup;

import fiber.io.Log;
import fiber.io.Timer;

public class BDBStorage {
	boolean closed;
	
	final EnvironmentConfig envConf;
	final DatabaseConfig dbConf;
	final TransactionConfig txnConf;
	
	final Environment env;
	
	final String root;
	final String backupRoot;
	final int incrementalBackupInterval;
	final int fullBackupInterval;
	
	public static final class Table {
		private final Database database;
		private final Lock rlock;
		private final Lock wlock;
		public Table(Database db, Lock r, Lock w) {
			this.database = db;
			this.rlock = r;
			this.wlock = w;
		}
		public final Database getDatabase() {
			return database;
		}
		public final Lock getRlock() {
			return rlock;
		}
		public final Lock getWlock() {
			return wlock;
		}
	}
	
	Map<Integer, Table> databases;
	
	public BDBStorage(BDBConfig conf) {
		this.closed = false;
		// 如果数据库根目录不存在,则创建之
		this.root = conf.getEnvRoot();
		File fd = new File(this.root);
		if (!fd.exists()) {
			fd.mkdirs();
		}
		this.backupRoot = conf.getBackupRoot().isEmpty() ? this.root + "/backup" : conf.getBackupRoot();
		fd = new File(this.backupRoot);
		if(!fd.exists()) {
			fd.mkdirs();
		}
		this.incrementalBackupInterval = conf.getIncrementalBackupInterval();
		this.fullBackupInterval = conf.getFullBackupInterval();
	        
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
		
		this.databases = new HashMap<Integer, Table>();
		for(Map.Entry<Integer, String> e : conf.getDatabases().entrySet()) {
			addTable(e.getKey(), e.getValue());
		}
		
		new Thread("BDBStorage.backup") {
			@Override
			public void run() {
				final int MIN_INCREMENTAL_BACKUP_INTERVAL = 6;
				final int MIN_FULL_BACKUP_INTERVAL = 10;
				
				boolean enableIncBackup = incrementalBackupInterval >= MIN_INCREMENTAL_BACKUP_INTERVAL;
				boolean enableFullBackup = fullBackupInterval >= MIN_FULL_BACKUP_INTERVAL;
				Log.notice("BDBStorage.backup init.");
				Log.notice("BDBStorage.backup incrementBackupInterval:%d enable:%s", incrementalBackupInterval, enableIncBackup);
				Log.notice("BDBStorage.backup fullBackupInterval:%d enable:%s", fullBackupInterval, enableFullBackup);
				if(!enableIncBackup && !enableFullBackup) return;
				
				final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				int now = Timer.currentTime();
				int nextIncBackupTime = enableIncBackup ? now + incrementalBackupInterval : Integer.MAX_VALUE;
				int nextFullBackupTime = enableFullBackup ? now + fullBackupInterval : Integer.MAX_VALUE;
				
				String backupConfFile = backupRoot + "/" + "incbackup.conf";
				long lastFileCopiedInPrevBackup = loadFromConfFile(backupConfFile);
				while(true) {
					try {
						int nextBackupTime = nextIncBackupTime < nextFullBackupTime ? nextIncBackupTime : nextFullBackupTime;
						now = Timer.currentTime();
						Thread.sleep((nextBackupTime - now) * 1000);
						Log.trace("BDBStorage.backup  active.");
						if(nextIncBackupTime <= now) {
							nextIncBackupTime = now + incrementalBackupInterval;
							String incrementalBackupDir = String.format("%s/inc-%s", backupRoot ,timeFormat.format(new Date()));
							Log.notice("BDBStorage.backup  incremental start. backup directory:%s lastFileId:%d", incrementalBackupDir, lastFileCopiedInPrevBackup);
							lastFileCopiedInPrevBackup = backup(incrementalBackupDir, lastFileCopiedInPrevBackup);
							Log.notice("BDBStorage.backup  incremental end.");
							saveToConfFile(backupConfFile, lastFileCopiedInPrevBackup);
						}
						if(nextFullBackupTime <= now) {
							nextFullBackupTime = now + fullBackupInterval;
							String fullBackupDir = String.format("%s/full-%s", backupRoot, timeFormat.format(new Date()));
							Log.notice("BDBStorage.backup full start. backup directory:%s", fullBackupDir);
							backup(fullBackupDir, 0);
							Log.notice("BDBStorage.backup full end");
						}
					} catch(Exception e) {
						Log.alert("BDBStorage.backup  exception:%s", e);
						e.printStackTrace();
					}
				}
			}


			final Charset ENCODING = StandardCharsets.UTF_8;
			private long loadFromConfFile(String backupConfFile) {
				try {
					List<String> lines = Files.readAllLines(Paths.get(backupConfFile), ENCODING);
					return lines.isEmpty() ? 0 : Long.parseLong(lines.get(0));
				} catch (Exception e) {
					return 0;
				}
			}
			
			private void saveToConfFile(String backupConfFile, long lastFileCopiedInPrevBackup) {
				try {
					List<String> lines = new ArrayList<String>();
					lines.add(Long.toString(lastFileCopiedInPrevBackup));
					Files.write(Paths.get(backupConfFile), lines, ENCODING);
				} catch(Exception e) {
					Log.alert("BDBStorage.backup savetoConfFile fail.backupConfFile:%s lastFileCopiedInPrevBackup:%d  exception:%s",
						backupConfFile, lastFileCopiedInPrevBackup, e);
				}
			}
		}.start();
		
		Runtime.getRuntime().addShutdownHook(
			new Thread("JVMShutDown.Storage") {
				@Override
				public void run() {
					try {
						shutdown();
					} catch (Exception e) {
						Log.fatal("JVMShutDown.Storage: close error. exception:%s", e);
					}
				}
			});
	}
	
/*
	public void reloadConfig(BDBConfig conf) {
		for(Map.Entry<Integer, String> e : conf.getDatabases().entrySet()) {
			if(!this.databases.containsKey(e.getKey())) {
				addTable(e.getKey(), e.getValue());
			}
		}
	}
*/
	
	public void addTable(int dbid, String dbname) {
		Database db = this.env.openDatabase(null, dbname, this.dbConf);
		Log.trace("database open. id:%d name:%s", dbid, dbname);
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		this.databases.put(dbid, new Table(db, lock.readLock(), lock.writeLock()));	
	}
	
	public Table getTable(int tableid) {
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
	
	public void lockDB(int tableid, boolean readlock) {
		lockDB(getTable(tableid), readlock);
	}
	
	public void lockDB(Table db, boolean readlock) {
		if(readlock) {
			db.rlock.lock();
		} else {
			db.wlock.lock();
		}	
	}
	
	public void unlockDB(int tableid, boolean readlock) {
		unlockDB(getTable(tableid), readlock);
	}
	
	public void unlockDB(Table db, boolean readlock) {
		if(readlock) {
			db.rlock.unlock();
		} else {
			db.wlock.unlock();
		}
	}
	
	public void lockDBs(TreeMap<Integer, Boolean> tolocks) {
		for(Map.Entry<Integer, Boolean> e : tolocks.entrySet()) {
			lockDB(e.getKey(), e.getValue());
		}
	}
	
	public void unlockDBs(TreeMap<Integer, Boolean> tolocks) {
		for(Map.Entry<Integer, Boolean> e : tolocks.entrySet()) {
			unlockDB(e.getKey(), e.getValue());
		}
	}
	
	private static BDBStorage instance;
	
	public static void init(BDBConfig conf) {
		assert(instance == null);
		instance = new BDBStorage(conf);
	}
	
	public void shutdown() {
		close();
	}
	
	synchronized public void close() {
		if(this.closed) return;
		this.closed = true;
		Log.trace("BDBStorage. shutdown. begin.");
		for (Map.Entry<Integer, Table> e : this.databases.entrySet()) {
			Database db = e.getValue().getDatabase();
			Lock lock = e.getValue().getWlock();
			int tableid = e.getKey();
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
	
	synchronized public long backup(String backupDir, long lastFileCopiedInPrevBackup) throws IOException {
		if(this.closed) return lastFileCopiedInPrevBackup;
		Log.notice("BDBStorage.backup begin. backupDir:%s lastFileCopiedInPrevBackup:%d", backupDir, lastFileCopiedInPrevBackup);
		File backupFile = new File(backupDir);
		if(!backupFile.exists()) {
			backupFile.mkdirs();
			Log.notice("backupDir:%s no exist. create it.", backupDir);
		}
	    DbBackup backupHelper = new DbBackup(this.env, lastFileCopiedInPrevBackup);

	    backupHelper.startBackup();
	    try {
	        String[] filesForBackup = backupHelper.getLogFilesInBackupSet();

	        for(String file : filesForBackup) {
	        	String src = this.root + "/" + file;
	        	String dst = backupDir + "/" + file;
	        	Files.copy(new File(src).toPath(), new File(dst).toPath(), REPLACE_EXISTING);
	        }

	        lastFileCopiedInPrevBackup = backupHelper.getLastFileInBackupSet();
	        Log.notice("BDBStorage.backup end. backupDir:%s lastFileCopiedInPrevBackup:%d", backupDir, lastFileCopiedInPrevBackup);
	        return lastFileCopiedInPrevBackup;
	    } finally {
	       backupHelper.endBackup();
	   }	
		
	}
	
	public static BDBStorage getInstance() {
		return instance;
	}

}
