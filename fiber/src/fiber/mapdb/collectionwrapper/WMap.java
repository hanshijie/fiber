package fiber.mapdb.collectionwrapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import fiber.common.Wrapper;
import fiber.io.Log;

public class WMap<K, V> extends Wrapper<Map<K, V>> implements Map<K, V> {
	public static <T, U> WMap<T, U> create(Map<T, U> w) {
		return new WMap<T, U>(w.getClass(), w, Wrapper.NONE_NOTIFIER);
	}
	
	public static <T, U> WMap<T, U> create(Class<?> c, Map<T, U> w) {
		return new WMap<T, U>(c, w, Wrapper.NONE_NOTIFIER);
	}
	
	public static <T, U> WMap<T, U> create(Class<?> c, Map<T, U> w, Notifier n) {
		return new WMap<T, U>(c, w, n);
	}
	
	
	
	private final Class<? extends Map<K, V>> clazz; 
	@SuppressWarnings("unchecked")
	public WMap(Class<?> c, Map<K, V> w, Notifier n) {
		super(w, n);
		clazz = (Class<? extends Map<K, V>>) c;
	}
	
	public Map<K, V> newInstance() {
		try {
			return this.clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// impossible
		}
		return null;
	}
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		return this.data.entrySet();
	}

	@Override
	public void clear() {
		if(this.data.isEmpty()) return;
		if(this.isModify()) {
			this.data.clear();
		} else {
			this.data = newInstance();
			this.notifier.onChange(this.data);
		}
	}

	@Override
	public boolean containsKey(Object key) {
		return this.data.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.data.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return this.data.get(key);
	}

	@Override
	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return this.data.keySet();
	}

	@Override
	public V put(K key, V value) {
		checkModify();
		return this.data.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if(m.isEmpty()) return;
		checkModify();
		this.data.putAll(m);
	}

	@Override
	public V remove(Object key) {
		if(this.isModify()) {
			return this.data.remove(key);
		} else {
			if(!this.data.containsKey(key)) return null;
			checkModify();
			return this.data.remove(key);
		}
	}

	@Override
	public int size() {
		return this.data.size();
	}

	@Override
	
	public Collection<V> values() {
		return this.data.values();
	}

	@Override
	public Map<K, V> shallowClone() {
		Map<K, V> newMap = newInstance();
		newMap.putAll(this.data);
		return newMap;
	}
	
	public static void assert2(int k, int v) {
		assert(v == k * k);
	}
	public static void test(Map<Integer, Integer> map) {
		int N = 10;
		for(int i = 0 ; i < N ; i++) {
			map.put(i, i * i);
		}
		WMap<Integer, Integer> w = WMap.create(map.getClass(), map);
		
		for(Map.Entry<Integer, Integer> e : map.entrySet()) {
			int k = e.getKey();
			int v = e.getValue();
			assert2(k, v);
		}
		
		for(Map.Entry<Integer, Integer> e : w.entrySet()) {
			int k = e.getKey();
			int v = e.getValue();
			assert2(k, v);
		}
		assert(w.size() == N);
		assert(w.keySet().size() == N);
		assert(w.values().size() == N);
		
		assert2(1, w.get(1));
		assert(w.get(N) == null);
		
		w.remove(-1);
		assert(!w.isModify());
		assert(w.size() == N);
		w.remove(0);
		assert(w.size() == N - 1);
		w.put(N, N * N);
		assert(w.size() == N);
		w.put(1, 111);
		assert(w.size() == N);
		w.put(N + 1, (N + 1) * (N + 1));
		assert(w.size() == N + 1);
		assert(w.keySet().size() == N + 1);
		assert(w.values().size() == N + 1);
		
		HashMap<Integer, Integer> newM = new HashMap<Integer, Integer>();
		for(int i = N * 2 ; i < N * 3 ; i++) {
			newM.put(i, i * i + 10000);
		}
		w.putAll(newM);
		assert(w.size() == N * 2 + 1);
		assert(w.keySet().size() == w.size());
		assert(w.values().size() == w.size());
		assert(map.size() == N);
		Log.trace("wrapper:%s", w);
		Log.trace("============>");

		
		Log.trace("############");
		
	}
	
	public static void main(String[] args) {
		test(new HashMap<Integer, Integer>());
		test(new TreeMap<Integer, Integer>());
		test(new ConcurrentHashMap<Integer, Integer>());
		test(new fiber.pcollections.HashMap<Integer, Integer>());
	}

}
