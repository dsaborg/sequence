package org.d2ab.sequence;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LimitingIterator<T> implements Iterator<T> {
	private final Iterator<T> iterator;
	private final int limit;
	int count;

	public LimitingIterator(Iterator<T> iterator, int limit) {
		this.iterator = iterator;
		this.limit = limit;
	}

	@Override
	public boolean hasNext() {
		return count < limit && iterator.hasNext();
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		T next = iterator.next();
		count++;
		return next;
	}
}
