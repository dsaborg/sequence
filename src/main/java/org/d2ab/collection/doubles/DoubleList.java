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
import org.d2ab.iterator.doubles.*;
import org.d2ab.util.Doubles;

import java.util.*;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A primitive specialization of {@link List} for {@code double} values.
 */
public interface DoubleList extends List<Double>, DoubleCollection {
	// TODO: Extract out relevant parts to IterableDoubleList

	/**
	 * Returns an immutable {@code DoubleList} of the given elements. The returned {@code DoubleList}'s
	 * {@link DoubleListIterator} supports forward iteration only.
	 */
	static DoubleList of(double... xs) {
		return new DoubleList.Base() {
			@Override
			public DoubleIterator iterator() {
				return new ArrayDoubleIterator(xs);
			}

			@Override
			public int size() {
				return xs.length;
			}
		};
	}

	/**
	 * @return a new empty mutable {@code DoubleList}.
	 *
	 * @since 2.1
	 */
	static DoubleList create() {
		return ArrayDoubleList.create();
	}

	/**
	 * @return a new mutable {@code DoubleList} with a copy of the given elements.
	 *
	 * @since 2.1
	 */
	static DoubleList create(double... xs) {
		return ArrayDoubleList.create(xs);
	}

	/**
	 * @return a {@code DoubleList} initialized with the members of the given {@link PrimitiveIterator.OfDouble}.
	 */
	static DoubleList copy(PrimitiveIterator.OfDouble iterator) {
		DoubleList copy = create();
		while (iterator.hasNext())
			copy.addDoubleExactly(iterator.nextDouble());
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
		return addDoubleExactly(x);
	}

	@Override
	default boolean addAll(int index, Collection<? extends Double> c) {
		if (c.isEmpty())
			return false;

		DoubleListIterator listIterator = listIterator(index);
		for (double t : c)
			listIterator.add(t);

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
		xs.forEachDouble(listIterator::add);

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

	default int binarySearchExactly(double x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void sort(Comparator<? super Double> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	default DoubleList subList(int from, int to) {
		return new SubList(this, from, to);
	}

	@Override
	default boolean addAll(Collection<? extends Double> c) {
		return Collectionz.addAll(this, c);
	}

	@Override
	default boolean addDoubleExactly(double x) {
		listIterator(size()).add(x);
		return true;
	}

	@Override
	default boolean addDouble(double x, double precision) {
		return addDoubleExactly(x);
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
		DoubleListIterator iterator = listIterator(index);
		if (!iterator.hasNext())
			throw new IndexOutOfBoundsException(String.valueOf(index));

		return iterator.nextDouble();
	}

	@Override
	default Double set(int index, Double x) {
		return setDouble(index, x);
	}

	default double setDouble(int index, double x) {
		DoubleListIterator listIterator = listIterator(index);
		double previous = listIterator.nextDouble();
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
		double previous = listIterator.nextDouble();
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
			if (Doubles.eq(iterator.nextDouble(), x, precision))
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
			if (iterator.nextDouble() == x)
				return index;

		return -1;
	}

	default int indexOfDouble(double x, double precision) {
		int index = 0;
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); index++)
			if (Doubles.eq(iterator.nextDouble(), x, precision))
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
		return Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED | Spliterator.NONNULL);
	}

	/**
	 * Base class for {@link DoubleList} implementations.
	 */
	abstract class Base extends DoubleCollection.Base implements DoubleList {
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof List))
				return false;

			DoubleListIterator it = listIterator();
			if (o instanceof DoubleList) {
				DoubleListIterator that = ((DoubleList) o).listIterator();
				while (it.hasNext()) {
					if (!that.hasNext())
						return false;
					if (it.nextDouble() != that.nextDouble())
						return false;
				}
				return !that.hasNext();
			} else {
				ListIterator<?> that = ((List<?>) o).listIterator();
				while (it.hasNext()) {
					if (!that.hasNext())
						return false;
					double x = it.nextDouble();
					Object y = that.next();
					if (!(y instanceof Double && x == (Double) y))
						return false;
				}
				return !that.hasNext();
			}
		}

		public int hashCode() {
			int hashCode = 1;
			for (DoubleIterator iterator = iterator(); iterator.hasNext(); )
				hashCode = 31 * hashCode + Double.hashCode(iterator.nextDouble());
			return hashCode;
		}
	}

	class SubList implements DoubleList {
		private final DoubleList list;

		private int from;
		private int to;

		public SubList(DoubleList list, int from, int to) {
			if (from < 0)
				throw new ArrayIndexOutOfBoundsException(from);
			if (to > list.size())
				throw new ArrayIndexOutOfBoundsException(to);
			this.list = list;
			this.from = from;
			this.to = to;
		}

		public DoubleIterator iterator() {
			return new DelegatingUnaryDoubleIterator(
					new LimitingDoubleIterator(new SkippingDoubleIterator(list.iterator(), from), to - from)) {
				@Override
				public double nextDouble() {
					return iterator.nextDouble();
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
