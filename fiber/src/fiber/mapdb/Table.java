package fiber.mapdb;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import fiber.io.Bean;

public class Table {
	private final ConcurrentLinkedHashMap<TKey, TValue> map;
	private final int id;
	public Table(int id, int initsize, int maxsize) {
		this.id = id;
		this.map = new ConcurrentLinkedHashMap.Builder<TKey, TValue>().
				initialCapacity(initsize).maximumWeightedCapacity(maxsize).
				concurrencyLevel(Runtime.getRuntime().availableProcessors()).build();
	}
	
	public final int getId() { return id; }
	
	public TValue get(TKey key) {
		TValue value = map.get(key);
		if(value == null) {
			// 此处在极端情形下有bug.
			// 当两个线程同时访问同一个key,
			// 其中一个线程取回数据,设置值，做了某些修改,
			// 然后又因为调整map大小,删除这个entry,
			// 之后第二个线程才返回, 用旧值设置了map.
			// 这时便出现了不一致情况.
			// 好在这种情形实践中可以保证不让它发生.
			value = map.putIfAbsent(key, new TValue(loadValue(key)));
		}
		return value;
	}
	public TValue put(TKey key, TValue value) { return map.put(key, value); }
	public TValue putIfAbsent(TKey key, TValue value) {	return map.putIfAbsent(key, value);	}
	
	public void onUpdate(TKey key, TValue value) { }
	protected Bean<?> loadValue(TKey key) { return null; }
}
