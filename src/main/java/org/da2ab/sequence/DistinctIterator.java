package org.da2ab.sequence;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class DistinctIterator<T> implements Iterator<T> {
	private Iterator<T> iterator;
	private Set<T> seen = new HashSet<T>();
	private T next;
	private boolean gotNext;

	public DistinctIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		if (gotNext)
			return true;

		while (!gotNext && iterator.hasNext()) {
			T next = iterator.next();
			if (seen.add(next)) {
				gotNext = true;
				this.next = next;
			}
		}

		return gotNext;
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		T result = next;
		gotNext = false;
		next = null;
		return result;
	}
}
