/*
 * Copyright 2016 Daniel Skogquist Åborg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.d2ab.collection.chars;

import org.d2ab.iterator.chars.CharIterator;

import java.util.ListIterator;

/**
 * A {@link ListIterator} over a sequence of {@code char} values.
 */
public interface CharListIterator extends ListIterator<Character>, CharIterator {
	static CharListIterator of(char... values) {
		return new ArrayCharListIterator(values);
	}

	@Override
	boolean hasNext();

	@Override
	char nextChar();

	@Override
	default Character next() {
		return nextChar();
	}

	@Override
	boolean hasPrevious();

	char previousChar();

	@Override
	default Character previous() {
		return previousChar();
	}

	@Override
	int nextIndex();

	@Override
	int previousIndex();

	@Override
	default void remove() {
		throw new UnsupportedOperationException();
	}

	default void set(char x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void set(Character x) {
		set((char) x);
	}

	default void add(char x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void add(Character x) {
		add((char) x);
	}

	static CharListIterator forwardOnly(CharIterator iterator, int index) {
		int skipped = iterator.skip(index);
		if (skipped != index)
			throw new IndexOutOfBoundsException("index: " + index + " size: " + skipped);

		return new CharListIterator() {
			int cursor = index;

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public char nextChar() {
				char nextChar = iterator.nextChar();
				cursor++;
				return nextChar;
			}

			@Override
			public void remove() {
				iterator.remove();
				cursor--;
			}

			@Override
			public boolean hasPrevious() {
				throw new UnsupportedOperationException();
			}

			@Override
			public char previousChar() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int nextIndex() {
				return cursor;
			}

			@Override
			public int previousIndex() {
				return cursor - 1;
			}
		};
	}
}
