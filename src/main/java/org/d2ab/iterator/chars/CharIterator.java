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

package org.d2ab.iterator.chars;

import org.d2ab.function.chars.*;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;

/**
 * An Iterator specialized for {@code char} values. Adapted from {@link PrimitiveIterator}.
 */
public interface CharIterator extends PrimitiveIterator<Character, CharConsumer> {
	CharIterator EMPTY = new CharIterator() {
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public char nextChar() {
			throw new NoSuchElementException();
		}
	};

	/**
	 * Return the next {@code char} in this iterator.
	 */
	char nextChar();

	/**
	 * Return the next {@code char} boxed into a {@link Character}.
	 */
	@Override
	default Character next() {
		return nextChar();
	}

	/**
	 * Perform the given action once for each remaining {@code char} in this iterator.
	 */
	@Override
	default void forEachRemaining(CharConsumer consumer) {
		while (hasNext())
			consumer.accept(nextChar());
	}

	/**
	 * Perform the given action once for each remaining {@code char} in this iterator.
	 */
	@Override
	default void forEachRemaining(Consumer<? super Character> consumer) {
		forEachRemaining((consumer instanceof CharConsumer) ? (CharConsumer) consumer : consumer::accept);
	}

	static CharIterator of(char... chars) {
		return new ArrayCharIterator(chars);
	}

	static CharIterator from(Iterator<Character> iterator) {
		return new MappedCharIterator<Character, Iterator<Character>>(iterator) {
			@Override
			public char nextChar() {
				return iterator.next();
			}
		};
	}

	static <T> CharIterator from(final Iterator<T> iterator, final ToCharFunction<? super T> mapper) {
		return new MappedCharIterator<T, Iterator<T>>(iterator) {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.next());
			}
		};
	}

	static CharIterator from(PrimitiveIterator.OfInt iterator) {
		return new MappedCharIterator<Integer, OfInt>(iterator) {
			@Override
			public char nextChar() {
				return (char) iterator.nextInt();
			}
		};
	}

	static CharIterator from(PrimitiveIterator.OfInt iterator, IntToCharFunction mapper) {
		return new MappedCharIterator<Integer, OfInt>(iterator) {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.nextInt());
			}
		};
	}

	static CharIterator from(PrimitiveIterator.OfLong iterator) {
		return new MappedCharIterator<Long, OfLong>(iterator) {
			@Override
			public char nextChar() {
				return (char) iterator.nextLong();
			}
		};
	}

	static CharIterator from(PrimitiveIterator.OfLong iterator, LongToCharFunction mapper) {
		return new MappedCharIterator<Long, OfLong>(iterator) {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.nextLong());
			}
		};
	}

	/**
	 * Skip one {@code char} in this iterator.
	 *
	 * @return true if there was a character left to skip, false if the end of the iterator has been hit.
	 */
	default boolean skip() {
		return skip(1) == 1;
	}

	/**
	 * Skip the given number of {@code char}s in this iterator.
	 *
	 * @return the number of steps actually skipped, may be less if end of iterator was hit.
	 */
	default long skip(long steps) {
		long count = 0;
		while (count < steps && hasNext()) {
			nextChar();
			count++;
		}
		return count;
	}

	/**
	 * Reduce this {@code CharIterator} into a single element by iteratively applying the given binary operator to
	 * the current result and each element in the iterator, starting with the given identity as the initial result.
	 */
	default char reduce(char identity, CharBinaryOperator operator) {
		char result = identity;
		while (hasNext())
			result = operator.applyAsChar(result, nextChar());
		return result;
	}

	/**
	 * @return true if this {@code CharIterator} contains the given {@code char}, false otherwise.
	 */
	default boolean contains(char c) {
		while (hasNext())
			if (nextChar() == c)
				return true;
		return false;
	}

	/**
	 * @return the number of {@code chars} remaining in this iterator.
	 */
	default long count() {
		long count = 0;
		for (; hasNext(); nextChar())
			count++;
		return count;
	}
}
