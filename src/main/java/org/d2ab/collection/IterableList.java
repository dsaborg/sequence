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

import org.d2ab.iterable.Iterables;
import org.d2ab.iterator.Iterators;

import java.util.*;

/**
 * Create a {@link List} view of the given {@link Iterable}, with changes to the underlying {@link Iterable}
 * reflected in the returned {@link List}. If a {@link List} is given it is returned unchanged. The list does not
 * implement {@link RandomAccess} unless the given {@link Iterable} does, and is best accessed in sequence. The
 * list supports removal operations, by using {@link Iterator#remove()} if implemented in the {@link Iterable}'s
 * {@link Iterator}. Add and set operations are supported only if {@link #listIterator(int)} is overridden with
 * a {@link ListIterator} that supports add and set.
 *
 * @since 1.2
 */
@FunctionalInterface
public interface IterableList<T> extends IterableCollection<T>, List<T> {
	static <T> List<T> from(Iterable<T> iterable) {
		if (iterable instanceof List)
			return (List<T>) iterable;

		return (IterableList<T>) iterable::iterator;
	}

	@Override
	default int size() {
		return Iterators.count(iterator());
	}

	@Override
	default boolean isEmpty() {
		return !iterator().hasNext();
	}

	@Override
	default boolean contains(Object o) {
		return Iterators.contains(iterator(), o);
	}

	@Override
	default Object[] toArray() {
		return Iterators.toList(iterator()).toArray();
	}

	@Override
	default <T1> T1[] toArray(T1[] a) {
		return Iterators.toList(iterator()).toArray(a);
	}

	@Override
	default boolean add(T t) {
		listIterator(size()).add(t);
		return true;
	}

	@Override
	default boolean remove(Object o) {
		Iterator<T> iterator = iterator();
		while (iterator.hasNext())
			if (Objects.equals(o, iterator.next())) {
				iterator.remove();
				return true;
			}

		return false;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		for (Object o : c)
			if (!contains(o))
				return false;
		return true;
	}

	@Override
	default boolean addAll(Collection<? extends T> c) {
		if (c.isEmpty())
			return false;

		c.forEach(listIterator()::add);
		return true;
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return removeIf(c::contains);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return removeIf(o -> !c.contains(o));
	}

	@Override
	default void clear() {
		Iterables.removeAll(this);
	}

	@Override
	default boolean addAll(int index, Collection<? extends T> c) {
		if (c.isEmpty())
			return false;

		c.forEach(listIterator(index)::add);
		return true;
	}

	@Override
	default T get(int index) {
		Iterator<T> iterator = iterator();
		if (Iterators.skip(iterator, index) == index && iterator.hasNext())
			return iterator.next();

		throw new IndexOutOfBoundsException();
	}

	@Override
	default T set(int index, T element) {
		ListIterator<T> listIterator = listIterator(index);
		T previous = listIterator.next();
		listIterator.set(element);
		return previous;
	}

	@Override
	default void add(int index, T element) {
		listIterator(index).add(element);
	}

	@Override
	default T remove(int index) {
		Iterator<T> iterator = iterator();
		if (Iterators.skip(iterator, index) == index && iterator.hasNext()) {
			T next = iterator.next();
			iterator.remove();
			return next;
		}

		throw new IndexOutOfBoundsException();
	}

	@Override
	default int indexOf(Object o) {
		int index = 0;
		for (T each : this) {
			if (Objects.equals(o, each))
				return index;
			index++;
		}
		return -1;
	}

	@Override
	default int lastIndexOf(Object o) {
		int lastIndex = -1;
		int index = 0;
		for (T each : this) {
			if (Objects.equals(o, each))
				lastIndex = index;
			index++;
		}
		return lastIndex;
	}

	default ListIterator<T> listIterator() {
		return listIterator(0);
	}

	default ListIterator<T> listIterator(int index) {
		Iterator<T> iterator = iterator();
		ListIterator<T> listIterator = new ListIterator<T>() {
			private final List<T> previous = new ArrayList<>();

			int cursor;

			@Override
			public boolean hasNext() {
				return cursor < previous.size() || iterator.hasNext();
			}

			@Override
			public T next() {
				if (cursor < previous.size())
					return previous.get(cursor++);

				cursor++;

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
				if (cursor < previous.size())
					throw new IllegalStateException("Cannot remove after previous");
				iterator.remove();
				previous.remove(--cursor);
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
	default List<T> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}
}
