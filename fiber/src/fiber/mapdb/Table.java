package fiber.mapdb;

import java.util.concurrent.ConcurrentHashMap;

public class Table {
	private final ConcurrentHashMap<TKey, TValue> map = new ConcurrentHashMap<TKey, TValue>();
	
	public TValue get(TKey key) { return map.get(key); }
	public TValue put(TKey key, TValue value) { return map.put(key, value); }
	public TValue putIfAbsent(TKey key, TValue value) {
		return map.putIfAbsent(key, value);
	}
	public boolean check(TKey key, TValue value) {
		TValue old = map.get(key);
		return old == value && old.getValue() == value.getValue();
	}
	public void update(TKey key, TValue value) {
		map.put(key, value);
		onUpdate(key, value);
	}
	public void onUpdate(TKey key, TValue value) { }
}
