package org.d2ab.iterator;

import java.util.Iterator;

public class ImmutableIterator<T> extends DelegatingUnaryIterator<T> {
	public ImmutableIterator(Iterator<? extends T> iterator) {
		super(iterator);
	}

	@Override
	public T next() {
		return iterator.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
