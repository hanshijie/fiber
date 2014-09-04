package fiber.app.server;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import fiber.db.Storage;
import static fiber.io.Log.log;

public final class G {
	
	/***********************************************
	 *  		modules define
	 ***********************************************/
	public static Login login = new Login();
	
	
	
	
	
	private static final Marker SHUT_DOWN = MarkerFactory.getMarker("SHUTDOWN");
	static {
		Runtime.getRuntime().addShutdownHook(
			new Thread("JVMShutDown") {
				@Override
				public void run() {
					try {
						shutdown();
					} catch (Exception e) {
						log.error(SHUT_DOWN, "JVMShutDown error", e);
					}
				}
			});
	}
	
	private static void shutdown() {
		log.info(SHUT_DOWN, "========  JVM shutdown begin =======");
		Storage storage = Storage.getInstance();
		if(storage != null) {
			DB.flush();
			try {
				storage.checkpoint();
			} catch (Exception e) {
				log.error(SHUT_DOWN, "storage.checkpoint fail.", e);
			}
			storage.close();
		}
		log.info(SHUT_DOWN, "========  JVM shutdown end   =======");
	}

}
