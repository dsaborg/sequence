package org.da2ab.sequence;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class TerminalIterator<T> implements Iterator<T> {
	private final Iterator<T> iterator;
	private final T terminal;
	private T next;
	private boolean gotNext;

	public TerminalIterator(Iterator<T> iterator, T terminal) {
		this.iterator = iterator;
		this.terminal = terminal;
	}

	@Override
	public boolean hasNext() {
		if (!gotNext && iterator.hasNext()) {
			next = iterator.next();
			gotNext = true;
		}
		return gotNext && !Objects.equals(next, terminal);
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
