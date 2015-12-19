package org.da2ab.sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ConcatenatingIterable<T> implements Iterable<T> {
	private final List<Iterable<T>> iterables = new ArrayList<>();

	public ConcatenatingIterable() {
	}

	@SafeVarargs
	public ConcatenatingIterable(Iterable<T>... iterables) {
		this.iterables.addAll(Arrays.asList(iterables));
	}

	@Override
	public Iterator<T> iterator() {
		return new ConcatenatingIterator<T>(iterables);
	}

	public <U> void addAll(Iterable<U> iterable) {
		for (U each : iterable) {
			if (each instanceof Iterable)
				add((Iterable<T>) each);
			else if (each instanceof Iterator)
				add((Iterator<T>) each);
			else if (each instanceof Object[])
				add(Arrays.asList((T[]) each));
			else
				throw new ClassCastException("Required an Iterable, Iterator or Array but got: " + each);
		}
	}

	public void add(Iterable<T> iterable) {
		iterables.add(iterable);
	}

	public void add(Iterator<T> iterator) {
		iterables.add(() -> iterator);
	}
}
