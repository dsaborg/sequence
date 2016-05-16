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

import org.d2ab.iterator.longs.LongIterator;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

/**
 * A primitive specialization of {@link Collection} for {code long} values. Supplements all {@link Long}-valued
 * methods with corresponding {@code long}-valued methods.
 */
public interface LongCollection extends Collection<Long>, LongIterable {
	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default void clear() {
		iterator().removeAll();
	}

	@Override
	default boolean add(Long l) {
		return addLong(l);
	}

	default boolean addLong(long l) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean contains(Object o) {
		return containsLong((long) o);
	}

	default boolean containsLong(long l) {
		return iterator().contains(l);
	}

	@Override
	default boolean remove(Object o) {
		return removeLong((long) o);
	}

	default boolean removeLong(long l) {
		LongIterator iterator = iterator();
		while (iterator.hasNext())
			if (iterator.nextLong() == l) {
				iterator.remove();
				return true;
			}

		return false;
	}

	/**
	 * Collect the {@code longs} in this {@code LongCollection} into an {@code long}-array.
	 */
	default long[] toLongArray() {
		return iterator().toArray(new long[size()]);
	}

	@Override
	default Long[] toArray() {
		return toArray(new Long[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return Collectionz.toArray(this, a);
	}

	default boolean addAll(LongCollection c) {
		if (c.isEmpty())
			return false;

		c.forEachLong(this::addLong);
		return true;
	}

	default boolean containsAll(LongCollection c) {
		for (long i : c)
			if (!containsLong(i))
				return false;

		return true;
	}

	default boolean removeAll(LongCollection c) {
		return removeLongsIf(c::containsLong);
	}

	default boolean retainAll(LongCollection c) {
		return removeLongsIf(i -> !c.containsLong(i));
	}

	default boolean addAll(long... is) {
		boolean changed = false;
		for (long i : is)
			changed |= addLong(i);
		return changed;
	}

	default boolean containsAll(long... is) {
		for (long i : is)
			if (!containsLong(i))
				return false;

		return true;
	}

	default boolean removeAll(long... is) {
		return removeLongsIf(i -> Arrayz.contains(is, i));
	}

	default boolean retainAll(long... is) {
		return removeLongsIf(i -> !Arrayz.contains(is, i));
	}

	@Override
	default boolean addAll(Collection<? extends Long> c) {
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
	default boolean removeIf(Predicate<? super Long> filter) {
		return removeLongsIf((LongPredicate) filter);
	}

	default boolean removeLongsIf(LongPredicate filter) {
		boolean changed = false;
		for (LongIterator iterator = iterator(); iterator.hasNext(); ) {
			if (filter.test(iterator.nextLong())) {
				iterator.remove();
				changed = true;
			}
		}
		return changed;
	}


	@Override
	default Spliterator.OfLong spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.NONNULL);
	}
}
