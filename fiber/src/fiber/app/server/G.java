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

	static {
		Runtime.getRuntime().addShutdownHook(
			new Thread("JVMShutDown") {
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
	
	private static void shutdown() {
		Log.notice("========  JVM shutdown begin =======");
		AllTable.flush();
		if(storage != null) {
			storage.close();
		}
		Log.notice("========  JVM shutdown end   =======");
	}
	
	
}
