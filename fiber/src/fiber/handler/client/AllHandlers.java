package fiber.handler.client;

import java.util.Map;
import java.util.HashMap;
import fiber.io.BeanHandler;

public final class AllHandlers {
	private final static Map<Integer, BeanHandler<?>> allhandlers = new HashMap<Integer, BeanHandler<?>>();
	static {
		allhandlers.put(2, new TestBeanHandler());
		allhandlers.put(3, new TestTypeHandler());
		allhandlers.put(4, new HelloHandler());
	}

	public static Map<Integer, BeanHandler<?>> get() {
		return allhandlers;
	}
}
