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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntToDoubleFunction;
import java.util.function.LongToDoubleFunction;
import java.util.function.ToDoubleFunction;

/**
 * An Iterator specialized for {@code long} values. Extends {@link OfLong} with helper methods.
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

	static DoubleIterator of(double... doubles) {
		return new ArrayDoubleIterator(doubles);
	}

	static DoubleIterator from(Iterator<Double> iterator) {
		if (iterator instanceof PrimitiveIterator.OfDouble)
			return from((PrimitiveIterator.OfDouble) iterator);

		return new MappedDoubleIterator<Double, Iterator<Double>>(iterator) {
			@Override
			public double nextDouble() {
				return iterator.next();
			}
		};
	}

	static DoubleIterator from(PrimitiveIterator.OfDouble iterator) {
		if (iterator instanceof DoubleIterator)
			return (DoubleIterator) iterator;

		return new MappedDoubleIterator<Double, OfDouble>(iterator) {
			@Override
			public double nextDouble() {
				return iterator.nextDouble();
			}
		};
	}

	static DoubleIterator from(PrimitiveIterator.OfLong iterator) {
		return new MappedDoubleIterator<Long, OfLong>(iterator) {
			@Override
			public double nextDouble() {
				return iterator.nextLong();
			}
		};
	}

	static DoubleIterator from(PrimitiveIterator.OfLong iterator, LongToDoubleFunction mapper) {
		return new MappedDoubleIterator<Long, OfLong>(iterator) {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextLong());
			}
		};
	}

	static DoubleIterator from(PrimitiveIterator.OfInt iterator) {
		return new MappedDoubleIterator<Integer, OfInt>(iterator) {
			@Override
			public double nextDouble() {
				return iterator.nextInt();
			}
		};
	}

	static DoubleIterator from(PrimitiveIterator.OfInt iterator, IntToDoubleFunction mapper) {
		return new MappedDoubleIterator<Integer, OfInt>(iterator) {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextInt());
			}
		};
	}

	static <T> DoubleIterator from(final Iterator<T> iterator,
	                               final ToDoubleFunction<? super T> mapper) {
		return new MappedDoubleIterator<T, Iterator<T>>(iterator) {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.next());
			}
		};
	}

	default void skip() {
		skip(1);
	}

	default void skip(double steps) {
		double count = 0;
		while ((count++ < steps) && hasNext()) {
			nextDouble();
		}
	}

	default double reduce(double identity, DoubleBinaryOperator operator) {
		double result = identity;
		while (hasNext())
			result = operator.applyAsDouble(result, nextDouble());
		return result;
	}
}
