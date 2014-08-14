package fiber.mapdb.collectionwrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import fiber.common.Wrapper;
import fiber.io.Log;

public class WList<W> extends Wrapper<List<W>> implements List<W> {
	public static <V> WList<V> create(List<V> set) {
		return create(set.getClass(), set, Wrapper.NONE_NOTIFIER);
	}
	
	public static <V> WList<V> create(Class<?> c, List<V> set) {
		return create(c, set, Wrapper.NONE_NOTIFIER);
	}
	
	public static <V> WList<V> create(Class<?> c, List<V> set, Notifier n) {
		return new WList<V>(c, set, n);
	}
	
	private final Class<? extends List<W>> clazz;
	@SuppressWarnings("unchecked")
	public WList(Class<?> c, List<W> w, fiber.common.Wrapper.Notifier n) {
		super(w, n);
		this.clazz = (Class<? extends List<W>>) c;
	}


	@Override
	public boolean add(W arg0) {
		checkModify();
		return this.data.add(arg0);
	}

	@Override
	public void add(int arg0, W arg1) {
		checkModify();
		this.data.add(arg0, arg1);	
	}

	@Override
	public boolean addAll(Collection<? extends W> arg0) {
		if(arg0.isEmpty()) return false;
		checkModify();
		return this.data.addAll(arg0);
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends W> arg1) {
		if(arg1.isEmpty()) return false;
		checkModify();
		return this.data.addAll(arg0, arg1);
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
	public boolean contains(Object arg0) {
		return this.data.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return this.data.containsAll(arg0);
	}

	@Override
	public W get(int arg0) {
		return this.data.get(arg0);
	}

	@Override
	public int indexOf(Object arg0) {
		return this.data.indexOf(arg0);
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
	public int lastIndexOf(Object arg0) {
		return this.data.lastIndexOf(arg0);
	}

	@Override
	public ListIterator<W> listIterator() {
		return this.data.listIterator();
	}

	@Override
	public ListIterator<W> listIterator(int arg0) {
		return this.data.listIterator(arg0);
	}

	@Override
	public boolean remove(Object arg0) {
		checkModify();
		return this.data.remove(arg0);
	}

	@Override
	public W remove(int arg0) {
		checkModify();
		return this.data.remove(arg0);
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		if(arg0.isEmpty() || this.data.isEmpty()) return false;
		checkModify();
		return this.data.removeAll(arg0);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		if(arg0.isEmpty() || this.data.isEmpty()) return false;
		checkModify();
		return this.data.retainAll(arg0);
	}

	@Override
	public W set(int arg0, W arg1) {
		checkModify();
		return this.set(arg0, arg1);
	}

	@Override
	public int size() {
		return this.data.size();
	}

	@Override
	public List<W> subList(int arg0, int arg1) {
		return this.data.subList(arg0, arg1);
	}

	@Override
	public Object[] toArray() {
		return this.data.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return this.data.toArray(arg0);
	}

	public List<W> newInstance() {
		try {
			return this.clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// impossible
		}
		return null;
	}
	
	@Override
	public List<W> shallowClone() {
		List<W> newList = newInstance();
		newList.addAll(this.data);
		return newList;
	}
	
	public static void test(List<Integer> list) {
		WList<Integer> w = WList.create(list);
		int N = 10;
		for(int i = 0 ; i < N ; i++) {
			list.add(i);
		}
		assert(list.size() == N);
		assert(w.size() == N);
		assert(!w.isModify());
		
		w.add(7);
		assert(w.isModify());
		w.add(N, N * N);
		assert(w.size() == N + 2);
		Log.trace("wrapper:%s", w);
		for(int i = 0 ; i < N + 2 ; i++) {
			w.remove(N  + 1 - i);
			assert(w.size() == N  + 1 - i);
		}
		Log.trace("wrapper:%s", w);
	}
	
	public static void test2(List<Integer> list) {
		WList<Integer> w = WPList.create(list);
		int N = 10;
		for(int i = 0 ; i < N ; i++) {
			list.add(i);
		}
		assert(list.size() == N);
		assert(w.size() == N);
		assert(!w.isModify());
		
		w.add(7);
		assert(w.isModify());
		w.add(N, N * N);
		assert(w.size() == N + 2);
		Log.trace("wrapper:%s", w);
		for(int i = 0 ; i < N + 2 ; i++) {
			w.remove(N  + 1 - i);
			assert(w.size() == N  + 1 - i);
		}
		Log.trace("wrapper:%s", w);
	}

	public static void main(String[] args) {
		test(new LinkedList<Integer>());
		test(new ArrayList<Integer>());
		test(new fiber.pcollections.ArrayList<Integer>());
		test2(new fiber.pcollections.ArrayList<Integer>());
		//test2(new LinkedList<Integer>());
	}

}
