package org.d2ab.test;

import org.d2ab.collection.ints.IntIterable;

@FunctionalInterface
public interface StrictIntIterable extends IntIterable {
	static IntIterable from(IntIterable iterable) {
		return () -> StrictIntIterator.from(iterable.iterator());
	}

	static IntIterable of(int... values) {
		return () -> StrictIntIterator.of(values);
	}
}
