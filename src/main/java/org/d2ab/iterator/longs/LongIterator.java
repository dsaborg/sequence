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

import org.d2ab.iterable.longs.LongIterable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.DoubleToLongFunction;
import java.util.function.IntToLongFunction;
import java.util.function.LongBinaryOperator;

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

	static LongIterator from(Iterable<Long> iterable) {
		return from(iterable.iterator());
	}

	static LongIterator from(Iterator<Long> iterator) {
		return new DelegatingLongIterator<Long, Iterator<Long>>(iterator) {
			@Override
			public long nextLong() {
				return iterator.next();
			}
		};
	}

	static LongIterator from(PrimitiveIterator.OfDouble iterator) {
		return new DelegatingLongIterator<Double, PrimitiveIterator.OfDouble>(iterator) {
			@Override
			public long nextLong() {
				return (long) iterator.nextDouble();
			}
		};
	}

	static LongIterator from(PrimitiveIterator.OfDouble iterator, DoubleToLongFunction mapper) {
		return new DelegatingLongIterator<Double, PrimitiveIterator.OfDouble>(iterator) {
			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.nextDouble());
			}
		};
	}

	static LongIterator from(PrimitiveIterator.OfInt iterator) {
		return new DelegatingLongIterator<Integer, PrimitiveIterator.OfInt>(iterator) {
			@Override
			public long nextLong() {
				return iterator.nextInt();
			}
		};
	}

	static LongIterator from(PrimitiveIterator.OfInt iterator, IntToLongFunction mapper) {
		return new DelegatingLongIterator<Integer, PrimitiveIterator.OfInt>(iterator) {
			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.nextInt());
			}
		};
	}

	default void skip() {
		skip(1);
	}

	default void skip(long steps) {
		long count = 0;
		while ((count++ < steps) && hasNext())
			nextLong();
	}

	default LongIterable asIterable() {
		return () -> this;
	}

	default long reduce(long identity, LongBinaryOperator operator) {
		long result = identity;
		while (hasNext())
			result = operator.applyAsLong(result, nextLong());
		return result;
	}
}
