package org.d2ab.test;

import org.d2ab.collection.ints.IntIterable;

import java.util.function.Consumer;

@FunctionalInterface
public interface StrictIntIterable extends IntIterable {
	static StrictIntIterable of(int... values) {
		return () -> StrictIntIterator.of(values);
	}

	static StrictIntIterable from(IntIterable iterable) {
		return () -> StrictIntIterator.from(iterable.iterator());
	}

	@Override
	default void forEach(Consumer<? super Integer> consumer) {
		throw new UnsupportedOperationException();
	}
}
