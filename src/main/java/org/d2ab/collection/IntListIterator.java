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

package org.d2ab.collection;

import org.d2ab.iterator.ints.IntIterator;

import java.util.ListIterator;

/**
 * A {@link ListIterator} over a sequence of {@code int} values.
 */
public interface IntListIterator extends ListIterator<Integer>, IntIterator {
	@Override
	boolean hasNext();

	@Override
	int nextInt();

	@Override
	default Integer next() {
		return nextInt();
	}

	@Override
	boolean hasPrevious();

	int previousInt();

	@Override
	default Integer previous() {
		return previousInt();
	}

	@Override
	int nextIndex();

	@Override
	int previousIndex();

	@Override
	default void remove() {
		throw new UnsupportedOperationException();
	}

	default void set(int i) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void set(Integer integer) {
		set((int) integer);
	}

	default void add(int i) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void add(Integer integer) {
		add((int) integer);
	}

	static IntListIterator forwardOnly(IntIterator iterator, int index) {
		iterator.skip(index);
		return new IntListIterator() {
			int cursor = index;

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public int nextInt() {
				int nextInt = iterator.nextInt();
				cursor++;
				return nextInt;
			}

			@Override
			public boolean hasPrevious() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int previousInt() {
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
