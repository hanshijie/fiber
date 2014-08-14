package fiber.pcollections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.pcollections.Empty;
import org.pcollections.PSet;

public class HashSet<E> implements Set<E> {
	private PSet<E> set;
	
	public HashSet() {
		this.set = Empty.set();
	}
	
	public HashSet(PSet<E> set) {
		this.set = set;
	}

	@Override
	public boolean add(E arg0) {
		PSet<E> old = this.set;
		this.set = this.set.plus(arg0);
		return old != this.set;
	}

	@Override
	public boolean addAll(Collection<? extends E> arg0) {
		PSet<E> old = this.set;
		this.set = this.set.plusAll(arg0);
		return old != this.set;
	}

	@Override
	public void clear() {
		this.set = Empty.set();
	}

	@Override
	public boolean contains(Object arg0) {
		return this.set.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return this.set.containsAll(arg0);
	}

	@Override
	public boolean isEmpty() {
		return this.set.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return this.set.iterator();
	}

	@Override
	public boolean remove(Object arg0) {
		PSet<E> old = this.set;
		this.set = this.set.minus(arg0);
		return old != this.set;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		PSet<E> old = this.set;
		this.set = this.set.minusAll(arg0);
		return old != this.set;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean retainAll(Collection<?> arg0) {
		PSet<E> old = this.set;
		ArrayList<E> retains = new ArrayList<E>();
		for(Object e : arg0) {
			if(this.set.contains(e)) {
				retains.add((E)e);
			}
		}
		if(retains.size() == old.size()) return false;
		this.set = Empty.<E>set().plusAll(retains);
		return true;
	}

	@Override
	public int size() {
		return this.set.size();
	}

	@Override
	public Object[] toArray() {
		return this.set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return this.set.toArray(arg0);
	}
	
	@Override
	public String toString() {
		return this.set.toString();
	}
	
	public HashSet<E> shallowClone() {
		return new HashSet<E>(this.set);
	}
	

	public static void main(String[] args) {


	}

}
