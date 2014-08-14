package fiber.mapdb.collectionwrapper;

import java.util.Map;

import fiber.common.Wrapper;
import fiber.pcollections.HashMap;

public class WPMap<K, V> extends WMap<K, V> {
	public static <T, U> WPMap<T, U> create(Map<T, U> w) {
		return new WPMap<T, U>(w.getClass(), w, Wrapper.NONE_NOTIFIER);
	}
	
	public static <T, U> WPMap<T, U> create(Class<?> c, Map<T, U> w) {
		return new WPMap<T, U>(c, w, Wrapper.NONE_NOTIFIER);
	}
	
	public static <T, U> WPMap<T, U> create(Class<?> c, Map<T, U> w, Notifier n) {
		return new WPMap<T, U>(c, w, n);
	}
	
	public WPMap(Class<?> c, Map<K, V> w, fiber.common.Wrapper.Notifier n) {
		super(c, w, n);
		assert(c == HashMap.class);
	}
	
	@Override
	public Map<K, V> shallowClone() {
		return ((HashMap<K, V>)this.data).shallowClone();
	}

}
