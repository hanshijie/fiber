package fiber.app.server;

import fiber.db.Storage;
import fiber.io.Log;

public final class G {
	
	/***********************************************
	 *  		modules define
	 ***********************************************/
	public static Login login = new Login();
	
	
	
	
	
	static {
		Runtime.getRuntime().addShutdownHook(
			new Thread("JVMShutDown") {
				@Override
				public void run() {
					try {
						shutdown();
					} catch (Exception e) {
						Log.fatal("JVMShutDown error. exception:%s", e);
					}
				}
			});
	}
	
	private static void shutdown() {
		Log.notice("========  JVM shutdown begin =======");
		Storage storage = Storage.getInstance();
		if(storage != null) {
			DB.flush();
			try {
				storage.checkpoint();
			} catch (Exception e) {
				Log.alert("storage.checkpoint fail. exception:%s", e);
			}
			storage.close();
		}
		Log.notice("========  JVM shutdown end   =======");
	}

}
