package fiber.mapdb;

import java.util.concurrent.ConcurrentHashMap;

import fiber.io.MarshalException;
import fiber.io.OctetsStream;

public abstract class Table {
	private final ConcurrentHashMap<Object, TValue> map;
	private final int id;
	public Table(int id) {
		this.id = id;
		this.map = new ConcurrentHashMap<Object, TValue>();
	}
	
	public final int getId() { return id; }
	public final int size() { return map.size(); }
	
	public TValue get(Object key) {
		TValue value = map.get(key);
		if(value == null) {
			value = map.putIfAbsent(key, new TValue(loadValue(key)));
		}
		return value;
	}
	public Object put(Object key, TValue value) { return map.put(key, value); }
	public Object putIfAbsent(Object key, TValue value) {	return map.putIfAbsent(key, value);	}
	
	public void onUpdate(Object key, TValue value) { }
	protected Object loadValue(Object key) { return null; }
	
	/**
	 * 注意,删除某元素时,必须保证没有线程在操作这个元素.
	 * 通常做法是加锁.
	 * @param key
	 */
	public void remove(Object key) {
		TValue value = this.map.remove(key);
		if(value != null) {
			value.setShrink(true);
		}
	}
	public void shrink() { }
	
	public abstract void marshalKey(OctetsStream os, Object key);
	public abstract Object unmarshalKey(OctetsStream os) throws MarshalException;	
	public abstract void marshalValue(OctetsStream os, Object value);
	public abstract Object unmarshalValue(OctetsStream os) throws MarshalException;

}
