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

package org.d2ab.collection.longs;

import org.d2ab.collection.Collectionz;
import org.d2ab.iterator.longs.LongIterator;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;

/**
 * A primitive specialization of {@link Collection} for {code long} values. Supplements all {@link Long}-valued
 * methods with corresponding {@code long}-valued methods.
 */
public interface LongCollection extends Collection<Long>, LongIterable {
	// TODO: Extract out relevant parts to IterableLongCollection

	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default void clear() {
		iterator().removeAll();
	}

	@Override
	default Long[] toArray() {
		return toArray(new Long[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return Collectionz.toArray(this, a);
	}

	/**
	 * Collect the {@code longs} in this {@code LongCollection} into an {@code long}-array.
	 */
	default long[] toLongArray() {
		return new ArrayLongList(this).toLongArray();
	}

	@Override
	default boolean add(Long x) {
		return addLong(x);
	}

	default boolean addLong(long x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean contains(Object o) {
		return o instanceof Long && containsLong((long) o);
	}

	@Override
	default boolean remove(Object o) {
		return o instanceof Long && removeLong((long) o);
	}

	@Override
	default boolean addAll(Collection<? extends Long> c) {
		return Collectionz.addAll(this, c);
	}

	default boolean addAllLongs(long... xs) {
		boolean changed = false;
		for (long x : xs)
			changed |= addLong(x);
		return changed;
	}

	default boolean addAllLongs(LongCollection c) {
		if (c.isEmpty())
			return false;

		c.forEachLong(this::addLong);
		return true;
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
		return removeLongsIf(filter::test);
	}

	@Override
	default Spliterator.OfLong spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.NONNULL);
	}

	/**
	 * Base class for {@link LongCollection} implementations.
	 */
	abstract class Base implements LongCollection {
		public static LongCollection create(long... longs) {
			return create(LongList.create(longs));
		}

		public static LongCollection create(final LongCollection collection) {
			return new LongCollection.Base() {
				@Override
				public LongIterator iterator() {
					return collection.iterator();
				}

				@Override
				public int size() {
					return collection.size();
				}

				@Override
				public boolean addLong(long x) {
					return collection.addLong(x);
				}
			};
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder(size() * 5); // heuristic
			builder.append("[");

			boolean tail = false;
			for (LongIterator iterator = iterator(); iterator.hasNext(); ) {
				if (tail)
					builder.append(", ");
				else
					tail = true;
				builder.append(iterator.nextLong());
			}

			builder.append("]");
			return builder.toString();
		}
	}
}
