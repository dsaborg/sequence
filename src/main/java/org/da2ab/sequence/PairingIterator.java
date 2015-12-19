package org.da2ab.sequence;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PairingIterator<T> implements Iterator<Pair<T, T>> {
	private Iterator<T> iterator;
	private T previous;
	private boolean gotPrevious;

	public PairingIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		boolean hasNext = iterator.hasNext();
		if (gotPrevious || !hasNext) {
			return hasNext;
		}

		previous = iterator.next();
		gotPrevious = true;
		return iterator.hasNext();
	}

	@Override
	public Pair<T, T> next() {
		if (!iterator.hasNext())
			throw new NoSuchElementException();

		T next = iterator.next();
		Pair result = Pair.of(previous, next);
		previous = next;
		return result;
	}
}
