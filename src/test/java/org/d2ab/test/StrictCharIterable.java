package org.d2ab.test;

import org.d2ab.collection.chars.CharIterable;

@FunctionalInterface
public interface StrictCharIterable extends CharIterable {
	static CharIterable from(CharIterable iterable) {
		return () -> StrictCharIterator.from(iterable.iterator());
	}

	static CharIterable of(char... values) {
		return () -> StrictCharIterator.of(values);
	}
}
