package org.d2ab.sequence;

import java.util.Iterator;
import java.util.function.Consumer;

public class PeekingIterator<T> implements Iterator<T> {
	private final Iterator<T> iterator;
	private final Consumer<T> action;

	public PeekingIterator(Iterator<T> iterator, Consumer<T> action) {
		this.iterator = iterator;
		this.action = action;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public T next() {
		T next = iterator.next();
		action.accept(next);
		return next;
	}
}
