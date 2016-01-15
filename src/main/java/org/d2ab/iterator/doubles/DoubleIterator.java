/*
 * Copyright 2015 Daniel Skogquist Åborg
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

import org.d2ab.iterable.doubles.DoubleIterable;

import java.util.Iterator;
import java.util.PrimitiveIterator;

/**
 * An Iterator specialized for {@code long} values. Extends {@link OfLong} with helper methods.
 */
public interface DoubleIterator extends PrimitiveIterator.OfDouble {
	static DoubleIterator of(double... doubles) {
		return new ArrayDoubleIterator(doubles);
	}

	static DoubleIterator from(Iterable<Double> iterable) {
		return from(iterable.iterator());
	}

	static DoubleIterator from(Iterator<Double> iterator) {
		return new DoubleIterator() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public double nextDouble() {
				return iterator.next();
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

	default DoubleIterable asIterable() {
		return () -> this;
	}
}
