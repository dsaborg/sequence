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

import org.d2ab.iterable.ints.IntIterable;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.util.Arrayz;

import java.util.Arrays;
import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * A {@link Collection} of {code int} values. Supplements all {@link Integer}-valued methods with corresponding
 * {@code int}-valued methods.
 */
public interface IntCollection extends Collection<Integer>, IntIterable {
	default void clear() {
		iterator().removeAll();
	}

	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default Integer[] toArray() {
		return toArray(this);
	}

	@Override
	@Deprecated
	default <T> T[] toArray(T[] a) {
		return toArray(this, a);
	}

	static Integer[] toArray(Collection<? extends Integer> collection) {
		Integer[] array = new Integer[collection.size()];

		int index = 0;
		for (Integer i : collection)
			array[index++] = i;

		return array;
	}

	@SuppressWarnings("unchecked")
	static <T> T[] toArray(Collection<? extends Integer> collection, T[] a) {
		int size = collection.size();
		if (a.length < size)
			a = Arrays.copyOf(a, size);

		int index = 0;
		for (Integer i : collection)
			a[index++] = (T) i;

		if (a.length > size)
			a[size] = null;

		return a;
	}

	/**
	 * Collect the {@code ints} in this {@code IntCollection} into an {@code int}-array.
	 */
	default int[] toIntArray() {
		int[] array = new int[size()];

		int index = 0;
		for (IntIterator iterator = iterator(); iterator.hasNext(); )
			array[index++] = iterator.nextInt();

		return array;
	}

	@Override
	default boolean remove(Object o) {
		return removeInt((int) o);
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean removeIf(Predicate<? super Integer> filter) {
		return removeIntsIf((IntPredicate) filter);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean add(Integer integer) {
		return addInt(integer);
	}

	@Override
	default boolean addAll(Collection<? extends Integer> c) {
		if (c.isEmpty())
			return false;

		c.forEach(this::addInt);
		return true;
	}

	default boolean addInt(int i) {
		throw new UnsupportedOperationException();
	}

	default boolean addAll(int... is) {
		throw new UnsupportedOperationException();
	}

	default boolean removeInt(int i) {
		for (IntIterator iterator = iterator(); iterator.hasNext(); )
			if (i == iterator.nextInt()) {
				iterator.remove();
				return true;
			}

		return false;
	}

	@Override
	default boolean contains(Object o) {
		return containsInt((int) o);
	}

	default boolean containsInt(int i) {
		IntIterator iterator = iterator();
		while (iterator.hasNext())
			if (iterator.nextInt() == i)
				return true;

		return false;
	}

	default boolean containsAll(int... is) {
		for (int i : is)
			if (!containsInt(i))
				return false;

		return true;
	}

	default boolean removeAll(int... is) {
		return removeIntsIf(i -> Arrayz.contains(is, i));
	}

	default boolean retainAll(int... is) {
		return removeIntsIf(i -> !Arrayz.contains(is, i));
	}

	default boolean removeIntsIf(IntPredicate filter) {
		boolean changed = false;
		for (IntIterator iterator = iterator(); iterator.hasNext(); ) {
			if (filter.test(iterator.nextInt())) {
				iterator.remove();
				changed = true;
			}
		}
		return changed;
	}


	@Override
	default Spliterator.OfInt spliterator() {
		return Spliterators.spliterator(iterator(), size(), 0);
	}
}
