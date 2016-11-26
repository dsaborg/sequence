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

package org.d2ab.collection.ints;

import org.d2ab.collection.Collectionz;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.iterator.ints.LimitingIntIterator;
import org.d2ab.iterator.ints.SkippingIntIterator;
import org.d2ab.iterator.ints.UnaryIntIterator;

import java.util.*;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A primitive specialization of {@link List} for {@code int} values.
 */
public interface IntList extends List<Integer>, IntCollection {
	/**
	 * @return an {@code IntList} of the given elements.
	 */
	static IntList of(int... xs) {
		return ArrayIntList.of(xs);
	}

	/**
	 * @return an {@code IntList} initialized with the members of the given {@link PrimitiveIterator.OfInt}.
	 */
	static IntList copy(PrimitiveIterator.OfInt iterator) {
		IntList copy = new ArrayIntList();
		while (iterator.hasNext())
			copy.addInt(iterator.nextInt());
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
		return o instanceof Integer && containsInt((int) o);
	}

	@Override
	default Integer[] toArray() {
		return toArray(new Integer[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return Collectionz.toArray(this, a);
	}

	@Override
	default boolean remove(Object o) {
		return o instanceof Integer && removeInt((int) o);
	}

	@Override
	default boolean add(Integer x) {
		return addInt(x);
	}

	@Override
	default boolean addAll(int index, Collection<? extends Integer> c) {
		if (c.size() == 0)
			return false;

		IntListIterator listIterator = listIterator(index);
		c.forEach(listIterator::add);

		return true;
	}

	default boolean addAllIntsAt(int index, int... xs) {
		if (xs.length == 0)
			return false;

		IntListIterator listIterator = listIterator(index);
		for (int x : xs)
			listIterator.add(x);

		return true;
	}

	default boolean addAllIntsAt(int index, IntCollection xs) {
		if (xs.isEmpty())
			return false;

		IntListIterator listIterator = listIterator(index);
		xs.forEach(listIterator::add);

		return true;
	}

	@Override
	default void replaceAll(UnaryOperator<Integer> operator) {
		replaceAllInts(operator::apply);
	}

	default void replaceAllInts(IntUnaryOperator operator) {
		IntListIterator listIterator = listIterator();
		while (listIterator.hasNext())
			listIterator.set(operator.applyAsInt(listIterator.nextInt()));
	}

	default void sortInts() {
		throw new UnsupportedOperationException();
	}

	default void sortInts(IntComparator c) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void sort(Comparator<? super Integer> c) {
		sortInts(c::compare);
	}

	default int binarySearch(int x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default IntList subList(int from, int to) {
		return new SubList(this, from, to);
	}

	@Override
	default boolean addAll(Collection<? extends Integer> c) {
		boolean modified = false;
		for (int x : c)
			modified |= addInt(x);
		return modified;
	}

	@Override
	default boolean addInt(int x) {
		listIterator(size()).add(x);
		return true;
	}

	@Override
	default boolean addAllInts(int... xs) {
		if (xs.length == 0)
			return false;

		IntListIterator listIterator = listIterator(size());
		for (int x : xs)
			listIterator.add(x);

		return true;
	}

	@Override
	default boolean addAllInts(IntCollection xs) {
		if (xs.isEmpty())
			return false;

		IntListIterator listIterator = listIterator(size());
		xs.forEachInt(listIterator::add);

		return true;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return Collectionz.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return removeIntsIf(c::contains);
	}

	@Override
	default boolean removeIf(Predicate<? super Integer> filter) {
		return removeIntsIf(filter::test);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return removeIntsIf(x -> !c.contains(x));
	}

	@Override
	default Integer get(int index) {
		return getInt(index);
	}

	default int getInt(int index) {
		return listIterator(index).nextInt();
	}

	@Override
	default Integer set(int index, Integer x) {
		return setInt(index, x);
	}

	default int setInt(int index, int x) {
		IntListIterator listIterator = listIterator(index);
		int previous = listIterator.next();
		listIterator.set(x);
		return previous;
	}

	@Override
	default void add(int index, Integer x) {
		addIntAt(index, x);
	}

	default void addIntAt(int index, int x) {
		listIterator(index).add(x);
	}

	@Override
	default Integer remove(int index) {
		return removeIntAt(index);
	}

	default int removeIntAt(int index) {
		IntListIterator listIterator = listIterator(index);
		int previous = listIterator.next();
		listIterator.remove();
		return previous;
	}

	@Override
	default int lastIndexOf(Object o) {
		return o instanceof Integer ? lastIndexOfInt((int) o) : -1;
	}

	default int lastIndexOfInt(int x) {
		int lastIndex = -1;

		int index = 0;
		for (IntIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.nextInt() == x)
				lastIndex = index;

		return lastIndex;
	}

	@Override
	default int indexOf(Object o) {
		return o instanceof Integer ? indexOfInt((int) o) : -1;
	}

	default int indexOfInt(int x) {
		int index = 0;
		for (IntIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.next() == x)
				return index;

		return -1;
	}

	@Override
	default IntListIterator listIterator() {
		return listIterator(0);
	}

	@Override
	default IntListIterator listIterator(int index) {
		return IntListIterator.forwardOnly(iterator(), index);
	}

	@Override
	default Spliterator.OfInt spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED | Spliterator.NONNULL);
	}

	class SubList implements IntList {
		private final IntList list;

		private int from;
		private int to;

		public SubList(IntList list, int from, int to) {
			if (from < 0)
				throw new ArrayIndexOutOfBoundsException(from);
			if (to > list.size())
				throw new ArrayIndexOutOfBoundsException(to);
			this.list = list;
			this.from = from;
			this.to = to;
		}

		public IntIterator iterator() {
			return new UnaryIntIterator(new LimitingIntIterator(new SkippingIntIterator(list.iterator(), from), to - from)) {
				@Override
				public int nextInt() {
					return iterator.nextInt();
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
