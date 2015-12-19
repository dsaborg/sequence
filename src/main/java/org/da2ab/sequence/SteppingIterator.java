package org.da2ab.sequence;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SteppingIterator<T> implements Iterator<T> {
	private final Iterator<T> iterator;
	private final int step;
	private boolean gotNext;
	private T next;

	public SteppingIterator(Iterator<T> iterator, int step) {
		this.iterator = iterator;
		this.step = step;
	}

	@Override
	public boolean hasNext() {
		if (gotNext)
			return true;

		if (!iterator.hasNext())
			return false;

		next = iterator.next();

		// skip steps
		int i = step;
		while (--i > 0 && iterator.hasNext())
			iterator.next();
		gotNext = true;

		return true;
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		T result = next;
		next = null;
		gotNext = false;
		return result;
	}
}
