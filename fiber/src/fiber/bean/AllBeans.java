package fiber.bean;

import java.util.HashMap;

import fiber.io.Bean;

public class AllBeans {
	private final static HashMap<Short, Bean<?>> stub = new HashMap<Short, Bean<?>>();
	static {
		stub.put(BFloat.TYPE, new BFloat());
		stub.put(BString.TYPE, new BString());
		stub.put(BShort.TYPE, new BShort());
		stub.put(BInteger.TYPE, new BInteger());
		stub.put(BOctets.TYPE, new BOctets());
		stub.put(BLong.TYPE, new BLong());
		stub.put(BDouble.TYPE, new BDouble());
		stub.put(BByte.TYPE, new BByte());
		
	}
	
	public static HashMap<Short, Bean<?>> get() {
		return stub;
	}

}