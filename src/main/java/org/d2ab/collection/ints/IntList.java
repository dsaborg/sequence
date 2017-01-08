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

import org.d2ab.collection.PrimitiveCollections;
import org.d2ab.collection.chars.CharList;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.ints.DelegatingUnaryIntIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.iterator.ints.LimitingIntIterator;
import org.d2ab.iterator.ints.SkippingIntIterator;
import org.d2ab.util.Strict;

import java.util.*;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A primitive specialization of {@link List} for {@code int} values.
 */
public interface IntList extends List<Integer>, IntCollection {
	/**
	 * Returns an immutable {@code IntList} of the given elements. The returned {@code IntList}'s
	 * {@link IntListIterator} supports forward iteration only.
	 */
	static IntList of(int... xs) {
		return new IntList.Base() {
			@Override
			public IntListIterator listIterator(int index) {
				return new ArrayIntListIterator(index, xs);
			}

			@Override
			public int size() {
				return xs.length;
			}
		};
	}

	/**
	 * @return a new empty mutable {@code IntList}.
	 *
	 * @since 2.1
	 */
	static IntList create() {
		return ArrayIntList.create();
	}

	/**
	 * @return a new mutable {@code IntList} with a copy of the given elements.
	 *
	 * @since 2.1
	 */
	static IntList create(int... xs) {
		return ArrayIntList.create(xs);
	}

	@Override
	default IntIterator iterator() {
		return listIterator();
	}

	@Override
	default IntListIterator listIterator() {
		return listIterator(0);
	}

	@Override
	IntListIterator listIterator(int index);

	/**
	 * @return an {@code IntList} initialized with the members of the given {@link PrimitiveIterator.OfInt}.
	 */
	static IntList copy(PrimitiveIterator.OfInt iterator) {
		IntList copy = create();
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
	default IntList asList() {
		return this;
	}

	@Override
	default Integer[] toArray() {
		return toArray(new Integer[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return PrimitiveCollections.toArray(this, a);
	}

	@Override
	default boolean add(Integer x) {
		return IntCollections.add(this, x);
	}

	@Override
	default boolean contains(Object o) {
		return IntCollections.contains(this, o);
	}

	@Override
	default boolean remove(Object o) {
		return IntCollections.remove(this, o);
	}

	@Override
	default boolean addAll(int index, Collection<? extends Integer> c) {
		if (c instanceof IntCollection)
			return addAllIntsAt(index, (IntCollection) c);

		Strict.check();

		if (c.isEmpty())
			return false;

		IntListIterator listIterator = listIterator(index);
		for (int t : c)
			listIterator.add(t);

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
		IntListIterator listIterator = listIterator(index);

		boolean modified = false;
		for (IntIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			listIterator.add(iterator.nextInt());
			modified = true;
		}
		return modified;
	}

	@Override
	default void replaceAll(UnaryOperator<Integer> operator) {
		Strict.check();

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

	@Override
	default void sort(Comparator<? super Integer> c) {
		Strict.check();

		throw new UnsupportedOperationException();
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
		return IntCollections.addAll(this, c);
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
		IntListIterator listIterator = listIterator(size());

		boolean modified = false;
		for (IntIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			listIterator.add(iterator.nextInt());
			modified = true;
		}
		return modified;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return IntCollections.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return IntCollections.removeAll(this, c);
	}

	@Override
	default boolean removeIf(Predicate<? super Integer> filter) {
		Strict.check();

		return removeIntsIf(filter::test);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return IntCollections.retainAll(this, c);
	}

	@Override
	default Integer get(int index) {
		Strict.check();

		return getInt(index);
	}

	default int getInt(int index) {
		IntListIterator iterator = listIterator(index);
		if (!iterator.hasNext())
			throw new IndexOutOfBoundsException(String.valueOf(index));

		return iterator.nextInt();
	}

	@Override
	default Integer set(int index, Integer x) {
		Strict.check();

		return setInt(index, x);
	}

	default int setInt(int index, int x) {
		IntListIterator listIterator = listIterator(index);
		if (!listIterator.hasNext())
			throw new IndexOutOfBoundsException(String.valueOf(index));
		int previous = listIterator.nextInt();
		listIterator.set(x);
		return previous;
	}

	@Override
	default void add(int index, Integer x) {
		Strict.check();

		addIntAt(index, x);
	}

	default void addIntAt(int index, int x) {
		listIterator(index).add(x);
	}

	@Override
	default Integer remove(int index) {
		Strict.check();

		return removeIntAt(index);
	}

	default int removeIntAt(int index) {
		IntListIterator listIterator = listIterator(index);
		if (!listIterator.hasNext())
			throw new IndexOutOfBoundsException(String.valueOf(index));
		int previous = listIterator.nextInt();
		listIterator.remove();
		return previous;
	}

	@Override
	default int lastIndexOf(Object o) {
		Strict.check();

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
		Strict.check();

		return o instanceof Integer ? indexOfInt((int) o) : -1;
	}

	default int indexOfInt(int x) {
		int index = 0;
		for (IntIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.nextInt() == x)
				return index;

		return -1;
	}

	@Override
	default Spliterator.OfInt spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED | Spliterator.NONNULL);
	}

	@Override
	default CharList asChars() {
		return new CharList() {
			@Override
			public CharIterator iterator() {
				return CharIterator.from(IntList.this.iterator());
			}

			@Override
			public int size() {
				return IntList.this.size();
			}
		};
	}

	/**
	 * Base class for {@link IntList} implementations.
	 */
	abstract class Base extends IntCollection.Base implements IntList {
		public static IntList create(int... ints) {
			final IntList backing = IntList.create(ints);
			return new IntList.Base() {
				@Override
				public IntListIterator listIterator(int index) {
					return backing.listIterator(index);
				}

				@Override
				public int size() {
					return backing.size();
				}
			};
		}

		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof List))
				return false;

			IntListIterator it = listIterator();
			if (o instanceof IntList) {
				IntListIterator that = ((IntList) o).listIterator();
				while (it.hasNext()) {
					if (!that.hasNext())
						return false;
					if (it.nextInt() != that.nextInt())
						return false;
				}
				return !that.hasNext();
			} else {
				ListIterator<?> that = ((List<?>) o).listIterator();
				while (it.hasNext()) {
					if (!that.hasNext())
						return false;
					int x = it.nextInt();
					Object y = that.next();
					if (!(y instanceof Integer && x == (Integer) y))
						return false;
				}
				return !that.hasNext();
			}
		}

		public int hashCode() {
			int hashCode = 1;
			for (IntIterator iterator = iterator(); iterator.hasNext(); )
				hashCode = 31 * hashCode + Integer.hashCode(iterator.nextInt());
			return hashCode;
		}
	}

	// TODO make full-fledged IntList implementation
	class SubList extends Base implements IterableIntList {
		private final IntList list;

		protected int from;
		protected int to;

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
			return new DelegatingUnaryIntIterator(
					new LimitingIntIterator(new SkippingIntIterator(list.iterator(), from), to - from)) {
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
