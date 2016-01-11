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

package org.d2ab.primitive.ints;

import java.util.Iterator;
import java.util.PrimitiveIterator;

/**
 * An Iterator specialized for {@code int} values. Extends {@link PrimitiveIterator.OfInt} with helper methods.
 */
public interface IntIterator extends PrimitiveIterator.OfInt {
	static IntIterator of(int... ints) {
		return new ArrayIntIterator(ints);
	}

	static IntIterator from(Iterable<Integer> iterable) {
		return from(iterable.iterator());
	}

	static IntIterator from(Iterator<Integer> iterator) {
		return new IntIterator() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public int nextInt() {
				return iterator.next();
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

	default IntIterable asIterable() {
		return () -> this;
	}
}
