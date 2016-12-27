package org.d2ab.test;

import org.d2ab.collection.chars.CharIterable;

import java.util.function.Consumer;

@FunctionalInterface
public interface StrictCharIterable extends CharIterable {
	static StrictCharIterable of(char... values) {
		return () -> StrictCharIterator.of(values);
	}

	static StrictCharIterable from(CharIterable iterable) {
		return () -> StrictCharIterator.from(iterable.iterator());
	}

	@Override
	default void forEach(Consumer<? super Character> consumer) {
		throw new UnsupportedOperationException();
	}
}
