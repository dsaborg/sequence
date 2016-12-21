package org.d2ab.test;

import org.d2ab.collection.chars.CharIterable;

import java.util.function.Consumer;

@FunctionalInterface
public interface StrictCharIterable extends CharIterable {
	static CharIterable of(char... values) {
		return () -> StrictCharIterator.of(values);
	}

	static CharIterable from(CharIterable iterable) {
		return () -> StrictCharIterator.from(iterable.iterator());
	}

	@Override
	default void forEach(Consumer<? super Character> consumer) {
		throw new UnsupportedOperationException();
	}
}
