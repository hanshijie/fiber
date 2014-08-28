package fiber.test;

import fiber.app.server.AllTable;
import fiber.app.server.G;
import fiber.app.server.TablePer;
import fiber.db.BDBConfig;
import fiber.db.BDBStorage;
import fiber.io.Log;
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
		
		G.storage = BDBStorage.create(conf);

		
		Table tUser = new TablePer(1, 1000, AllTable.IntMarshaller, AllTable.IntMarshaller);
	
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
