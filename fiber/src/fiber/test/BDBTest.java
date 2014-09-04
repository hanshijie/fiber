package fiber.test;

import fiber.app.server.DB;
import fiber.db.BDBConfig;
import fiber.db.BDBStorage;
import fiber.db.Storage;
import fiber.db.TValue;
import fiber.db.Table;
import fiber.db.TablePer;
import fiber.db.Table.Walk;
import static fiber.io.Log.log;

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
		
		Storage.setInstance(BDBStorage.create(conf));

		
		Table tUser = new TablePer(1, 1000, DB.IntMarshaller, DB.IntMarshaller);
	
		tUser.walk(new Walk() {
			int i = 0;
			@Override
			public boolean onProcess(Table table, Object key, TValue value) {
				log.trace("[{}] walk. key:{} value:{}", ++i, key, value);
				return true;
			}
			
		});
	}

}
