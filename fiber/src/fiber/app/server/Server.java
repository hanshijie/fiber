package fiber.app.server;

import java.util.Map;

import fiber.bean.SessionInfo;
import fiber.common.LockPool;
import fiber.common.TaskPool;
import fiber.handler.server.AllHandlers;
import fiber.io.BeanHandler;
import fiber.io.IOConfig;
import fiber.io.IOPoller;
import fiber.io.IOSession;
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
			TaskPool.init(2, 2, 2);
			LockPool.init(32);

			//final String LUA_FILE = args[2];
			IOPoller poller = new IOPoller(1);

			final Map<Integer, BeanHandler<?>> handlerStub = AllHandlers.get();
			//for(int i = 0 ; i < 8 ; i++)
			{	
				String addr = "0.0.0.0";
				short port = 1314;
				ServerManager server = new ServerManager(poller, handlerStub) {
					protected void onAddSession(IOSession s) {
						for(int i = 0 ; i < 10 ; i++) {
							SessionInfo si = new SessionInfo();
							si.setuid(12);
							si.setlogintime(18);
							si.getroleids().add(100);
							si.getroleids().add(200);
							s.send(si);
						}
					}
					
					protected void onDelSession(IOSession s) {
						
					}
				};
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
