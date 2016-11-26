package org.d2ab.iterator;

import java.util.Iterator;

/**
 * A {@link DelegatingIterator} that delegates to another {@link Iterator} of the same type.
 */
public abstract class UnaryIterator<T> extends DelegatingIterator<T, Iterator<T>, T> {
	public UnaryIterator(Iterator<T> iterator) {
		super(iterator);
	}
}
