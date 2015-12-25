package org.d2ab.sequence;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SkippingIterator<T> implements Iterator<T> {
	private final Iterator<T> iterator;
	private final int skip;
	boolean skipped;

	public SkippingIterator(Iterator<T> iterator, int skip) {
		this.iterator = iterator;
		this.skip = skip;
	}

	@Override
	public boolean hasNext() {
		if (!skipped) {
			Iterators.skip(skip, iterator);
			skipped = true;
		}

		return iterator.hasNext();
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return iterator.next();
	}
}
