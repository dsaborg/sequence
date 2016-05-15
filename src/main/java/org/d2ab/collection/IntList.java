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

import org.d2ab.iterator.ints.IntIterator;

import java.util.*;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A {@link List} backed by ints.
 */
public interface IntList extends List<Integer>, IntCollection {
	static IntList of(int... is) {
		return new ArrayIntList(is);
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
		return containsInt((int) o);
	}

	@Override
	default Integer[] toArray() {
		return Collectionz.toBoxedIntegerArray(this);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return Collectionz.toArray(this, a);
	}

	@Override
	default boolean remove(Object o) {
		return removeInt((int) o);
	}

	@Override
	default boolean add(Integer integer) {
		return addInt(integer);
	}

	@Override
	default boolean addAll(int index, Collection<? extends Integer> c) {
		if (c.size() == 0)
			return false;

		c.forEach(listIterator(index)::add);
		return true;

	}

	default boolean addAllAt(int index, int... is) {
		if (is.length == 0)
			return false;

		IntListIterator listIterator = listIterator(index);
		for (int i : is)
			listIterator.add(i);
		return true;
	}

	@Override
	default void replaceAll(UnaryOperator<Integer> operator) {
		replaceAllInts((IntUnaryOperator) operator);
	}

	default void replaceAllInts(IntUnaryOperator operator) {
		IntListIterator listIterator = listIterator();
		while (listIterator.hasNext())
			listIterator.set(operator.applyAsInt(listIterator.nextInt()));
	}

	default void sortInts(IntComparator c) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void sort(Comparator<? super Integer> c) {
		sortInts((IntComparator) c);
	}

	@Override
	default IntList subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean addAll(Collection<? extends Integer> c) {
		boolean modified = false;
		for (int i : c)
			modified |= addInt(i);
		return modified;
	}

	default boolean addInt(int i) {
		listIterator(size()).add(i);
		return true;
	}

	default boolean addAll(int... is) {
		if (is.length == 0)
			return false;

		IntListIterator listIterator = listIterator(size());
		for (int i : is)
			listIterator.add(i);

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
		return removeIntsIf((IntPredicate) filter);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return removeIntsIf(i -> !c.contains(i));
	}

	@Override
	default Integer get(int index) {
		return getAt(index);
	}

	default int getAt(int index) {
		return listIterator(index).next();
	}

	@Override
	default Integer set(int index, Integer element) {
		return setAt(index, element);
	}

	default int setAt(int index, int element) {
		IntListIterator listIterator = listIterator(index);
		int previous = listIterator.next();
		listIterator.set(element);
		return previous;
	}

	@Override
	default void add(int index, Integer element) {
		addAt(index, element);
	}

	default void addAt(int index, int element) {
		listIterator(index).add(element);
	}

	@Override
	default Integer remove(int index) {
		return removeAt(index);
	}

	default int removeAt(int index) {
		IntListIterator listIterator = listIterator(index);
		int previous = listIterator.next();
		listIterator.remove();
		return previous;
	}

	@Override
	default int lastIndexOf(Object o) {
		return lastIndexOf((int) o);
	}

	default int lastIndexOf(int i) {
		int lastIndex = -1;

		int index = 0;
		for (IntIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.nextInt() == i)
				lastIndex = index;

		return lastIndex;
	}

	@Override
	default int indexOf(Object o) {
		return indexOf((int) o);
	}

	default int indexOf(int i) {
		int index = 0;
		for (IntIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.next() == i)
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

	default Spliterator.OfInt spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED);
	}
}
