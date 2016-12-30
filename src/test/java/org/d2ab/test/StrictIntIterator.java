package org.d2ab.test;

import org.d2ab.iterator.ints.IntIterator;

import java.util.function.Consumer;

public class StrictIntIterator implements IntIterator {
	private final IntIterator iterator;

	public static IntIterator of(int... values) {
		return from(IntIterator.of(values));
	}

	public static IntIterator from(IntIterator iterator) {
		return new StrictIntIterator(iterator);
	}

	public StrictIntIterator(IntIterator iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public int nextInt() {
		return iterator.nextInt();
	}

	@Override
	public Integer next() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove() {
		iterator.remove();
	}

	@Override
	public void forEachRemaining(Consumer<? super Integer> consumer) {
		throw new UnsupportedOperationException();
	}
}
