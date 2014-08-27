package fiber.test;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Transaction;

import fiber.app.server.AllTable;
import fiber.app.server.TablePer;
import fiber.db.BDBConfig;
import fiber.db.BDBStorage;
import fiber.io.Log;
import fiber.io.OctetsStream;
import fiber.mapdb.TValue;
import fiber.mapdb.Table;
import fiber.mapdb.Table.Walk;

public class BDBTest {

	public static void main(String[] args) throws Exception {
		BDBConfig conf = new BDBConfig();
		conf.setEnvRoot("e:/bdb_root");
		conf.setBackupRoot("e:/bdb_backup_root");
		conf.setIncrementalBackupInterval(10);
		conf.setFullBackupInterval(60);
		conf.setCacheSize(10 * 1024 * 1024);
		
		conf.AddDatabse(1, "user");
		conf.AddDatabse(2, "role");
		
		BDBStorage.init(conf);
		
		BDBStorage storage = BDBStorage.getInstance();
		Database user = storage.getTable(1).getDatabase();
		Database role = storage.getTable(2).getDatabase();
		
		Table tUser = new TablePer(1, true, 1000, AllTable.IntMarshaller, AllTable.IntMarshaller);
		for(long i = 0 ; i < 1000 ; i++) {
			Transaction txn = storage.getTxn();
			DatabaseEntry key = new DatabaseEntry();
			key.setData(OctetsStream.create(10).marshal(i).toOctets().array());
			DatabaseEntry value = new DatabaseEntry();
			value.setData(OctetsStream.create(10).marshal(i * i).toOctets().array());
			user.put(txn, key, value);
			
			role.put(txn, key, value);
			txn.commit();
		}

		tUser.walk(new Walk() {
			int i = 0;
			@Override
			public boolean onProcess(Table table, Object key, TValue value) {
				Log.trace("[%d] walk. key:%s value:%s", ++i, key, value);
				return true;
			}
			
		});
	}

}
