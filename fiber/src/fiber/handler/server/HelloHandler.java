package fiber.handler.server;

import fiber.io.*;
import fiber.bean.HelloArg;
import fiber.bean.HelloRes;

public class HelloHandler extends RpcHandler<HelloArg, HelloRes> {
	@Override
	public boolean onServer(IOSession session, HelloArg arg, HelloRes res) {
		return true;
	}

	@Override
	public void onClient(IOSession session, HelloArg arg, HelloRes res) {
	}

	@Override
	public void onTimeout(IOSession session, HelloArg arg) {
	}
}
