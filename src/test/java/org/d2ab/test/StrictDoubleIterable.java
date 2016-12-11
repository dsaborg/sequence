package org.d2ab.test;

import org.d2ab.collection.doubles.DoubleIterable;

@FunctionalInterface
public interface StrictDoubleIterable extends DoubleIterable {
	static DoubleIterable from(DoubleIterable iterable) {
		return () -> StrictDoubleIterator.from(iterable.iterator());
	}

	static DoubleIterable of(double... values) {
		return () -> StrictDoubleIterator.of(values);
	}
}
