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

package org.d2ab.iterator.doubles;

import org.d2ab.util.Doubles;
import org.d2ab.util.Strict;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.*;

/**
 * An Iterator specialized for {@code double} values. Extends {@link PrimitiveIterator.OfDouble} with helper methods.
 */
public interface DoubleIterator extends PrimitiveIterator.OfDouble {
	DoubleIterator EMPTY = new DoubleIterator() {
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public double nextDouble() {
			throw new NoSuchElementException();
		}
	};

	static DoubleIterator empty() {
		return EMPTY;
	}

	static DoubleIterator of(double... doubles) {
		return new ArrayDoubleIterator(doubles);
	}

	static DoubleIterator from(double[] doubles, int size) {
		return new ArrayDoubleIterator(doubles, size);
	}

	static DoubleIterator from(double[] doubles, int offset, int size) {
		return new ArrayDoubleIterator(doubles, offset, size);
	}

	static DoubleIterator from(Iterator<Double> iterator) {
		if (iterator instanceof PrimitiveIterator.OfDouble)
			return from((PrimitiveIterator.OfDouble) iterator);

		return new DelegatingTransformingDoubleIterator<Double, Iterator<Double>>(iterator) {
			@Override
			public double nextDouble() {
				return iterator.next();
			}
		};
	}

	static DoubleIterator from(PrimitiveIterator.OfDouble iterator) {
		if (iterator instanceof DoubleIterator)
			return (DoubleIterator) iterator;

		return new DelegatingTransformingDoubleIterator<Double, OfDouble>(iterator) {
			@Override
			public double nextDouble() {
				return iterator.nextDouble();
			}
		};
	}

	static DoubleIterator from(PrimitiveIterator.OfLong iterator) {
		return new DelegatingTransformingDoubleIterator<Long, OfLong>(iterator) {
			@Override
			public double nextDouble() {
				return iterator.nextLong();
			}
		};
	}

	static DoubleIterator from(PrimitiveIterator.OfLong iterator, LongToDoubleFunction mapper) {
		return new DelegatingTransformingDoubleIterator<Long, OfLong>(iterator) {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextLong());
			}
		};
	}

	static DoubleIterator from(PrimitiveIterator.OfInt iterator) {
		return new DelegatingTransformingDoubleIterator<Integer, OfInt>(iterator) {
			@Override
			public double nextDouble() {
				return iterator.nextInt();
			}
		};
	}

	static DoubleIterator from(PrimitiveIterator.OfInt iterator, IntToDoubleFunction mapper) {
		return new DelegatingTransformingDoubleIterator<Integer, OfInt>(iterator) {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextInt());
			}
		};
	}

	static <T> DoubleIterator from(Iterator<T> iterator,
	                               ToDoubleFunction<? super T> mapper) {
		return new DelegatingTransformingDoubleIterator<T, Iterator<T>>(iterator) {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.next());
			}
		};
	}

	@Override
	default Double next() {
		assert Strict.LENIENT : "DoubleIterator.next()";

		return nextDouble();
	}

	@Override
	default void forEachRemaining(Consumer<? super Double> action) {
		assert Strict.LENIENT : "DoubleIterator.forEachRemaining(Consumer)";

		forEachRemaining((DoubleConsumer) action::accept);
	}

	default boolean skip() {
		return skip(1) == 1;
	}

	default int skip(int steps) {
		int count = 0;
		while (count < steps && hasNext()) {
			nextDouble();
			count++;
		}
		return count;
	}

	/**
	 * @return the number of {@code doubles} remaining in this iterator.
	 */
	default int size() {
		return size(iterator -> {
			long count = 0;
			for (; iterator.hasNext(); iterator.nextDouble())
				count++;
			return count;
		});
	}

	// for testing purposes
	default int size(ToLongFunction<DoubleIterator> counter) {
		double count = counter.applyAsLong(this);

		if (count > Integer.MAX_VALUE)
			throw new IllegalStateException("count > Integer.MAX_VALUE: " + count);

		return (int) count;
	}

	default double reduce(double identity, DoubleBinaryOperator operator) {
		double result = identity;
		while (hasNext())
			result = operator.applyAsDouble(result, nextDouble());
		return result;
	}

	/**
	 * @return true if this {@code DoubleIterator} contains the given {@code double}, false otherwise.
	 */
	default boolean contains(double d, double precision) {
		while (hasNext())
			if (Doubles.eq(nextDouble(), d, precision))
				return true;

		return false;
	}

	default boolean isEmpty() {
		return !hasNext();
	}

	default void removeAll() {
		while (hasNext()) {
			nextDouble();
			remove();
		}
	}
}
