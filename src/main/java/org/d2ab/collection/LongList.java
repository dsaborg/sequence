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

import java.util.*;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A primitive specialization of {@link List} for {@code long} values.
 */
public interface LongList extends List<Long>, LongCollection {
	static LongList of(long... ls) {
		return new ArrayLongList(ls);
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
		return containsLong((int) o);
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
		return removeLong((int) o);
	}

	@Override
	default boolean add(Long integer) {
		return addLong(integer);
	}

	@Override
	default boolean addAll(int index, Collection<? extends Long> c) {
		if (c.size() == 0)
			return false;

		c.forEach(listIterator(index)::add);
		return true;
	}

	default boolean addAllAt(int index, long... ls) {
		if (ls.length == 0)
			return false;

		LongListIterator listIterator = listIterator(index);
		for (long l : ls)
			listIterator.add(l);
		return true;
	}

	@Override
	default void replaceAll(UnaryOperator<Long> operator) {
		replaceAllLongs((LongUnaryOperator) operator);
	}

	default void replaceAllLongs(LongUnaryOperator operator) {
		LongListIterator listIterator = listIterator();
		while (listIterator.hasNext())
			listIterator.set(operator.applyAsLong(listIterator.nextLong()));
	}

	default void sortLongs(LongComparator c) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void sort(Comparator<? super Long> c) {
		sortLongs((LongComparator) c);
	}

	@Override
	default LongList subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean addAll(Collection<? extends Long> c) {
		boolean modified = false;
		for (long l : c)
			modified |= addLong(l);
		return modified;
	}

	default boolean addLong(long l) {
		listIterator(size()).add(l);
		return true;
	}

	default boolean addAll(long... ls) {
		if (ls.length == 0)
			return false;

		LongListIterator listIterator = listIterator(size());
		for (long l : ls)
			listIterator.add(l);

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
		return removeLongsIf((LongPredicate) filter);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return removeLongsIf(l -> !c.contains(l));
	}

	@Override
	default Long get(int index) {
		return getAt(index);
	}

	default long getAt(int index) {
		return listIterator(index).next();
	}

	@Override
	default Long set(int index, Long element) {
		return setAt(index, element);
	}

	default long setAt(int index, long element) {
		LongListIterator listIterator = listIterator(index);
		long previous = listIterator.next();
		listIterator.set(element);
		return previous;
	}

	@Override
	default void add(int index, Long element) {
		addAt(index, element);
	}

	default void addAt(int index, long element) {
		listIterator(index).add(element);
	}

	@Override
	default Long remove(int index) {
		return removeAt(index);
	}

	default long removeAt(int index) {
		LongListIterator listIterator = listIterator(index);
		long previous = listIterator.next();
		listIterator.remove();
		return previous;
	}

	@Override
	default int lastIndexOf(Object o) {
		return lastIndexOf((long) o);
	}

	default int lastIndexOf(long l) {
		int lastIndex = -1;

		int index = 0;
		for (LongIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.nextLong() == l)
				lastIndex = index;

		return lastIndex;
	}

	@Override
	default int indexOf(Object o) {
		return indexOf((long) o);
	}

	default int indexOf(long l) {
		int index = 0;
		for (LongIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.next() == l)
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

	default Spliterator.OfLong spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED);
	}
}
