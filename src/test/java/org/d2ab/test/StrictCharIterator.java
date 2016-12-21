package org.d2ab.test;

import org.d2ab.iterator.chars.CharIterator;

public interface StrictCharIterator extends CharIterator {
	static CharIterator of(char... values) {
		return from(CharIterator.of(values));
	}

	static CharIterator from(CharIterator iterator) {
		return new CharIterator() {
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
		};
	}
}
