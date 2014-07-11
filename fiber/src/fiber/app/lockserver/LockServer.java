package fiber.app.lockserver;

import fiber.handler.client.AllHandlers;
import fiber.io.IOConfig;
import fiber.io.IOPoller;
import fiber.io.ServerManager;

public final class LockServer {

	public static void main(String[] args) {
		try {
		//long runtimeId = MTimer.currentTimeMillis();
		String addr = "0.0.0.0";
		short port = 2222;
		
		IOPoller poller = new IOPoller(1);
		ServerManager server = new GSServerManager(poller, AllHandlers.get());
		IOConfig config = server.getConfig();
		config.setAddr(addr, port);
		server.startServer();
		
		while(true) {
			poller.poll(1000);
			LockManager.processAcquires();
		}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
