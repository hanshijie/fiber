package fiber.mapdb;

import java.util.concurrent.ConcurrentHashMap;

import fiber.common.Marshaller;
import fiber.io.MarshalException;
import fiber.io.OctetsStream;

public class Table {
	private final ConcurrentHashMap<Object, TValue> map;
	private final int id;
	private final boolean persist;
	private final Marshaller msKey;
	private final Marshaller msValue;

	public Table(int id, boolean persist, Marshaller msKey, Marshaller msValue) {
		this.id = id;
		this.persist = persist;
		this.map = new ConcurrentHashMap<Object, TValue>();
		this.msKey = msKey;
		this.msValue = msValue;
	}
	
	public final int getId() { return id; }
	public final boolean isPersist() { return persist; }
	public final int size() { return map.size(); }
	
	public TValue get(Object key) {
		TValue value = map.get(key);
		if(value == null) {
			TValue newValue = new TValue(loadValue(key));
			value = map.putIfAbsent(key, newValue);
			return value != null ? value : newValue;
		} else {
			return value;
		}
	}
	public Object put(Object key, TValue value) { return map.put(key, value); }
	public Object putIfAbsent(Object key, TValue value) {	return map.putIfAbsent(key, value);	}
	
	public void onUpdate(Object key, TValue value) { }
	protected Object loadValue(Object key) { return null; }
	
	/**
	 * 注意,删除某元素时,必须保证没有线程在操作这个元素.
	 * 通常做法是加锁.
	 */
	public void remove(Object key) {
		TValue value = this.map.remove(key);
		if(value != null) {
			value.setShrink(true);
		}
	}
	public void shrink() { }
	
	public final void marshalKey(OctetsStream os, Object key) {
		this.msKey.marshal(os, key);
	}
	public final Object unmarshalKey(OctetsStream os) throws MarshalException {
		return this.msKey.unmarshal(os);
	}
	public final void marshalValue(OctetsStream os, Object value) {
		this.msValue.marshal(os, value);
	}
	public final Object unmarshalValue(OctetsStream os) throws MarshalException {
		return this.msValue.unmarshal(os);
	}

}
