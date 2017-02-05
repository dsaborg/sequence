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

import org.d2ab.collection.PrimitiveCollections;
import org.d2ab.iterator.longs.LongIterator;

import java.util.Collection;
import java.util.RandomAccess;
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

	/**
	 * @return a {@link LongList} view of this {@code LongCollection}, which is updated as the {@code LongCollection}
	 * changes. The list does not implement {@link RandomAccess} and is best accessed in sequence.
	 *
	 * @since 2.2
	 */
	default LongList asList() {
		return CollectionLongList.from(this);
	}

	@Override
	default Long[] toArray() {
		return toArray(new Long[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return PrimitiveCollections.toArray(this, a);
	}

	/**
	 * Collect the {@code longs} in this {@code LongCollection} into a {@code long}-array.
	 */
	default long[] toLongArray() {
		return new ArrayLongList(this).toLongArray();
	}

	@Override
	default boolean add(Long x) {
		return LongCollections.add(x, this);
	}

	default boolean addLong(long x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean contains(Object o) {
		return LongCollections.contains(o, this);
	}

	@Override
	default boolean remove(Object o) {
		return LongCollections.remove(o, this);
	}

	@Override
	default boolean addAll(Collection<? extends Long> c) {
		return LongCollections.addAll(this, c);
	}

	default boolean addAllLongs(long... xs) {
		boolean modified = false;
		for (long x : xs)
			modified |= addLong(x);
		return modified;
	}

	default boolean addAllLongs(LongCollection c) {
		boolean modified = false;
		for (LongIterator iterator = c.iterator(); iterator.hasNext(); )
			modified |= addLong(iterator.nextLong());
		return modified;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return LongCollections.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return LongCollections.removeAll(this, c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return LongCollections.retainAll(this, c);
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
			return from(LongList.create(longs));
		}

		public static LongCollection from(final LongCollection collection) {
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
