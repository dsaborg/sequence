package org.da2ab.sequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;

public class ConcatenatingIterable<T> implements Iterable<T> {
	private final List<Iterable<T>> iterables = new ArrayList<>();

	public ConcatenatingIterable() {
	}

	@SafeVarargs
	public ConcatenatingIterable(Iterable<T>... iterables) {
		this.iterables.addAll(asList(iterables));
	}

	public static <U, T> ConcatenatingIterable<U> from(Iterable<T> iterable) {
		return new ConcatenatingIterable<U>().addAll(iterable);
	}

	public <U> ConcatenatingIterable<T> addAll(Iterable<U> iterable) {
		for (U each : iterable) {
			if (each == null || each instanceof Iterable)
				add((Iterable<T>) each);
			else if (each instanceof Iterator)
				add((Iterator<T>) each);
			else if (each instanceof Object[])
				add(asList((T[]) each));
			else
				throw new ClassCastException("Required an Iterable, Iterator or Array but got: " + each.getClass());
		}
		return this;
	}

	public ConcatenatingIterable<T> add(Iterable<T> iterable) {
		iterables.add(iterable);
		return this;
	}

	public ConcatenatingIterable<T> add(Iterator<T> iterator) {
		iterables.add(() -> iterator);
		return this;
	}

	@Override
	public Iterator<T> iterator() {
		return new ConcatenatingIterator<T>(iterables);
	}

	public ConcatenatingIterable<T> add(T[] objects) {
		iterables.add(asList(objects));
		return this;
	}
}
