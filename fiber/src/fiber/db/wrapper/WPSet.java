package fiber.db.wrapper;

import java.util.Set;

import fiber.db.Wrapper;
import fiber.pcollections.HashSet;

public class WPSet<E> extends WSet<E> {
	public static <V> WPSet<V> create(Set<V> set) {
		return create(set.getClass(), set, Wrapper.NONE_NOTIFIER);
	}
	
	public static <V> WPSet<V> create(Class<?> c, Set<V> set) {
		return create(c, set, Wrapper.NONE_NOTIFIER);
	}
	
	public static <V> WPSet<V> create(Class<?> c, Set<V> set, Notifier n) {
		return new WPSet<V>(c, set, n);
	}
	
	public WPSet(Class<?> c, Set<E> w, Notifier n) {
		super(c, w, n);
		assert(c == HashSet.class);
	}

	@Override
	public Set<E> shallowClone() {
		return ((HashSet<E>)this.data).shallowClone();
	}
}
