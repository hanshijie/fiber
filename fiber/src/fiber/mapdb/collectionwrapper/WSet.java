package fiber.mapdb.collectionwrapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import fiber.common.Wrapper;
import fiber.io.Log;

public class WSet<W> extends Wrapper<Set<W>> implements Set<W> {
	public static <V> WSet<V> create(Set<V> set) {
		return create(set.getClass(), set, Wrapper.NONE_NOTIFIER);
	}
	
	public static <V> WSet<V> create(Class<?> c, Set<V> set) {
		return create(c, set, Wrapper.NONE_NOTIFIER);
	}
	
	public static <V> WSet<V> create(Class<?> c, Set<V> set, Notifier n) {
		return new WSet<V>(c, set, n);
	}
	
	private final Class<? extends Set<W>> clazz;
	
	@SuppressWarnings("unchecked")
	public WSet(Class<?> c, Set<W> w, fiber.common.Wrapper.Notifier n) {
		super(w, n);
		this.clazz = (Class<? extends Set<W>>) c;
	}


	@Override
	public boolean add(W e) {
		checkModify();
		return this.data.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends W> c) {
		if(c.isEmpty()) return true;
		checkModify();
		return this.data.addAll(c);
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
	public boolean contains(Object o) {
		return this.data.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.data.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	@Override
	public Iterator<W> iterator() {
		return this.data.iterator();
	}

	@Override
	public boolean remove(Object o) {
		checkModify();
		return this.data.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if(c.isEmpty()) return false;
		checkModify();
		return this.data.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if(this.data.isEmpty()) return false;
		checkModify();
		return this.data.retainAll(c);
	}

	@Override
	public int size() {
		return this.data.size();
	}

	@Override
	public Object[] toArray() {
		return this.data.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.data.toArray(a);
	}
	
	public Set<W> newInstance() {
		try {
			return this.clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// impossible
		}
		return null;
	}
	
	@Override
	public Set<W> shallowClone() {
		Set<W> newSet =  this.newInstance();
		newSet.addAll(this.data);
		return newSet;
	}
	

	public static void test(Set<Integer> set) {
		int N = 10;
		for(int i = 0 ; i < N ; i++) {
			set.add(i);
		}
		WSet<Integer> w = WSet.create(set.getClass(), set);
		assert(w.size() == N);
		assert(!w.isModify());
		
		for(int i = N ; i < N * 2 ; i++) {
			w.add(i);
			assert(w.isModify());
			assert(w.size() == i + 1);
		}
		Log.trace("wrapper:%s", w);
		for(int i = 0 ; i < N * 2 ; i++) {
			w.remove(i);
			assert(w.size() == N * 2 - 1 - i);
		}
		Log.trace("wrapper:%s", w);
		assert(set.size() == N);
		
	}
	
	public static void main(String[] args) {
		test(new TreeSet<Integer>());
		test(new HashSet<Integer>());
		test(new fiber.pcollections.HashSet<Integer>());
	}

}
