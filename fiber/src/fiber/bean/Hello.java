package fiber.bean;

import fiber.io.*;

public final class Hello extends RpcBean<HelloArg, HelloRes> {
	public  static final Hello STUB = new Hello();
	public Hello() {}
	public Hello(HelloArg a) { arg = a; }
	@Override public final Hello stub() { return STUB; }
	@Override public final int maxsize() { return 100; }
	@Override public final int type() { return 4; }
	@Override public final int getTimeout() { return 30; }
	@Override public final Hello create() { return new Hello(); }
	@Override public final HelloArg createArg() { return new HelloArg(); }
	@Override public final HelloRes createRes() { return new HelloRes(); }
}
