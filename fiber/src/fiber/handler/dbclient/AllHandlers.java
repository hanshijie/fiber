package fiber.handler.dbclient;

import java.util.Map;
import java.util.HashMap;
import fiber.io.BeanHandler;

public final class AllHandlers {
	private final static Map<Integer, BeanHandler<?>> allhandlers = new HashMap<Integer, BeanHandler<?>>();
	static {

	}

	public static Map<Integer, BeanHandler<?>> get() {
		return allhandlers;
	}
}
