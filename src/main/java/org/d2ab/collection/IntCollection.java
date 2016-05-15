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

import org.d2ab.collection.iterator.IntIterator;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * A primitive specialization of {@link Collection} for {code int} values. Supplements all {@link Integer}-valued
 * methods with corresponding {@code int}-valued methods.
 */
public interface IntCollection extends Collection<Integer>, IntIterable {
	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default void clear() {
		iterator().removeAll();
	}

	@Override
	default boolean add(Integer integer) {
		return addInt(integer);
	}

	default boolean addInt(int i) {
		throw new UnsupportedOperationException();
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

	@Override
	default boolean remove(Object o) {
		return removeInt((int) o);
	}

	default boolean removeInt(int i) {
		for (IntIterator iterator = iterator(); iterator.hasNext(); )
			if (i == iterator.nextInt()) {
				iterator.remove();
				return true;
			}

		return false;
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
	default Integer[] toArray() {
		return Collectionz.toBoxedIntegerArray(this);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return Collectionz.toArray(this, a);
	}

	default boolean addAll(IntCollection c) {
		return Collectionz.addAll(this, c);
	}

	default boolean containsAll(IntCollection c) {
		return Collectionz.containsAll(this, c);
	}

	default boolean removeAll(IntCollection c) {
		return Collectionz.removeAll(this, c);
	}

	default boolean retainAll(IntCollection c) {
		return Collectionz.retainAll(this, c);
	}

	default boolean addAll(int... is) {
		boolean changed = false;
		for (int i : is)
			changed |= addInt(i);
		return changed;
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

	@Override
	default boolean addAll(Collection<? extends Integer> c) {
		return Collectionz.addAll(this, c);
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return Collectionz.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return Collectionz.removeAll(this, c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return Collectionz.retainAll(this, c);
	}

	@Override
	default boolean removeIf(Predicate<? super Integer> filter) {
		return removeIntsIf((IntPredicate) filter);
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
