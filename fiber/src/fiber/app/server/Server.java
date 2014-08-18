package fiber.app.server;

import java.util.Map;

import fiber.handler.server.AllHandlers;
import fiber.io.BeanHandler;
import fiber.io.IOConfig;
import fiber.io.IOPoller;
import fiber.io.Log;
import fiber.io.ServerManager;

public class Server {

	public static void main(String[] args) {
		
		try {
			if(args.length != 1) {
				Log.trace("Usage:  Server [loglevel] ");
				return;
			}
			final int logLevel = Integer.parseInt(args[0]);
			System.setProperty("log_level", Integer.valueOf(logLevel).toString());

			//final String LUA_FILE = args[2];
			IOPoller poller = new IOPoller(1);

			final Map<Integer, BeanHandler<?>> handlerStub = AllHandlers.get();
			//for(int i = 0 ; i < 8 ; i++)
			{	
				String addr = "0.0.0.0";
				short port = 1314;
				ServerManager server = new ServerManager(poller, handlerStub);
				IOConfig conf = server.getConfig();
				conf.setAddr(addr, port);
				server.startServer();
			}
			
			//LuaState.setBaseDir(new File(LUA_FILE).getParent());
			//LuaState.setInitLuaFile(LUA_FILE);
			Log.notice("init succ...");
			poller.runBackground();
			
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		

	}

}
