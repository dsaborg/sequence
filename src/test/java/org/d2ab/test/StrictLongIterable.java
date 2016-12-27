package org.d2ab.test;

import org.d2ab.collection.longs.LongIterable;

import java.util.function.Consumer;

@FunctionalInterface
public interface StrictLongIterable extends LongIterable {
	static StrictLongIterable of(long... values) {
		return () -> StrictLongIterator.of(values);
	}

	static StrictLongIterable from(LongIterable iterable) {
		return () -> StrictLongIterator.from(iterable.iterator());
	}

	@Override
	default void forEach(Consumer<? super Long> consumer) {
		throw new UnsupportedOperationException();
	}
}
