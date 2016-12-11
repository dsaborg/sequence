package org.d2ab.test;

import org.d2ab.iterator.doubles.DoubleIterator;

public interface StrictDoubleIterator extends DoubleIterator {
	static DoubleIterator from(DoubleIterator iterator) {
		return new DoubleIterator() {
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
		};
	}

	static DoubleIterator of(double... values) {
		return from(DoubleIterator.of(values));
	}
}
