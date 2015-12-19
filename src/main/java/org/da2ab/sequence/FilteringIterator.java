package org.da2ab.sequence;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class FilteringIterator<T> implements Iterator<T> {
	private final Iterator<T> iterator;
	private final Predicate<T> predicate;
	private boolean foundNext;
	T foundValue;

	public FilteringIterator(Iterator<T> iterator, Predicate<T> predicate) {
		this.iterator = iterator;
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (foundNext) { // already checked
			return true;
		}

		do { // find next matching, bail out if EOF
			foundNext = iterator.hasNext();
			if (!foundNext)
				return false;
		} while (!predicate.test(foundValue = iterator.next()));

		// found matching value
		return true;
	}

	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		T nextValue = foundValue;
		foundNext = false;
		foundValue = null;
		return nextValue;
	}
}
