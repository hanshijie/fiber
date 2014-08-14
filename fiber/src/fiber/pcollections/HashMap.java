package fiber.pcollections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.pcollections.Empty;
import org.pcollections.PMap;

public class HashMap<K, V> implements Map<K, V> {
	private PMap<K, V> map;
	
	public HashMap() {
		this.map = Empty.map();
	}
	
	public HashMap(PMap<K, V> map) {
		this.map = map;
	}
	
	@Override
	public void clear() {
		map = Empty.map();
	}

	@Override
	public boolean containsKey(Object arg0) {
		return map.containsKey(arg0);
	}

	@Override
	public boolean containsValue(Object arg0) {
		return map.containsValue(arg0);
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return map.entrySet();
	}

	@Override
	public V get(Object arg0) {
		return map.get(arg0);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public V put(K arg0, V arg1) {
		V old = map.get(arg0);
		map = map.plus(arg0, arg1);
		return old;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> arg0) {
		map = map.plusAll(arg0);
	}

	@Override
	public V remove(Object arg0) {
		V old = map.get(arg0);
		map = map.minus(arg0);
		return old;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}
	
	@Override
	public String toString() {
		return this.map.toString();
	}
	
	public HashMap<K, V> shallowClone() {
		return new HashMap<K, V>(this.map);
	}
	

	public static void main(String[] args) {

	}

}
