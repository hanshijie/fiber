package fiber.test;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Transaction;

import fiber.db.BDBConfig;
import fiber.db.BDBStorage;

public class BDBTest {

	public static void main(String[] args) throws Exception {
		BDBConfig conf = new BDBConfig();
		conf.setEnvRoot("bdb_root");
		conf.setBackupRoot("bdb_backup_root");
		conf.setIncrementalBackupInterval(10);
		conf.setFullBackupInterval(60);
		conf.setCacheSize(10 * 1024 * 1024);
		
		conf.AddDatabse(1, "user");
		conf.AddDatabse(2, "role");
		
		BDBStorage.init(conf);
		
		BDBStorage storage = BDBStorage.getInstance();
		Database user = storage.getTable(1).getDatabase();
		Database role = storage.getTable(2).getDatabase();
		for(long i = 0 ;  ; i++) {
			Thread.sleep(10);
			Transaction txn = storage.getTxn();
			DatabaseEntry key = new DatabaseEntry();
			key.setData(Long.toString(i).getBytes());
			DatabaseEntry value = new DatabaseEntry();
			value.setData(Long.toString(i * i).getBytes());
			user.put(txn, key, value);
			
			role.put(txn, key, value);
			txn.commit();
		}
	}

}
