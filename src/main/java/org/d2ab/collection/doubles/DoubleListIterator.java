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

package org.d2ab.collection.doubles;

import org.d2ab.iterator.doubles.DoubleIterator;

import java.util.ListIterator;

/**
 * A {@link ListIterator} over a sequence of {@code double} values.
 */
public interface DoubleListIterator extends ListIterator<Double>, DoubleIterator {
	@Override
	boolean hasNext();

	@Override
	double nextDouble();

	@Override
	default Double next() {
		return nextDouble();
	}

	@Override
	boolean hasPrevious();

	double previousDouble();

	@Override
	default Double previous() {
		return previousDouble();
	}

	@Override
	int nextIndex();

	@Override
	int previousIndex();

	@Override
	default void remove() {
		throw new UnsupportedOperationException();
	}

	default void set(double x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void set(Double x) {
		set((double) x);
	}

	default void add(double x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void add(Double x) {
		add((double) x);
	}

	static DoubleListIterator forwardOnly(DoubleIterator iterator, int index) {
		iterator.skip(index);
		return new DoubleListIterator() {
			int cursor = index;

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public double nextDouble() {
				double nextDouble = iterator.nextDouble();
				cursor++;
				return nextDouble;
			}

			@Override
			public void remove() {
				iterator.remove();
				cursor--;
			}

			@Override
			public boolean hasPrevious() {
				throw new UnsupportedOperationException();
			}

			@Override
			public double previousDouble() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int nextIndex() {
				return cursor;
			}

			@Override
			public int previousIndex() {
				return cursor - 1;
			}
		};
	}
}
