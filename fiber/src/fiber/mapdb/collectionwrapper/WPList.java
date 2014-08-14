package fiber.mapdb.collectionwrapper;

import java.util.List;

import fiber.pcollections.ArrayList;


public class WPList<E> extends WList<E> {

	public WPList(Class<?> c, List<E> w, Notifier n) {
		super(c, w, n);
		assert(c == ArrayList.class);
	}
	
	@Override
	public List<E> shallowClone() {
		return ((ArrayList<E>)this.data).shallowClone();
	}

}
