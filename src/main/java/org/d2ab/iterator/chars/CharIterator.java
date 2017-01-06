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

import org.d2ab.function.*;
import org.d2ab.util.Strict;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

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

	static CharIterator empty() {
		return EMPTY;
	}

	static CharIterator of(char... chars) {
		return new ArrayCharIterator(chars);
	}

	static CharIterator from(char[] chars, int size) {
		return new ArrayCharIterator(chars, size);
	}

	static CharIterator from(char[] chars, int offset, int size) {
		return new ArrayCharIterator(chars, offset, size);
	}

	static CharIterator from(Iterator<Character> iterator) {
		if (iterator instanceof CharIterator)
			return (CharIterator) iterator;

		return new DelegatingTransformingCharIterator<Character, Iterator<Character>>(iterator) {
			@Override
			public char nextChar() {
				return iterator.next();
			}
		};
	}

	static <T> CharIterator from(Iterator<T> iterator, ToCharFunction<? super T> mapper) {
		return new DelegatingTransformingCharIterator<T, Iterator<T>>(iterator) {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.next());
			}
		};
	}

	static CharIterator from(PrimitiveIterator.OfInt iterator) {
		return new DelegatingTransformingCharIterator<Integer, OfInt>(iterator) {
			@Override
			public char nextChar() {
				return (char) iterator.nextInt();
			}
		};
	}

	static CharIterator from(PrimitiveIterator.OfInt iterator, IntToCharFunction mapper) {
		return new DelegatingTransformingCharIterator<Integer, OfInt>(iterator) {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.nextInt());
			}
		};
	}

	static CharIterator from(PrimitiveIterator.OfLong iterator) {
		return new DelegatingTransformingCharIterator<Long, OfLong>(iterator) {
			@Override
			public char nextChar() {
				return (char) iterator.nextLong();
			}
		};
	}

	static CharIterator from(PrimitiveIterator.OfLong iterator, LongToCharFunction mapper) {
		return new DelegatingTransformingCharIterator<Long, OfLong>(iterator) {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.nextLong());
			}
		};
	}

	/**
	 * Return the next {@code char} in this iterator.
	 */
	char nextChar();

	/**
	 * Return the next {@code char} boxed into a {@link Character}.
	 */
	@Override
	default Character next() {
		assert Strict.LENIENT : "CharIterator.next()";

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
		assert Strict.LENIENT : "CharIterator.forEachRemaining(Consumer)";

		forEachRemaining((CharConsumer) consumer::accept);
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
	default int skip(int steps) {
		int count = 0;
		while (count < steps && hasNext()) {
			nextChar();
			count++;
		}
		return count;
	}

	/**
	 * @return the number of {@code chars} remaining in this iterator.
	 */
	default int size() {
		return size(iterator -> {
			long count = 0;
			for (; iterator.hasNext(); iterator.nextChar())
				count++;
			return count;
		});
	}

	// for testing purposes
	default int size(ToLongFunction<CharIterator> counter) {
		long count = counter.applyAsLong(this);

		if (count > Integer.MAX_VALUE)
			throw new IllegalStateException("count > Integer.MAX_VALUE: " + count);

		return (int) count;
	}

	default boolean isEmpty() {
		return !hasNext();
	}

	default void removeAll() {
		while (hasNext()) {
			nextChar();
			remove();
		}
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
	 * Reduce this {@code CharIterator} into a single element by iteratively applying the given binary operator to
	 * the current result and each element in the iterator, starting with the given identity as the initial result.
	 */
	default char reduce(char identity, CharBinaryOperator operator) {
		char result = identity;
		while (hasNext())
			result = operator.applyAsChar(result, nextChar());
		return result;
	}
}
