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

import org.d2ab.iterator.Iterators;

import java.util.*;

/**
 * A sequential {@link List} view of an {@link Iterable}
 */
public class IterableList<T> extends AbstractSequentialList<T> {
	private Iterable<T> iterable;

	public IterableList(Iterable<T> iterable) {
		this.iterable = iterable;
	}

	/**
	 * Create a {@code List} view of the given {@link Iterable}, which is updated in real time as the
	 * {@link Iterable} changes. If a {@link List} is given it is returned unchanged. The list does not implement
	 * {@link RandomAccess} unless the given {@link Iterable} does, and is best accessed in sequence. The list does
	 * not support modification except removal, by {@link Iterator#remove()} if implemented in the {@link Iterable}.
	 */
	public static <T> List<T> from(Iterable<T> iterable) {
		if (iterable instanceof List)
			return (List<T>) iterable;

		return new IterableList<>(iterable);
	}

	@Override
	public Iterator<T> iterator() {
		return iterable.iterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		Iterator<T> iterator = iterator();
		ListIterator<T> listIterator = new ListIterator<T>() {
			private final List<T> previous = new LinkedList<T>();

			int cursor;

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				cursor++;

				if (cursor < previous.size())
					return previous.get(cursor);

				T next = iterator.next();
				previous.add(next);
				return next;
			}

			@Override
			public boolean hasPrevious() {
				return cursor > 0;
			}

			@Override
			public T previous() {
				return previous.get(--cursor);
			}

			@Override
			public int nextIndex() {
				return cursor;
			}

			@Override
			public int previousIndex() {
				return cursor - 1;
			}

			@Override
			public void remove() {
				iterator.remove();
			}

			@Override
			public void set(T t) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void add(T t) {
				throw new UnsupportedOperationException();
			}
		};
		Iterators.skip(listIterator, index);
		return listIterator;
	}

	@Override
	public int size() {
		return (int) Iterators.count(iterator());
	}
}
