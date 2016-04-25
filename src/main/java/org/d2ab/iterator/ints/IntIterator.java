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

package org.d2ab.iterator.ints;

import org.d2ab.function.ints.CharToIntFunction;
import org.d2ab.iterator.chars.CharIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.DoubleToIntFunction;
import java.util.function.IntBinaryOperator;
import java.util.function.LongToIntFunction;
import java.util.function.ToIntFunction;

/**
 * An Iterator specialized for {@code int} values. Extends {@link PrimitiveIterator.OfInt} with helper methods.
 */
public interface IntIterator extends PrimitiveIterator.OfInt {
	IntIterator EMPTY = new IntIterator() {
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public int nextInt() {
			throw new NoSuchElementException();
		}
	};

	static IntIterator of(int... ints) {
		return new ArrayIntIterator(ints);
	}

	static IntIterator from(Iterator<Integer> iterator) {
		if (iterator instanceof PrimitiveIterator.OfInt)
			return from((PrimitiveIterator.OfInt) iterator);

		return new MappedIntIterator<Integer, Iterator<Integer>>(iterator) {
			@Override
			public int nextInt() {
				return iterator.next();
			}
		};
	}

	static IntIterator from(PrimitiveIterator.OfInt iterator) {
		if (iterator instanceof IntIterator)
			return (IntIterator) iterator;

		return new MappedIntIterator<Integer, OfInt>(iterator) {
			@Override
			public int nextInt() {
				return iterator.nextInt();
			}
		};
	}

	static IntIterator from(PrimitiveIterator.OfDouble iterator) {
		return new MappedIntIterator<Double, OfDouble>(iterator) {
			@Override
			public int nextInt() {
				return (int) iterator.nextDouble();
			}
		};
	}

	static IntIterator from(PrimitiveIterator.OfDouble iterator, DoubleToIntFunction mapper) {
		return new MappedIntIterator<Double, OfDouble>(iterator) {
			@Override
			public int nextInt() {
				return mapper.applyAsInt(iterator.nextDouble());
			}
		};
	}

	static IntIterator from(CharIterator iterator) {
		return new MappedIntIterator<Character, CharIterator>(iterator) {
			@Override
			public int nextInt() {
				return iterator.nextChar();
			}
		};
	}

	static IntIterator from(CharIterator iterator, CharToIntFunction mapper) {
		return new MappedIntIterator<Character, CharIterator>(iterator) {
			@Override
			public int nextInt() {
				return mapper.applyAsInt(iterator.nextChar());
			}
		};
	}

	static IntIterator from(PrimitiveIterator.OfLong iterator) {
		return new MappedIntIterator<Long, OfLong>(iterator) {
			@Override
			public int nextInt() {
				return (int) iterator.nextLong();
			}
		};
	}

	static IntIterator from(PrimitiveIterator.OfLong iterator, LongToIntFunction mapper) {
		return new MappedIntIterator<Long, OfLong>(iterator) {
			@Override
			public int nextInt() {
				return mapper.applyAsInt(iterator.nextLong());
			}
		};
	}

	static <T> IntIterator from(final Iterator<T> iterator, final ToIntFunction<? super T> mapper) {
		return new MappedIntIterator<T, Iterator<T>>(iterator) {
			@Override
			public int nextInt() {
				return mapper.applyAsInt(iterator.next());
			}
		};
	}

	default void skip() {
		skip(1);
	}

	default void skip(long steps) {
		long count = 0;
		while ((count++ < steps) && hasNext()) {
			nextInt();
		}
	}

	default int reduce(int identity, IntBinaryOperator operator) {
		int result = identity;
		while (hasNext())
			result = operator.applyAsInt(result, nextInt());
		return result;
	}
}
