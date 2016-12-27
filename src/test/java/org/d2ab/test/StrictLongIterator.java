package org.d2ab.test;

import org.d2ab.iterator.longs.LongIterator;

import java.util.function.Consumer;

public class StrictLongIterator implements LongIterator {
	private final LongIterator iterator;

	public static LongIterator of(long... values) {
		return from(LongIterator.of(values));
	}

	public static LongIterator from(LongIterator iterator) {
		return new StrictLongIterator(iterator);
	}

	public StrictLongIterator(LongIterator iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public long nextLong() {
		return iterator.nextLong();
	}

	@Override
	public Long next() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove() {
		iterator.remove();
	}

	@Override
	public void forEachRemaining(Consumer<? super Long> action) {
		throw new UnsupportedOperationException();
	}
}
