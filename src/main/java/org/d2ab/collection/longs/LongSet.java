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
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A primitive specialization of {@link Set} for {@code long} values.
 */
public interface LongSet extends Set<Long>, LongCollection {
	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default void clear() {
		iterator().removeAll();
	}

	@Override
	default boolean add(Long x) {
		return addLong(x);
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
	default Long[] toArray() {
		return toArray(new Long[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return Collectionz.toArray(this, a);
	}

	@Override
	default boolean addAll(Collection<? extends Long> c) {
		return LongCollections.addAll(this, c);
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
	default Spliterator.OfLong spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.DISTINCT | Spliterator.NONNULL);
	}

	/**
	 * Base class for {@link LongSet} implementations.
	 */
	abstract class Base extends LongCollection.Base implements LongSet {
		public static LongSet create(long... longs) {
			return from(LongSortedSet.create(longs));
		}

		public static LongSet from(final LongCollection collection) {
			return new LongSet.Base() {
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

		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof Set))
				return false;

			if (o instanceof LongSet) {
				LongSet that = (LongSet) o;
				return size() == that.size() && containsAllLongs(that);
			} else {
				Set<?> that = (Set<?>) o;
				return size() == that.size() && containsAll(that);
			}
		}

		public int hashCode() {
			int hashCode = 0;
			for (LongIterator iterator = iterator(); iterator.hasNext(); )
				hashCode += Long.hashCode(iterator.nextLong());
			return hashCode;
		}
	}
}
