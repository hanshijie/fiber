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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.je.util.DbBackup;

import fiber.io.Octets;
import fiber.io.Timer;

public final class BDBStorage extends Storage {
	private final Logger log = LoggerFactory.getLogger(BDBStorage.class);
	public static BDBStorage create(BDBConfig conf) {
		return new BDBStorage(conf);
	}
	
	boolean closed;
	
	final EnvironmentConfig envConf;
	final DatabaseConfig dbConf;
	final TransactionConfig txnConf;
	
	final Environment env;
	
	final String root;
	final String backupRoot;
	final int incrementalBackupInterval;
	final int fullBackupInterval;
	
	public static final class DTable {
		private final Database database;
		private final Lock rlock;
		private final Lock wlock;
		public DTable(Database db, Lock r, Lock w) {
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
	
	Map<Integer, DTable> databases;
	
	private final static Marker BACKUP = MarkerFactory.getMarker("BACKUP"); 
	BDBStorage(BDBConfig conf) {
		this.closed = false;
		// if environment root directory not exists, we create it.
		this.root = conf.getEnvRoot();
		File fd = new File(this.root);
		if (!fd.exists()) {
			fd.mkdirs();
		}
		this.backupRoot = conf.getBackupRoot().isEmpty() ? this.root + "/backup" : conf.getBackupRoot();
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
		
		this.databases = new HashMap<Integer, DTable>();
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
				log.info(BACKUP, "init.");
				log.info(BACKUP, "incrementBackupInterval:{} enable:{}", incrementalBackupInterval, enableIncBackup);
				log.info(BACKUP, "fullBackupInterval:{} enable:{}", fullBackupInterval, enableFullBackup);
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
						log.info(BACKUP, "backup active.");
						env.flushLog(true);
						if(nextIncBackupTime <= now) {
							nextIncBackupTime = now + incrementalBackupInterval;
							String incrementalBackupDir = String.format("%s/inc-%s", backupRoot ,timeFormat.format(new Date()));
							log.info(BACKUP, "incremental start. backup directory:{} lastFileId:{}", incrementalBackupDir, lastFileCopiedInPrevBackup);
							lastFileCopiedInPrevBackup = backup(incrementalBackupDir, lastFileCopiedInPrevBackup);
							log.info(BACKUP, "incremental end.");
							saveToConfFile(backupConfFile, lastFileCopiedInPrevBackup);
						}
						if(nextFullBackupTime <= now) {
							nextFullBackupTime = now + fullBackupInterval;
							String fullBackupDir = String.format("%s/full-%s", backupRoot, timeFormat.format(new Date()));
							log.info(BACKUP, "full start. backup directory:{}", fullBackupDir);
							backup(fullBackupDir, 0);
							log.info(BACKUP, "full end");
						}
					} catch(Exception e) {
						log.error(BACKUP, "backup fail.", e);
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
					log.error(BACKUP, "savetoConfFile fail.backupConfFile:{} lastFileCopiedInPrevBackup:{}",
						backupConfFile, lastFileCopiedInPrevBackup);
					log.error(BACKUP, "savetoConfFile", e);
				}
			}
		}.start();
	}
	
	public void addTable(int dbid, String dbname) {
		Database db = this.env.openDatabase(null, dbname, this.dbConf);
		log.info("database open. id:{} name:{}", dbid, dbname);
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		this.databases.put(dbid, new DTable(db, lock.readLock(), lock.writeLock()));	
	}
	
	public DTable getTable(int tableid) {
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
	
	public void lockDB(DTable db, boolean readlock) {
		if(readlock) {
			db.rlock.lock();
		} else {
			db.wlock.lock();
		}	
	}
	
	public void unlockDB(int tableid, boolean readlock) {
		unlockDB(getTable(tableid), readlock);
	}
	
	public void unlockDB(DTable db, boolean readlock) {
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
	
	public Octets getData(Transaction txn, Database db, Octets key, LockMode lm) {
		DatabaseEntry dkey = new DatabaseEntry(key.array());
		DatabaseEntry dvalue = new DatabaseEntry();
		OperationStatus status = db.get(txn, dkey, dvalue, lm);
		if(status == OperationStatus.SUCCESS) {
			return Octets.wrap(dvalue.getData());
		} else {
			return null;
		}
	}
	
	public boolean putData(Transaction txn , Database db, Octets key, Octets value) {
		log.debug("BDBStorage.putData  key:{}", key.dump());
		DatabaseEntry dkey = new DatabaseEntry(key.array());
		DatabaseEntry dvalue = new DatabaseEntry(value.array());
		OperationStatus status = db.put(txn, dkey, dvalue);
		return (status == OperationStatus.SUCCESS);
	}
	
	public boolean delData(Transaction txn , Database db, Octets key) {
		log.debug("BDBStorage. delData. key:{}", key.dump());
		DatabaseEntry dkey = new DatabaseEntry(key.array());
		OperationStatus status = db.delete(txn, dkey);
		return (status == OperationStatus.SUCCESS);
	}
	
	public void walk(int tableid, Octets begin, Walker w) {
		DTable dTable = getTable(tableid);
		Lock lock = dTable.wlock;
		lock.lock();
		try {
			Database db = dTable.getDatabase();
			Cursor cursor = db.openCursor(null, null);
			DatabaseEntry key = new DatabaseEntry(begin.array());
			DatabaseEntry value = new DatabaseEntry();
			OperationStatus status = cursor.getSearchKeyRange(key, value, LockMode.READ_UNCOMMITTED);
			for( ; status == OperationStatus.SUCCESS ; status = cursor.getNext(key, value, LockMode.READ_UNCOMMITTED)) {
				Octets okey = Octets.create(key.getData(), key.getSize());
				Octets ovalue = Octets.create(value.getData(), value.getSize());
				if(!w.onProcess(okey, ovalue)) break;
			}
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public boolean truncateTable(int tableid) {
		DTable dTable = getTable(tableid);
		Lock lock = dTable.wlock;
		lock.lock();
		Transaction txn = this.getTxn();
		Database db = dTable.getDatabase();
		String dbName  = db.getDatabaseName();
		try {
			db.close();
			this.env.truncateDatabase(txn, dbName, false);
			txn.commit();
		} catch (DatabaseException e) {
			log.error("BDBStorage.clearTable fail. tableid:{}", tableid);
			log.error("BDBStorage.clearTable excpetion:", e);
			txn.abort();
			return false;
		} finally {
			lock.unlock();
		}
		addTable(tableid, dbName);
		return true;
	}
	
	private final static Marker STORAGE_CLOSE = MarkerFactory.getMarker("STORAGE CLOSE");
	public void close() {
		synchronized(this) {
			log.info(STORAGE_CLOSE, "begin.");
			if(this.closed) return;
			this.closed = true;
			for (Map.Entry<Integer, DTable> e : this.databases.entrySet()) {
				Database db = e.getValue().getDatabase();
				Lock lock = e.getValue().getWlock();
				int tableid = e.getKey();
				String name = db.getDatabaseName();
				lock.lock(); // never unlock this locks to forbid another operations on closed databases.
				log.info(STORAGE_CLOSE, "table <{}, {}> close begin.", tableid, name);
				db.close();
				log.info(STORAGE_CLOSE, "table <{}, {}> close end.", tableid, name);
			}
			this.env.close();
			log.info(STORAGE_CLOSE, "end.");
		}
	}
	
	synchronized public long backup(String backupDir, long lastFileCopiedInPrevBackup) throws IOException {
		if(this.closed) return lastFileCopiedInPrevBackup;
		log.info("BDBStorage.backup begin. backupDir:{} lastFileCopiedInPrevBackup:{}", backupDir, lastFileCopiedInPrevBackup);
		File backupFile = new File(backupDir);
		if(!backupFile.exists()) {
			backupFile.mkdirs();
			log.info("backupDir:{} no exist. create it.", backupDir);
		}
	    DbBackup backupHelper = new DbBackup(this.env, lastFileCopiedInPrevBackup);

	    backupHelper.startBackup();
	    try {
	        String[] filesForBackup = backupHelper.getLogFilesInBackupSet();
	        for(String file : filesForBackup) {
	        	String src = this.root + "/" + file;
	        	String dst = backupDir + "/" + file;
	        	Files.copy(Paths.get(src), Paths.get(dst), REPLACE_EXISTING);
	        }

	        lastFileCopiedInPrevBackup = backupHelper.getLastFileInBackupSet();
	        log.info("BDBStorage.backup end. backupDir:{} lastFileCopiedInPrevBackup:{}", backupDir, lastFileCopiedInPrevBackup);
	        return lastFileCopiedInPrevBackup;
	    } finally {
	       backupHelper.endBackup();
	   }	
		
	}

	@Override
	public Octets get(int tableid, Octets key) {
		log.debug(GET, "tableid:{} key:{}", tableid, key.dump());
		DTable dTable = getTable(tableid);
		Lock lock = dTable.rlock;
		lock.lock();
		try {
			Database db = dTable.getDatabase();
			return getData(null, db, key, LockMode.READ_UNCOMMITTED);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public boolean put(int tableid, Octets key, Octets value) {
		log.debug(PUT, "tableid:{} key:{} value:{}", tableid, key.dump(), value);
		DTable dTable = getTable(tableid);
		Lock lock = dTable.wlock;
		lock.lock();
		try {
			Database db = dTable.getDatabase();
			return putData(null, db, key, value);
		} finally {
			lock.unlock();
		}		
	}
	

	@Override
	public boolean del(int tableid, Octets key) {
		log.debug(DEL, "tableid:{} key:{}", tableid, key.dump());
		DTable dTable = getTable(tableid);
		Lock lock = dTable.wlock;
		lock.lock();
		try {
			Database db = dTable.getDatabase();
			return delData(null, db, key);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public Map<Integer, ArrayList<Octets>> get(
			Map<Integer, ArrayList<Octets>> tableDatasMap) {
		return null;
	}
	
	private final static Marker PUT = MarkerFactory.getMarker("STORAGE PUT");
	private final static Marker GET = MarkerFactory.getMarker("STORAGE GET");
	private final static Marker DEL = MarkerFactory.getMarker("STORAGE DEL");
	@Override
	public boolean put(Map<Integer, ArrayList<Pair>> tableDatasMap) {
		TreeMap<Integer, Boolean> locks = new TreeMap<Integer, Boolean>();
		for(Integer tableid : tableDatasMap.keySet()) {
			locks.put(tableid, false); // write lock.
		}
		lockDBs(locks);
		Transaction txn = this.getTxn();
		try {
			for(Map.Entry<Integer, ArrayList<Pair>> e : tableDatasMap.entrySet()) {
				Integer tableid = e.getKey();
				DTable table = this.getTable(tableid);
				Database db = table.getDatabase();
				for(Pair pair : e.getValue()) {
					// we presume value which is empty Octets means we should delete it.
					Octets value = pair.getValue();
					if(!value.empty()) {
						this.putData(txn, db, pair.getKey(), pair.getValue());
					} else {
						this.delData(txn, db, pair.getKey());
					}
				}
			}
			txn.commit();
			return true;
		} catch (Exception e) {
			log.error(PUT, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			log.error(PUT, "fail!", e);
			log.error(PUT, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			txn.abort();
			return false;
		} finally {
			unlockDBs(locks);
		}
	}


	private final static Marker CHECKPOINT = MarkerFactory.getMarker("CHECKPOINT");
	@Override
	public void checkpoint() throws Exception {
		// unnecessary to invoke environment.checkpoint();
		this.env.flushLog(true);
		log.info(CHECKPOINT, "succ.");
	}
	

}
