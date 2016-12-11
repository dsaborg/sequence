package org.d2ab.test;

import org.d2ab.collection.longs.LongIterable;

@FunctionalInterface
public interface StrictLongIterable extends LongIterable {
	static LongIterable from(LongIterable iterable) {
		return () -> StrictLongIterator.from(iterable.iterator());
	}

	static LongIterable of(long... values) {
		return () -> StrictLongIterator.of(values);
	}
}
