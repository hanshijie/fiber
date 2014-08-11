package fiber.mapdb;

import java.util.concurrent.ConcurrentHashMap;

import fiber.io.Bean;

public class Table {
	private final ConcurrentHashMap<TKey, TValue> map = new ConcurrentHashMap<TKey, TValue>();
	
	public TValue get(TKey key) {
		TValue value = map.get(key);
		if(value == null) {
			value = map.putIfAbsent(key, new TValue(loadValue(key)));
		}
		return value;
	}
	public TValue put(TKey key, TValue value) { return map.put(key, value); }
	public TValue putIfAbsent(TKey key, TValue value) {	return map.putIfAbsent(key, value);	}
	
	public void onUpdate(TKey key, TValue value) { }
	
	/**
	 * 删除value.isempty() 的数据.
	 * 因为它们存在没有意义.
	 */
	public void shrink() { }
	protected Bean<?> loadValue(TKey key) { return null; }
	
}
