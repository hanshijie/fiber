package fiber.handler.client;

import java.util.HashMap;
import java.util.Map;

import fiber.io.BeanHandler;

public class AllHandlers {

	private final static Map<Short, BeanHandler<?>> handlerStubMap = new HashMap<Short, BeanHandler<?>>();
	static {

	}
	
	public static Map<Short, BeanHandler<?>> get() {
		return handlerStubMap;
	}
}