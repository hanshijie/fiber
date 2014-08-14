package fiber.pcollections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.pcollections.Empty;
import org.pcollections.PVector;

public class ArrayList<E> implements List<E> {
	private PVector<E> list;

	public ArrayList() {
		this.list = Empty.vector();
	}
	
	public ArrayList(PVector<E> list) {
		this.list = list;
	}
	
	@Override
	public boolean add(E arg0) {
		this.list = this.list.plus(arg0);
		return true;
	}

	@Override
	public void add(int arg0, E arg1) {
		this.list = this.list.plus(arg0, arg1);
	}

	@Override
	public boolean addAll(Collection<? extends E> arg0) {
		PVector<E> old = this.list;
		this.list = this.list.plusAll(arg0);
		return this.list != old;
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends E> arg1) {
		PVector<E> old = this.list;
		this.list = this.list.plusAll(arg0, arg1);
		return this.list != old;
	}

	@Override
	public void clear() {
		this.list = Empty.vector();
	}

	@Override
	public boolean contains(Object arg0) {
		return this.list.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return this.list.containsAll(arg0);
	}

	@Override
	public E get(int arg0) {
		return this.list.get(arg0);
	}

	@Override
	public int indexOf(Object arg0) {
		return this.list.indexOf(arg0);
	}

	@Override
	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return this.list.iterator();
	}

	@Override
	public int lastIndexOf(Object arg0) {
		return this.list.lastIndexOf(arg0);
	}

	@Override
	public ListIterator<E> listIterator() {
		return this.list.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int arg0) {
		return this.list.listIterator(arg0);
	}

	@Override
	public boolean remove(Object arg0) {
		PVector<E> old = this.list;
		this.list = this.list.minus(arg0);
		return old != this.list;
	}

	@Override
	public E remove(int arg0) {
		E e = this.list.get(arg0);
		this.list = this.list.minus(arg0);
		return e;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		PVector<E> old = this.list;
		this.list = this.list.minusAll(arg0);
		return old != this.list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean retainAll(Collection<?> arg0) {
		PVector<E> old = this.list;
		ArrayList<E> retains = new ArrayList<E>();
		for(Object e : arg0) {
			if(this.list.contains(e)) {
				retains.add((E)e);
			}
		}
		if(retains.size() == old.size()) return false;
		this.list = Empty.<E>vector().plusAll(retains);
		return true;
	}

	@Override
	public E set(int arg0, E arg1) {
		E e = this.list.get(arg0);
		this.list = this.list.with(arg0, arg1);
		return e;
	}

	@Override
	public int size() {
		return this.list.size();
	}

	@Override
	public List<E> subList(int arg0, int arg1) {
		return this.list.subList(arg0, arg1);
	}

	@Override
	public Object[] toArray() {
		return this.list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return this.list.toArray(arg0);
	}
	
	@Override
	public String toString() {
		return this.list.toString();
	}

	public ArrayList<E> shallowClone() {
		return new ArrayList<E>(this.list);
	}

	public static void main(String[] args) {


	}
}
