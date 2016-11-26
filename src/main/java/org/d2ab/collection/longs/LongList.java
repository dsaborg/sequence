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
import org.d2ab.iterator.longs.LimitingLongIterator;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.iterator.longs.SkippingLongIterator;
import org.d2ab.iterator.longs.UnaryLongIterator;

import java.util.*;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A primitive specialization of {@link List} for {@code long} values.
 */
public interface LongList extends List<Long>, LongCollection {
	/**
	 * @return a {@code LongList} of the given elements.
	 */
	static LongList of(long... xs) {
		return ArrayLongList.of(xs);
	}

	/**
	 * @return a {@code LongList} initialized with the members of the given {@link PrimitiveIterator.OfLong}.
	 */
	static LongList copy(PrimitiveIterator.OfLong iterator) {
		LongList copy = new ArrayLongList();
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
		if (c.size() == 0)
			return false;

		LongListIterator listIterator = listIterator(index);
		c.forEach(listIterator::add);

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
		if (xs.isEmpty())
			return false;

		LongListIterator listIterator = listIterator(index);
		xs.forEach(listIterator::add);

		return true;
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

	default void sortLongs(LongComparator c) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void sort(Comparator<? super Long> c) {
		sortLongs(c::compare);
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
		boolean modified = false;
		for (long x : c)
			modified |= addLong(x);
		return modified;
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
		if (xs.isEmpty())
			return false;

		LongListIterator listIterator = listIterator(size());
		xs.forEachLong(listIterator::add);

		return true;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return Collectionz.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return removeLongsIf(c::contains);
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
		return listIterator(index).nextLong();
	}

	@Override
	default Long set(int index, Long x) {
		return setLong(index, x);
	}

	default long setLong(int index, long x) {
		LongListIterator listIterator = listIterator(index);
		long previous = listIterator.next();
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
		long previous = listIterator.next();
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
			if (iterator.next() == x)
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

	class SubList implements LongList {
		private final LongList list;

		private int from;
		private int to;

		public SubList(LongList list, int from, int to) {
			this.list = list;
			this.from = from;
			this.to = to;
		}

		public LongIterator iterator() {
			return new UnaryLongIterator(new LimitingLongIterator(new SkippingLongIterator(list.iterator(), from), to - from)) {
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
