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

package org.d2ab.collection.doubles;

import org.d2ab.collection.Collectionz;
import org.d2ab.iterator.doubles.DoubleIterator;

import java.util.*;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A primitive specialization of {@link List} for {@code double} values.
 */
public interface DoubleList extends List<Double>, DoubleCollection {
	/**
	 * @return a {@code DoubleList} of the given elements.
	 */
	static DoubleList of(double... xs) {
		return ArrayDoubleList.of(xs);
	}

	/**
	 * @return a {@code DoubleList} initialized with the members of the given {@link PrimitiveIterator.OfDouble}.
	 */
	static DoubleList copy(PrimitiveIterator.OfDouble iterator) {
		DoubleList copy = new ArrayDoubleList();
		while (iterator.hasNext())
			copy.add(iterator.nextDouble());
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
		return o instanceof Double && containsDoubleExactly((double) o);
	}

	@Override
	default Double[] toArray() {
		return toArray(new Double[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return Collectionz.toArray(this, a);
	}

	@Override
	default boolean remove(Object o) {
		return o instanceof Double && removeDoubleExactly((double) o);
	}

	@Override
	default boolean add(Double x) {
		return addDouble(x);
	}

	@Override
	default boolean addAll(int index, Collection<? extends Double> c) {
		if (c.size() == 0)
			return false;

		DoubleListIterator listIterator = listIterator(index);
		c.forEach(listIterator::add);

		return true;
	}

	default boolean addAllDoublesAt(int index, double... xs) {
		if (xs.length == 0)
			return false;

		DoubleListIterator listIterator = listIterator(index);
		for (double x : xs)
			listIterator.add(x);

		return true;
	}

	default boolean addAllDoublesAt(int index, DoubleCollection xs) {
		if (xs.isEmpty())
			return false;

		DoubleListIterator listIterator = listIterator(index);
		xs.forEach(listIterator::add);

		return true;
	}

	@Override
	default void replaceAll(UnaryOperator<Double> operator) {
		replaceAllDoubles(operator::apply);
	}

	default void replaceAllDoubles(DoubleUnaryOperator operator) {
		DoubleListIterator listIterator = listIterator();
		while (listIterator.hasNext())
			listIterator.set(operator.applyAsDouble(listIterator.nextDouble()));
	}

	default void sortDoubles() {
		throw new UnsupportedOperationException();
	}

	default void sortDoubles(DoubleComparator c) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void sort(Comparator<? super Double> c) {
		sortDoubles(c::compare);
	}

	@Override
	default DoubleList subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean addAll(Collection<? extends Double> c) {
		boolean modified = false;
		for (double x : c)
			modified |= addDouble(x);
		return modified;
	}

	@Override
	default boolean addDouble(double x) {
		listIterator(size()).add(x);
		return true;
	}

	@Override
	default boolean addAllDoubles(double... xs) {
		if (xs.length == 0)
			return false;

		DoubleListIterator listIterator = listIterator(size());
		for (double x : xs)
			listIterator.add(x);

		return true;
	}

	@Override
	default boolean addAllDoubles(DoubleCollection xs) {
		if (xs.isEmpty())
			return false;

		DoubleListIterator listIterator = listIterator(size());
		xs.forEachDouble(listIterator::add);

		return true;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return Collectionz.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return removeDoublesIf(c::contains);
	}

	@Override
	default boolean removeIf(Predicate<? super Double> filter) {
		return removeDoublesIf(filter::test);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return removeDoublesIf(x -> !c.contains(x));
	}

	@Override
	default Double get(int index) {
		return getDouble(index);
	}

	default double getDouble(int index) {
		return listIterator(index).nextDouble();
	}

	@Override
	default Double set(int index, Double x) {
		return setDouble(index, x);
	}

	default double setDouble(int index, double x) {
		DoubleListIterator listIterator = listIterator(index);
		double previous = listIterator.next();
		listIterator.set(x);
		return previous;
	}

	@Override
	default void add(int index, Double x) {
		addDoubleAt(index, x);
	}

	default void addDoubleAt(int index, double x) {
		listIterator(index).add(x);
	}

	@Override
	default Double remove(int index) {
		return removeDoubleAt(index);
	}

	default double removeDoubleAt(int index) {
		DoubleListIterator listIterator = listIterator(index);
		double previous = listIterator.next();
		listIterator.remove();
		return previous;
	}

	@Override
	default int lastIndexOf(Object o) {
		return o instanceof Double ? lastIndexOfDoubleExactly((double) o) : -1;
	}

	default int lastIndexOfDoubleExactly(double x) {
		int lastIndex = -1;

		int index = 0;
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.nextDouble() == x)
				lastIndex = index;

		return lastIndex;
	}

	default int lastIndexOfDouble(double x, double precision) {
		int lastIndex = -1;

		int index = 0;
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); index++)
			if (DoubleComparator.equals(iterator.nextDouble(), x, precision))
				lastIndex = index;

		return lastIndex;
	}

	@Override
	default int indexOf(Object o) {
		return o instanceof Double ? indexOfDoubleExactly((double) o) : -1;
	}

	default int indexOfDoubleExactly(double x) {
		int index = 0;
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.next() == x)
				return index;

		return -1;
	}

	default int indexOfDouble(double x, double precision) {
		int index = 0;
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); index++)
			if (DoubleComparator.equals(iterator.next(), x, precision))
				return index;

		return -1;
	}

	@Override
	default DoubleListIterator listIterator() {
		return listIterator(0);
	}

	@Override
	default DoubleListIterator listIterator(int index) {
		return DoubleListIterator.forwardOnly(iterator(), index);
	}

	@Override
	default Spliterator.OfDouble spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED);
	}
}
