package org.d2ab.sequence;

import java.util.Iterator;
import java.util.function.UnaryOperator;

public class RecursiveIterator<T> implements Iterator<T> {
	private final T seed;
	private final UnaryOperator<T> op;
	private T previous;
	private boolean hasPrevious;

	public RecursiveIterator(T seed, UnaryOperator<T> op) {
		this.seed = seed;
		this.op = op;
	}

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public T next() {
		T next = hasPrevious ? op.apply(previous) : seed;
		previous = next;
		hasPrevious = true;
		return next;
	}
}
