package org.d2ab.test;

import org.d2ab.iterator.chars.CharIterator;

import java.util.function.Consumer;

public class StrictCharIterator implements CharIterator {
	private CharIterator iterator;

	public static CharIterator of(char... values) {
		return from(CharIterator.of(values));
	}

	public static CharIterator from(CharIterator iterator) {
		return new StrictCharIterator(iterator);
	}

	public StrictCharIterator(CharIterator iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public char nextChar() {
		return iterator.nextChar();
	}

	@Override
	public Character next() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove() {
		iterator.remove();
	}

	@Override
	public void forEachRemaining(Consumer<? super Character> consumer) {
		throw new UnsupportedOperationException();
	}
}
