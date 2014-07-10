package fiber.app.client;

import java.util.Map;
import java.util.ArrayList;

import fiber.handler.client.AllHandlers;
import fiber.io.BeanHandler;
import fiber.io.ClientManager;
import fiber.io.IOConfig;
import fiber.io.IOPoller;
import fiber.io.Log;
import fiber.io.SystemConfig;

public class Client {

	public static void main(String[] args) {
		
		try {
			if(args.length != 4) {
				System.out.print("usage. Client [ip] [port] [connectNum] [taskNum]");
				return;
			}
			SystemConfig config = SystemConfig.getInstance();
			config.setLogLevel(Log.LOG_NOTICE);
			String addr = args[0];
			short port = Short.parseShort(args[1]);
			final int CONNECT_NUM = Integer.parseInt(args[2]);
			final int TASK_NUM = Integer.parseInt(args[3]);
			
			final IOPoller poller = new IOPoller(1);
			final Map<Short, BeanHandler<?>> handlerStub = AllHandlers.get();

			ArrayList<ClientManager> clients = new ArrayList<ClientManager>();
			for(int i = 0 ; i < CONNECT_NUM ; i++) {
				ClientManager client = new ClientManager(poller, handlerStub);
				IOConfig sconfig = client.getConfig();
				sconfig.setAddr(addr, port);
				client.startClient();
				clients.add(client);
				Thread.sleep(20);
			}
			
			System.out.println("start task");
			//Random random = new Random(1218);
			for(int i = 0 ; i < CONNECT_NUM ; i++) {
				for(int j = 0 ; j < TASK_NUM ; j++) {
					/*
					ClientManager client = clients.get(i);
					RpcHello rpc = new RpcHello();
					rpc.setArg(new HelloArg(random.nextInt(10000)));
					client.sendRpc(rpc, null);
					*/
				}
			}
			
			Log.notice("end ..");
			poller.runBackground();
		} catch (Exception e2) {
			e2.printStackTrace();
		}


	}

}
