package org.d2ab.test;

import org.d2ab.collection.doubles.DoubleIterable;

import java.util.function.Consumer;

@FunctionalInterface
public interface StrictDoubleIterable extends DoubleIterable {
	static DoubleIterable of(double... values) {
		return () -> StrictDoubleIterator.of(values);
	}

	static DoubleIterable from(DoubleIterable iterable) {
		return () -> StrictDoubleIterator.from(iterable.iterator());
	}

	@Override
	default void forEach(Consumer<? super Double> consumer) {
		throw new UnsupportedOperationException();
	}
}
