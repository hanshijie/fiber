package fiber.mapdb.collectionwrapper;

import java.util.Map;

import fiber.pcollections.HashMap;

public class WPMap<K, V> extends WMap<K, V> {

	public WPMap(Class<?> c, Map<K, V> w, fiber.common.Wrapper.Notifier n) {
		super(c, w, n);
		assert(c == HashMap.class);
	}
	
	@Override
	public Map<K, V> shallowClone() {
		return ((HashMap<K, V>)this.data).shallowClone();
	}

}
