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
import org.d2ab.iterator.longs.*;

import java.util.*;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A primitive specialization of {@link List} for {@code long} values.
 */
public interface LongList extends List<Long>, LongCollection {
	// TODO: Extract out relevant parts to IterableLongList

	/**
	 * Returns an immutable {@code LongList} of the given elements. The returned {@code LongList}'s
	 * {@link LongListIterator} supports forward iteration only.
	 */
	static LongList of(long... xs) {
		return new LongList.Base() {
			@Override
			public LongIterator iterator() {
				return new ArrayLongIterator(xs);
			}

			@Override
			public int size() {
				return xs.length;
			}
		};
	}

	/**
	 * @return a new empty mutable {@code LongList}.
	 *
	 * @since 2.1
	 */
	static LongList create() {
		return ArrayLongList.create();
	}

	/**
	 * @return a new mutable {@code LongList} with a copy of the given elements.
	 *
	 * @since 2.1
	 */
	static LongList create(long... xs) {
		return ArrayLongList.create(xs);
	}

	/**
	 * @return a {@code LongList} initialized with the members of the given {@link PrimitiveIterator.OfLong}.
	 */
	static LongList copy(PrimitiveIterator.OfLong iterator) {
		LongList copy = create();
		while (iterator.hasNext())
			copy.addLong(iterator.nextLong());
		return copy;
	}

	default void clear() {
		iterator().removeAll();
	}

	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default LongList asList() {
		return this;
	}

	@Override
	default boolean contains(Object o) {
		return o instanceof Long && containsLong((long) o);
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
	default boolean remove(Object o) {
		return o instanceof Long && removeLong((long) o);
	}

	@Override
	default boolean add(Long x) {
		return addLong(x);
	}

	@Override
	default boolean addAll(int index, Collection<? extends Long> c) {
		if (c.isEmpty())
			return false;

		LongListIterator listIterator = listIterator(index);
		for (long x : c)
			listIterator.add(x);

		return true;
	}

	default boolean addAllLongsAt(int index, long... xs) {
		if (xs.length == 0)
			return false;

		LongListIterator listIterator = listIterator(index);
		for (long x : xs)
			listIterator.add(x);

		return true;
	}

	default boolean addAllLongsAt(int index, LongCollection xs) {
		LongListIterator listIterator = listIterator(index);

		boolean modified = false;
		for (LongIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			listIterator.add(iterator.nextLong());
			modified = true;
		}
		return modified;
	}

	@Override
	default void replaceAll(UnaryOperator<Long> operator) {
		replaceAllLongs(operator::apply);
	}

	default void replaceAllLongs(LongUnaryOperator operator) {
		LongListIterator listIterator = listIterator();
		while (listIterator.hasNext())
			listIterator.set(operator.applyAsLong(listIterator.nextLong()));
	}

	default void sortLongs() {
		throw new UnsupportedOperationException();
	}

	@Override
	default void sort(Comparator<? super Long> c) {
		throw new UnsupportedOperationException();
	}

	default int binarySearch(long x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default LongList subList(int from, int to) {
		return new SubList(this, from, to);
	}

	@Override
	default boolean addAll(Collection<? extends Long> c) {
		return LongCollections.addAll(this, c);
	}

	@Override
	default boolean addLong(long x) {
		listIterator(size()).add(x);
		return true;
	}

	@Override
	default boolean addAllLongs(long... xs) {
		if (xs.length == 0)
			return false;

		LongListIterator listIterator = listIterator(size());
		for (long x : xs)
			listIterator.add(x);

		return true;
	}

	@Override
	default boolean addAllLongs(LongCollection xs) {
		LongListIterator listIterator = listIterator(size());

		boolean modified = false;
		for (LongIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			listIterator.add(iterator.nextLong());
			modified = true;
		}
		return modified;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return LongCollections.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		boolean modified = false;
		for (LongIterator iterator = iterator(); iterator.hasNext(); ) {
			if (c.contains(iterator.nextLong())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	@Override
	default boolean removeIf(Predicate<? super Long> filter) {
		return removeLongsIf(filter::test);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return removeLongsIf(x -> !c.contains(x));
	}

	@Override
	default Long get(int index) {
		return getLong(index);
	}

	default long getLong(int index) {
		LongListIterator iterator = listIterator(index);
		if (!iterator.hasNext())
			throw new IndexOutOfBoundsException(String.valueOf(index));

		return iterator.nextLong();
	}

	@Override
	default Long set(int index, Long x) {
		return setLong(index, x);
	}

	default long setLong(int index, long x) {
		LongListIterator listIterator = listIterator(index);
		if (!listIterator.hasNext())
			throw new IndexOutOfBoundsException(String.valueOf(index));
		long previous = listIterator.nextLong();
		listIterator.set(x);
		return previous;
	}

	@Override
	default void add(int index, Long x) {
		addLongAt(index, x);
	}

	default void addLongAt(int index, long x) {
		listIterator(index).add(x);
	}

	@Override
	default Long remove(int index) {
		return removeLongAt(index);
	}

	default long removeLongAt(int index) {
		LongListIterator listIterator = listIterator(index);
		long previous = listIterator.nextLong();
		listIterator.remove();
		return previous;
	}

	@Override
	default int lastIndexOf(Object o) {
		return o instanceof Long ? lastIndexOfLong((long) o) : -1;
	}

	default int lastIndexOfLong(long x) {
		int lastIndex = -1;

		int index = 0;
		for (LongIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.nextLong() == x)
				lastIndex = index;

		return lastIndex;
	}

	@Override
	default int indexOf(Object o) {
		return o instanceof Long ? indexOfLong((long) o) : -1;
	}

	default int indexOfLong(long x) {
		int index = 0;
		for (LongIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.nextLong() == x)
				return index;

		return -1;
	}

	@Override
	default LongListIterator listIterator() {
		return listIterator(0);
	}

	@Override
	default LongListIterator listIterator(int index) {
		return LongListIterator.forwardOnly(iterator(), index);
	}

	@Override
	default Spliterator.OfLong spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED | Spliterator.NONNULL);
	}

	/**
	 * Base class for {@link LongList} implementations.
	 */
	abstract class Base extends LongCollection.Base implements LongList {
		public static LongList create(long... longs) {
			return from(LongList.create(longs));
		}

		public static LongList from(final LongCollection collection) {
			return new LongList.Base() {
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

		public static LongList from(final LongList list) {
			return new LongList.Base() {
				@Override
				public LongIterator iterator() {
					return list.iterator();
				}

				@Override
				public LongListIterator listIterator(int index) {
					return list.listIterator(index);
				}

				@Override
				public int size() {
					return list.size();
				}

				@Override
				public boolean addLong(long x) {
					return list.addLong(x);
				}
			};
		}

		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof List))
				return false;

			LongListIterator it = listIterator();
			if (o instanceof LongList) {
				LongListIterator that = ((LongList) o).listIterator();
				while (it.hasNext()) {
					if (!that.hasNext())
						return false;
					if (it.nextLong() != that.nextLong())
						return false;
				}
				return !that.hasNext();
			} else {
				ListIterator<?> that = ((List<?>) o).listIterator();
				while (it.hasNext()) {
					if (!that.hasNext())
						return false;
					long x = it.nextLong();
					Object y = that.next();
					if (!(y instanceof Long && x == (Long) y))
						return false;
				}
				return !that.hasNext();
			}
		}

		public int hashCode() {
			int hashCode = 1;
			for (LongIterator iterator = iterator(); iterator.hasNext(); )
				hashCode = 31 * hashCode + Long.hashCode(iterator.nextLong());
			return hashCode;
		}
	}

	class SubList implements LongList {
		private final LongList list;

		private int from;
		private int to;

		public SubList(LongList list, int from, int to) {
			if (from < 0)
				throw new ArrayIndexOutOfBoundsException(from);
			if (to > list.size())
				throw new ArrayIndexOutOfBoundsException(to);
			this.list = list;
			this.from = from;
			this.to = to;
		}

		public LongIterator iterator() {
			return new DelegatingUnaryLongIterator(
					new LimitingLongIterator(new SkippingLongIterator(list.iterator(), from), to - from)) {
				@Override
				public long nextLong() {
					return iterator.nextLong();
				}

				@Override
				public void remove() {
					super.remove();
					to--;
				}
			};
		}

		public int size() {
			return to - from;
		}
	}
}
