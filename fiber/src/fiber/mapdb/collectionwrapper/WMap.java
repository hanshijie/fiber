package fiber.mapdb.collectionwrapper;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import fiber.app.server.Wrapper;

public final class WMap<K, V> extends Wrapper<Map<K, V>> implements Map<K, V> {
	private final Class<? extends Map<K, V>> clazz; 
	private boolean flat;
	private Map<K, V> insertupdateMap;
	private Set<Object> removeSet;
	public WMap(Class<? extends Map<K, V>> c, Map<K, V> w, Notifier n) {
		super(w, n);
		clazz = c;
	}
	
	public Map<K, V> newInstance() {
		try {
			return this.clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// impossible
		}
		return null;
	}
	
	public void doflat() {
		this.data = shallowClone();
		this.insertupdateMap = null;
		this.removeSet = null;
		flat = true;
		this.notifier.onChange(this.data);
	}
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		if(!flat) {
			doflat();
		}
		return this.data.entrySet();
	}

	@Override
	public void commit() {
		if(!flat) {
			if(this.removeSet != null) {
				for(Object key : this.removeSet) {
					this.data.remove(key);
				}
			}
			if(this.insertupdateMap != null) {
				this.data.putAll(this.insertupdateMap);
			}
		}
	}

	@Override
	public void clear() {
		if(flat) {
			this.data.clear();
		} else {
			if(this.data.isEmpty() && this.insertupdateMap == null) return;
			flat = true;
			this.insertupdateMap = null;
			this.removeSet = null;
			this.data =  newInstance();
			this.notifier.onChange(this.data);
		}	
	}

	@Override
	public boolean containsKey(Object key) {
		if(flat) {
			return this.data.containsKey(key);
		} else {
			return (this.insertupdateMap != null && this.insertupdateMap.containsKey(key)) || 
				(this.data.containsKey(key) && (this.removeSet == null || !this.removeSet.contains(key)));
		}
	}

	@Override
	public boolean containsValue(Object value) {
		if(flat || (this.insertupdateMap == null && this.removeSet == null)) {
			return this.data.containsValue(value);
		} else {
			doflat();
			return this.data.containsValue(value);
		}
	}

	@Override
	public V get(Object key) {
		if(flat) {
			return this.data.get(key);
		} else {
			if(this.removeSet != null & this.removeSet.contains(key)) return null;
			if(this.insertupdateMap != null) {
				V value = this.insertupdateMap.get(key);
				if(value != null) return value;
			}
			return this.data.get(key);
		}
	}

	@Override
	public boolean isEmpty() {
		if(flat || (this.insertupdateMap == null || this.insertupdateMap.isEmpty())) {
			return this.data.isEmpty();
		} else {
			doflat();
			return this.data.isEmpty();
		}
	}

	@Override
	public Set<K> keySet() {
		if(flat || (this.insertupdateMap == null || this.insertupdateMap.isEmpty())) {
			return this.data.keySet();
		} else {
			doflat();
			return this.data.keySet();
		}
	}

	@Override
	public V put(K key, V value) {
		if(flat) {
			return this.data.put(key, value);
		} else {
			if(this.insertupdateMap == null && this.removeSet == null) {
				this.notifier.onChange(this.data);
			}
			
			if(this.removeSet != null) {
				this.removeSet.remove(key);
			}
			if(this.insertupdateMap != null) {
				V old = this.insertupdateMap.put(key, value);
				return (old == null) ? this.data.get(key) : old;
			} else {
				this.insertupdateMap = new TreeMap<K, V>();
				this.insertupdateMap.put(key, value);
				return this.data.get(key);
			}
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if(m.isEmpty()) return;
		if(flat) {
			this.data.putAll(m);
		} else {
			if(this.insertupdateMap == null && this.removeSet == null) {
				this.notifier.onChange(this.data);
			}
			if(this.removeSet != null) {
				for(K key : m.keySet()) { 
					this.removeSet.remove(key);
				}
			}
			if(this.insertupdateMap == null) {
				this.insertupdateMap = new TreeMap<K, V>();
			}
			this.insertupdateMap.putAll(m);
		}	
		
	}

	@Override
	public V remove(Object key) {
		if(flat) {
			return this.data.remove(key);
		} else {
			V old = (this.insertupdateMap != null) ? insertupdateMap.remove(key) : null;
			old = old != null ? old : this.data.get(key);
			if(old != null) {
				if(this.removeSet == null) {
					this.removeSet = new TreeSet<Object>();
					if(this.insertupdateMap == null) {
						this.notifier.onChange(this.data);
					}
				}
				this.removeSet.add(key);
			}
			return old;
		}
	}

	@Override
	public int size() {
		if(flat || (this.insertupdateMap == null && this.removeSet == null)) {
			return this.data.size();
		} else {
			int s = this.data.size();
			if(this.insertupdateMap != null) {
				for(K key : this.insertupdateMap.keySet()) {
					if(!this.data.containsKey(key)) {
						s++;
					}
				}
			}
			if(this.removeSet != null) {
				s -= this.removeSet.size();
			}
			return s;
		}
	}

	@Override
	
	public Collection<V> values() {
		if(flat || (this.insertupdateMap == null && this.removeSet == null)) {
			return this.data.values();
		} else {
			doflat();
			return this.data.values();
		}
	}

	@Override
	public Map<K, V> shallowClone() {
		Map<K, V> newMap = newInstance();
		newMap.putAll(this.data);
		if(this.insertupdateMap != null) {
			newMap.putAll(this.insertupdateMap);
		}
		if(this.removeSet != null) {
			for(Object key : this.removeSet) {
				newMap.remove(key);
			}
		}
		return newMap;
	}

}
