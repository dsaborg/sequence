package org.da2ab.sequence;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ConcatenatingIterator<T> implements Iterator<T> {
	private final Iterator<Iterable<T>> iterables;
	private Iterator<T> iterator;

	@SafeVarargs
	public ConcatenatingIterator(Iterable<T>... iterables) {
		this(Arrays.asList(iterables));
	}

	public ConcatenatingIterator(Iterable<Iterable<T>> iterables) {
		this.iterables = iterables.iterator();
	}

	@Override
	public boolean hasNext() {
		if ((iterator == null || !iterator.hasNext()) && iterables.hasNext()) {
			iterator = iterables.next().iterator();
		}
		return iterator != null && iterator.hasNext();
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		return iterator.next();
	}
}
