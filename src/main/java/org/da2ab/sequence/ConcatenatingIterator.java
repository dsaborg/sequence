package org.da2ab.sequence;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;

public class ConcatenatingIterator<T> implements Iterator<T> {
	private final Iterator<? extends Iterable<? extends T>> iterables;
	private Iterator<? extends T> iterator;

	@SafeVarargs
	public ConcatenatingIterator(Iterable<? extends T>... iterables) {
		this(asList(iterables));
	}

	public ConcatenatingIterator(Iterable<? extends Iterable<? extends T>> iterables) {
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
