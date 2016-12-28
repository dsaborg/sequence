package org.d2ab.test;

import org.d2ab.iterator.doubles.DoubleIterator;

import java.util.function.Consumer;

public class StrictDoubleIterator implements DoubleIterator {
	private final DoubleIterator iterator;

	public static DoubleIterator of(double... values) {
		return from(DoubleIterator.of(values));
	}

	public static DoubleIterator from(DoubleIterator iterator) {
		return new StrictDoubleIterator(iterator);
	}

	public StrictDoubleIterator(DoubleIterator iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public double nextDouble() {
		return iterator.nextDouble();
	}

	@Override
	public Double next() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove() {
		iterator.remove();
	}

	@Override
	public void forEachRemaining(Consumer<? super Double> consumer) {
		throw new UnsupportedOperationException();
	}
}
