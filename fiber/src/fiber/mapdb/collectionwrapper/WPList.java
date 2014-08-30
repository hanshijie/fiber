package fiber.mapdb.collectionwrapper;

import java.util.List;

import fiber.db.Wrapper;
import fiber.pcollections.ArrayList;


public class WPList<E> extends WList<E> {
	public static <V> WPList<V> create(List<V> set) {
		return create(set.getClass(), set, Wrapper.NONE_NOTIFIER);
	}
	
	public static <V> WPList<V> create(Class<?> c, List<V> set) {
		return create(c, set, Wrapper.NONE_NOTIFIER);
	}
	
	public static <V> WPList<V> create(Class<?> c, List<V> set, Notifier n) {
		return new WPList<V>(c, set, n);
	}
	
	public WPList(Class<?> c, List<E> w, Notifier n) {
		super(c, w, n);
		assert(c == ArrayList.class);
	}
	
	@Override
	public List<E> shallowClone() {
		return ((ArrayList<E>)this.data).shallowClone();
	}

}
