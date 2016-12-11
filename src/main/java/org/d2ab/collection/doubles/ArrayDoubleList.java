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

import org.d2ab.collection.Arrayz;
import org.d2ab.iterator.doubles.DoubleIterator;

import java.util.*;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;

import static org.d2ab.collection.doubles.DoubleComparator.eq;

/**
 * A {@link DoubleList} backed by a double-array, supporting all {@link DoubleList}-methods by modifying and/or replacing the
 * underlying array.
 */
public class ArrayDoubleList extends DoubleList.Base implements DoubleList, RandomAccess {
	private double[] contents;
	private int size;

	private int modCount;

	/**
	 * @return a new mutable {@code ArrayDoubleList} initialized with a copy of the given contents.
	 *
	 * @deprecated Use {@link #create(double...)} instead.
	 */
	@Deprecated
	public static ArrayDoubleList of(double... xs) {
		return create(xs);
	}

	/**
	 * Create a new empty mutable {@code ArrayDoubleList}. When possible, it's preferred to use
	 * {@link DoubleList#create()} instead.
	 *
	 * @return a new empty mutable {@code ArrayDoubleList}.
	 *
	 * @see DoubleList#create()
	 * @see #withCapacity(int)
	 *
	 * @since 2.1
	 */
	public static ArrayDoubleList create() {
		return new ArrayDoubleList();
	}

	/**
	 * Create a new mutable {@code ArrayDoubleList} initialized with a copy of the given contents. When possible, it's
	 * preferred to use {@link DoubleList#create(double...)} instead.
	 *
	 * @return a new mutable {@code ArrayDoubleList} initialized with a copy of the given contents.
	 *
	 * @see DoubleList#create(double...)
	 * @see #ArrayDoubleList(DoubleCollection)
	 *
	 * @since 2.1
	 */
	public static ArrayDoubleList create(double... xs) {
		return new ArrayDoubleList(xs);
	}

	/**
	 * @return a new mutable {@code ArrayDoubleList} with the given initial capacity.
	 *
	 * @since 2.1
	 */
	public static ArrayDoubleList withCapacity(int capacity) {
		return new ArrayDoubleList(capacity);
	}

	/**
	 * Create a new mutable {@code ArrayDoubleList}.
	 *
	 * @since 2.0
	 *
	 * @deprecated Use {@link #create()} instead.
	 */
	@Deprecated
	public ArrayDoubleList() {
		this(10);
	}

	/**
	 * Create a new mutable {@code ArrayDoubleList} with the given initial capacity.
	 *
	 * @since 2.0
	 *
	 * @deprecated Use {@link #withCapacity(int)} instead.
	 */
	@Deprecated
	public ArrayDoubleList(int capacity) {
		this.contents = new double[capacity];
	}

	public ArrayDoubleList(DoubleCollection xs) {
		this();
		addAllDoubles(xs);
	}

	public ArrayDoubleList(double[] xs) {
		this.contents = Arrays.copyOf(xs, xs.length);
		this.size = xs.length;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public void clear() {
		size = 0;
	}

	@Override
	public double[] toDoubleArray() {
		return Arrays.copyOfRange(contents, 0, size);
	}

	@Override
	public DoubleIterator iterator() {
		return listIterator();
	}

	@Override
	public DoubleListIterator listIterator(int index) {
		rangeCheckForAdd(index);
		return new ListIter(index);
	}

	@Override
	public void sortDoubles() {
		Arrays.sort(contents, 0, size);
	}

	@Override
	public int binarySearchExactly(double x) {
		return Arrays.binarySearch(contents, 0, size, x);
	}

	@Override
	public DoubleList subList(int from, int to) {
		return new SubList(from, to);
	}

	@Override
	public void replaceAllDoubles(DoubleUnaryOperator operator) {
		for (int i = 0; i < size; i++)
			contents[i] = operator.applyAsDouble(contents[i]);

		modCount++;
	}

	@Override
	public double getDouble(int index) {
		rangeCheck(index);
		return contents[index];
	}

	@Override
	public double setDouble(int index, double x) {
		rangeCheck(index);
		double previous = contents[index];
		contents[index] = x;

		modCount++;
		return previous;
	}

	@Override
	public void addDoubleAt(int index, double x) {
		rangeCheckForAdd(index);
		uncheckedAdd(index, x);

		modCount++;
	}

	@Override
	public double removeDoubleAt(int index) {
		rangeCheck(index);
		double previous = contents[index];
		uncheckedRemove(index);

		modCount++;
		return previous;
	}

	@Override
	public int lastIndexOfDoubleExactly(double x) {
		for (int i = size - 1; i >= 0; i--)
			if (contents[i] == x)
				return i;

		return -1;
	}

	@Override
	public int indexOfDoubleExactly(double x) {
		for (int i = 0; i < size; i++)
			if (contents[i] == x)
				return i;

		return -1;
	}

	@Override
	public Spliterator.OfDouble spliterator() {
		return Arrays.spliterator(contents, 0, size);
	}

	@Override
	public boolean addDoubleExactly(double x) {
		growIfNecessaryBy(1);
		contents[size++] = x;

		modCount++;
		return true;
	}

	@Override
	public boolean addDouble(double x, double precision) {
		return addDoubleExactly(x);
	}

	@Override
	public boolean addAllDoubles(double... xs) {
		if (xs.length == 0)
			return false;

		growIfNecessaryBy(xs.length);
		System.arraycopy(xs, 0, contents, size, xs.length);
		size += xs.length;

		modCount++;
		return true;
	}

	@Override
	public boolean addAllDoubles(DoubleCollection xs) {
		int xsSize = xs.size();
		if (xsSize == 0)
			return false;

		growIfNecessaryBy(xsSize);
		uncheckedAdd(size, xs, xsSize);
		size += xsSize;

		modCount++;
		return true;
	}

	@Override
	public boolean addAllDoublesAt(int index, double... xs) {
		if (xs.length == 0)
			return false;

		rangeCheckForAdd(index);
		growIfNecessaryBy(xs.length);
		System.arraycopy(contents, index, contents, index + xs.length, size - index);
		System.arraycopy(xs, 0, contents, index, xs.length);
		size += xs.length;

		modCount++;
		return true;
	}

	@Override
	public boolean addAllDoublesAt(int index, DoubleCollection xs) {
		int xsSize = xs.size();
		if (xsSize == 0)
			return false;

		rangeCheckForAdd(index);
		growIfNecessaryBy(xsSize);
		System.arraycopy(contents, index, contents, index + xsSize, size - index);
		uncheckedAdd(index, xs, xsSize);
		size += xsSize;

		modCount++;
		return true;
	}

	@Override
	public boolean removeDouble(double x, double precision) {
		for (int i = 0; i < size; i++)
			if (eq(contents[i], x, precision)) {
				uncheckedRemove(i);
				modCount++;
				return true;
			}

		return false;
	}

	@Override
	public boolean removeDoubleExactly(double x) {
		for (int i = 0; i < size; i++)
			if (contents[i] == x) {
				uncheckedRemove(i);
				modCount++;
				return true;
			}

		return false;
	}

	@Override
	public boolean containsDoubleExactly(double x) {
		for (int i = 0; i < size; i++)
			if (contents[i] == x)
				return true;

		return false;
	}

	@Override
	public boolean removeAllDoubles(double[] xs, double precision) {
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (Arrayz.contains(xs, contents[i], precision)) {
				uncheckedRemove(i--);
				modified = true;
			}

		if (modified)
			modCount++;
		return modified;
	}

	@Override
	public boolean removeAllDoublesExactly(double... xs) {
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (Arrayz.containsExactly(xs, contents[i])) {
				uncheckedRemove(i--);
				modified = true;
			}

		if (modified)
			modCount++;
		return modified;
	}

	@Override
	public boolean retainAllDoubles(double[] xs, double precision) {
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (!Arrayz.contains(xs, contents[i], precision)) {
				uncheckedRemove(i--);
				modified = true;
			}

		if (modified)
			modCount++;
		return modified;
	}

	@Override
	public boolean retainAllDoublesExactly(double... xs) {
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (!Arrayz.containsExactly(xs, contents[i])) {
				uncheckedRemove(i--);
				modified = true;
			}

		if (modified)
			modCount++;
		return modified;
	}

	@Override
	public boolean removeDoublesIf(DoublePredicate filter) {
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (filter.test(contents[i])) {
				uncheckedRemove(i--);
				modified = true;
			}

		if (modified)
			modCount++;
		return modified;
	}

	@Override
	public void forEachDouble(DoubleConsumer consumer) {
		for (int i = 0; i < size; i++)
			consumer.accept(contents[i]);
	}

	private void growIfNecessaryBy(int grow) {
		int newSize = size + grow;
		if (newSize > contents.length) {
			int newCapacity = newSize + (newSize >> 1);
			double[] copy = new double[newCapacity];
			System.arraycopy(contents, 0, copy, 0, size);
			contents = copy;
		}
	}

	private void uncheckedAdd(int index, double x) {
		growIfNecessaryBy(1);
		System.arraycopy(contents, index, contents, index + 1, size++ - index);
		contents[index] = x;
	}

	protected void uncheckedAdd(int index, DoubleIterable xs, int xsSize) {
		if (xs instanceof ArrayDoubleList) {
			System.arraycopy(((ArrayDoubleList) xs).contents, 0, contents, index, xsSize);
		} else {
			DoubleIterator iterator = xs.iterator();
			for (int i = 0; i < xsSize; i++)
				contents[i + index] = iterator.nextDouble();
		}
	}

	private void uncheckedRemove(int index) {
		System.arraycopy(contents, index + 1, contents, index, size-- - index - 1);
	}

	private void rangeCheck(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index: " + index + " size: " + size);
	}

	private void rangeCheckForAdd(int index) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("index: " + index + " size: " + size);
	}

	private class ListIter implements DoubleListIterator {
		private int nextIndex;
		private int currentIndex;
		private final int from;
		private int to;
		private boolean addOrRemove;
		private boolean nextOrPrevious;

		private int expectedModCount = modCount;

		private ListIter(int index) {
			this(index, 0, size);
		}

		private ListIter(int index, int from, int to) {
			if (index < 0)
				throw new ArrayIndexOutOfBoundsException(index);
			if (index > to - from)
				throw new ArrayIndexOutOfBoundsException(index);
			this.nextIndex = index;
			this.currentIndex = index - 1;
			this.from = from;
			this.to = to;
		}

		@Override
		public boolean hasNext() {
			return nextIndex < to - from;
		}

		@Override
		public double nextDouble() {
			checkForCoModification();
			if (!hasNext())
				throw new NoSuchElementException();
			addOrRemove = false;
			nextOrPrevious = true;
			return contents[(currentIndex = nextIndex++) + from];
		}

		@Override
		public boolean hasPrevious() {
			return nextIndex > 0;
		}

		@Override
		public double previousDouble() {
			checkForCoModification();
			if (!hasPrevious())
				throw new NoSuchElementException();
			addOrRemove = false;
			nextOrPrevious = true;
			return contents[(currentIndex = --nextIndex) + from];
		}

		@Override
		public int nextIndex() {
			return nextIndex;
		}

		@Override
		public int previousIndex() {
			return nextIndex - 1;
		}

		@Override
		public void remove() {
			checkForCoModification();
			if (addOrRemove)
				throw new IllegalStateException("add() or remove() called");
			if (!nextOrPrevious)
				throw new IllegalStateException("nextDouble() or previousDouble() not called");

			uncheckedRemove((nextIndex = currentIndex--) + from);

			addOrRemove = true;
			to--;
			modCount++;
			expectedModCount++;
		}

		@Override
		public void set(double x) {
			checkForCoModification();
			if (addOrRemove)
				throw new IllegalStateException("add() or remove() called");
			if (!nextOrPrevious)
				throw new IllegalStateException("nextDouble() or previousDouble() not called");

			contents[currentIndex + from] = x;

			modCount++;
			expectedModCount++;
		}

		@Override
		public void add(double x) {
			checkForCoModification();
			uncheckedAdd((currentIndex = nextIndex++) + from, x);

			addOrRemove = true;
			to++;
			modCount++;
			expectedModCount++;
		}

		private void checkForCoModification() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
		}
	}

	private class SubList implements DoubleList {
		private int from;
		private int to;

		public SubList(int from, int to) {
			if (from < 0)
				throw new ArrayIndexOutOfBoundsException(from);
			if (to > size)
				throw new ArrayIndexOutOfBoundsException(to);
			this.from = from;
			this.to = to;
		}

		@Override
		public DoubleIterator iterator() {
			return listIterator();
		}

		@Override
		public DoubleListIterator listIterator(int index) {
			return new ArrayDoubleList.ListIter(index, from, to) {
				@Override
				public void add(double x) {
					super.add(x);
					ArrayDoubleList.SubList.this.to++;
				}

				@Override
				public void remove() {
					super.remove();
					ArrayDoubleList.SubList.this.to--;
				}
			};
		}

		public int size() {
			return to - from;
		}
	}
}
