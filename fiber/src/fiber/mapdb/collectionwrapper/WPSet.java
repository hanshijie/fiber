package fiber.mapdb.collectionwrapper;

import java.util.Set;

import fiber.pcollections.HashSet;

public class WPSet<E> extends WSet<E> {

	public WPSet(Class<?> c, Set<E> w, Notifier n) {
		super(c, w, n);
		assert(c == HashSet.class);
	}

	@Override
	public Set<E> shallowClone() {
		return ((HashSet<E>)this.data).shallowClone();
	}
}
