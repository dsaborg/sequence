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

import org.d2ab.function.chars.CharBinaryOperator;
import org.d2ab.function.chars.CharConsumer;
import org.d2ab.function.chars.IntToCharFunction;
import org.d2ab.function.chars.LongToCharFunction;

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

	static CharIterator from(Iterable<Character> iterable) {
		return from(iterable.iterator());
	}

	static CharIterator from(Iterator<Character> iterator) {
		return new MappedCharIterator<Character, Iterator<Character>>(iterator) {
			@Override
			public char nextChar() {
				return iterator.next();
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
	 */
	default void skip() {
		skip(1);
	}

	/**
	 * Skip the given number of {@code char}s in this iterator.
	 */
	default void skip(long steps) {
		long count = 0;
		while ((count++ < steps) && hasNext()) {
			nextChar();
		}
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
