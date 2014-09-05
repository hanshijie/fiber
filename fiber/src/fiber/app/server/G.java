package fiber.app.server;

import fiber.db.Storage;
import static fiber.io.Log.log;

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
						log.error("JVMShutDown ", e);
					}
				}
			});
	}
	
	private static void shutdown() {
		log.info("========  JVMShutDown begin =======");
		Storage storage = Storage.getInstance();
		if(storage != null) {
			DB.flush();
			try {
				storage.checkpoint();
			} catch (Exception e) {
				log.error("storage.checkpoint fail.", e);
			}
			storage.close();
		}
		log.info("========  JVMShutDown end   =======");
	}

}
