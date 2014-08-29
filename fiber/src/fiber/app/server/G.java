package fiber.app.server;

import fiber.db.Storage;
import fiber.io.Log;

public final class G {
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
	
	public static Storage storage = null;
	
	private static void shutdown() {
		Log.notice("========  JVM shutdown begin =======");
		if(storage != null) {
			AllTable.flush();
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
