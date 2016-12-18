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

package org.d2ab.iterator.longs;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.DoubleToLongFunction;
import java.util.function.IntToLongFunction;
import java.util.function.LongBinaryOperator;
import java.util.function.ToLongFunction;

/**
 * An Iterator specialized for {@code long} values. Extends {@link OfLong} with helper methods.
 */
public interface LongIterator extends PrimitiveIterator.OfLong {
	LongIterator EMPTY = new LongIterator() {
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public long nextLong() {
			throw new NoSuchElementException();
		}
	};

	static LongIterator of(long... longs) {
		return new ArrayLongIterator(longs);
	}

	static LongIterator from(long[] longs, int size) {
		return new ArrayLongIterator(longs, size);
	}

	static LongIterator from(long[] longs, int offset, int size) {
		return new ArrayLongIterator(longs, offset, size);
	}

	static LongIterator from(PrimitiveIterator.OfLong iterator) {
		if (iterator instanceof LongIterator)
			return (LongIterator) iterator;

		return new DelegatingTransformingLongIterator<Long, OfLong>(iterator) {
			@Override
			public long nextLong() {
				return iterator.nextLong();
			}
		};
	}

	static LongIterator from(Iterator<Long> iterator) {
		if (iterator instanceof PrimitiveIterator.OfLong)
			return from((PrimitiveIterator.OfLong) iterator);

		return new DelegatingTransformingLongIterator<Long, Iterator<Long>>(iterator) {
			@Override
			public long nextLong() {
				return iterator.next();
			}
		};
	}

	static LongIterator from(PrimitiveIterator.OfDouble iterator) {
		return new DelegatingTransformingLongIterator<Double, OfDouble>(iterator) {
			@Override
			public long nextLong() {
				return (long) iterator.nextDouble();
			}
		};
	}

	static LongIterator from(PrimitiveIterator.OfDouble iterator, DoubleToLongFunction mapper) {
		return new DelegatingTransformingLongIterator<Double, OfDouble>(iterator) {
			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.nextDouble());
			}
		};
	}

	static LongIterator from(PrimitiveIterator.OfInt iterator) {
		return new DelegatingTransformingLongIterator<Integer, OfInt>(iterator) {
			@Override
			public long nextLong() {
				return iterator.nextInt();
			}
		};
	}

	static LongIterator from(PrimitiveIterator.OfInt iterator, IntToLongFunction mapper) {
		return new DelegatingTransformingLongIterator<Integer, OfInt>(iterator) {
			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.nextInt());
			}
		};
	}

	static <T> LongIterator from(Iterator<T> iterator, ToLongFunction<? super T> mapper) {
		return new DelegatingTransformingLongIterator<T, Iterator<T>>(iterator) {
			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.next());
			}
		};
	}

	default boolean skip() {
		return skip(1) == 1;
	}

	default int skip(int steps) {
		int count = 0;
		while (count < steps && hasNext()) {
			nextLong();
			count++;
		}
		return count;
	}

	default long reduce(long identity, LongBinaryOperator operator) {
		long result = identity;
		while (hasNext())
			result = operator.applyAsLong(result, nextLong());
		return result;
	}

	/**
	 * @return true if this {@code LongIterator} contains the given {@code long}, false otherwise.
	 */
	default boolean contains(long l) {
		while (hasNext())
			if (nextLong() == l)
				return true;
		return false;
	}

	/**
	 * @return the number of {@code longs} remaining in this iterator.
	 */
	default int count() {
		long count = 0;
		for (; hasNext(); nextLong())
			count++;

		if (count > Integer.MAX_VALUE)
			throw new IllegalStateException("count > Integer.MAX_VALUE: " + count);

		return (int) count;
	}

	default boolean isEmpty() {
		return !hasNext();
	}

	default void removeAll() {
		while (hasNext()) {
			nextLong();
			remove();
		}
	}
}
