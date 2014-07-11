package fiber.mapdb;

import java.util.concurrent.ConcurrentHashMap;

import fiber.io.Bean;

public class Table {
	private final ConcurrentHashMap<TKey, TValue> map = new ConcurrentHashMap<TKey, TValue>();
	
	public TValue get(TKey key) {
		TValue value = map.get(key);
		if(value == null) {
			value = map.putIfAbsent(key, new TValue());
		}
		synchronized(value) {
			if(!value.isLoaded()) {
				value.setValue(loadValue(key));
				value.setLoaded(true);
			}
		}
		return value;
	}
	public TValue put(TKey key, TValue value) { return map.put(key, value); }
	public TValue putIfAbsent(TKey key, TValue value) {
		return map.putIfAbsent(key, value);
	}
	public boolean check(TKey key, TValue value) {
		TValue old = map.get(key);
		return old == value;
	}
	public void update(TKey key, TValue value) {
		map.put(key, value);
		onUpdate(key, value);
	}
	protected void onUpdate(TKey key, TValue value) { }
	protected Bean<?> loadValue(TKey key) { return null; }
}
