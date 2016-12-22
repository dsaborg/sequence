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

import org.d2ab.collection.Arrayz;
import org.d2ab.iterator.ints.IntIterator;

import java.util.*;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

/**
 * An {@link IntList} backed by an int-array, supporting all {@link IntList}-methods by modifying and/or replacing the
 * underlying array.
 */
public class ArrayIntList extends IntList.Base implements RandomAccess {
	private int[] contents;
	private int size;

	private int modCount;

	/**
	 * Create a new empty mutable {@code ArrayIntList}. When possible, it's preferred to use {@link IntList#create()}
	 * instead.
	 *
	 * @return a new empty mutable {@code ArrayIntList}.
	 *
	 * @see IntList#create()
	 * @see #withCapacity(int)
	 * @since 2.1
	 */
	public static ArrayIntList create() {
		return new ArrayIntList();
	}

	/**
	 * Create a new mutable {@code ArrayIntList} initialized with a copy of the given contents. When possible, it's
	 * preferred to use {@link IntList#create(int...)} instead.
	 *
	 * @return a new mutable {@code ArrayIntList} initialized with a copy of the given contents.
	 *
	 * @see IntList#create(int...)
	 * @see #ArrayIntList(IntCollection)
	 * @since 2.1
	 */
	public static ArrayIntList create(int... xs) {
		return new ArrayIntList(xs);
	}

	/**
	 * @return a new mutable {@code ArrayIntList} with the given initial capacity.
	 *
	 * @since 2.1
	 */
	public static ArrayIntList withCapacity(int capacity) {
		return new ArrayIntList(capacity);
	}

	/**
	 * Create a new mutable {@code ArrayIntList}.
	 *
	 * @since 2.0
	 */
	public ArrayIntList() {
		this(10);
	}

	/**
	 * Create a new mutable {@code ArrayIntList} with the given initial capacity.
	 *
	 * @since 2.0
	 */
	private ArrayIntList(int capacity) {
		this.contents = new int[capacity];
	}

	public ArrayIntList(IntCollection xs) {
		this();
		addAllInts(xs);
	}

	public ArrayIntList(int[] xs) {
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
	public int[] toIntArray() {
		return Arrays.copyOfRange(contents, 0, size);
	}

	@Override
	public IntIterator iterator() {
		return listIterator();
	}

	@Override
	public IntListIterator listIterator(int index) {
		rangeCheckForAdd(index);
		return new ListIter(index);
	}

	@Override
	public void sortInts() {
		Arrays.sort(contents, 0, size);
	}

	@Override
	public int binarySearch(int x) {
		return Arrays.binarySearch(contents, 0, size, x);
	}

	@Override
	public IntList subList(int from, int to) {
		return new SubList(from, to);
	}

	@Override
	public void replaceAllInts(IntUnaryOperator operator) {
		for (int i = 0; i < size; i++)
			contents[i] = operator.applyAsInt(contents[i]);

		modCount++;
	}

	@Override
	public int getInt(int index) {
		rangeCheck(index);
		return contents[index];
	}

	@Override
	public int setInt(int index, int x) {
		rangeCheck(index);
		int previous = contents[index];
		contents[index] = x;

		modCount++;
		return previous;
	}

	@Override
	public void addIntAt(int index, int x) {
		rangeCheckForAdd(index);
		uncheckedAdd(index, x);

		modCount++;
	}

	@Override
	public int removeIntAt(int index) {
		rangeCheck(index);
		int previous = contents[index];
		uncheckedRemove(index);

		modCount++;
		return previous;
	}

	@Override
	public int lastIndexOfInt(int x) {
		for (int i = size - 1; i >= 0; i--)
			if (contents[i] == x)
				return i;

		return -1;
	}

	@Override
	public int indexOfInt(int x) {
		for (int i = 0; i < size; i++)
			if (contents[i] == x)
				return i;

		return -1;
	}

	@Override
	public Spliterator.OfInt spliterator() {
		return Arrays.spliterator(contents, 0, size);
	}

	@Override
	public boolean addInt(int x) {
		growIfNecessaryBy(1);
		contents[size++] = x;

		modCount++;
		return true;
	}

	@Override
	public boolean addAllInts(int... xs) {
		if (xs.length == 0)
			return false;

		growIfNecessaryBy(xs.length);
		System.arraycopy(xs, 0, contents, size, xs.length);
		size += xs.length;

		modCount++;
		return true;
	}

	@Override
	public boolean addAllInts(IntCollection xs) {
		int xsSize = xs.size();
		if (xsSize == 0)
			return false;

		growIfNecessaryBy(xsSize);
		uncheckedAdd(size, xs, xsSize);

		modCount++;
		return true;
	}

	@Override
	public boolean addAllIntsAt(int index, int... xs) {
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
	public boolean addAllIntsAt(int index, IntCollection xs) {
		int xsSize = xs.size();
		if (xsSize == 0)
			return false;

		rangeCheckForAdd(index);
		growIfNecessaryBy(xsSize);
		System.arraycopy(contents, index, contents, index + xsSize, size - index);
		uncheckedAdd(index, xs, xsSize);

		modCount++;
		return true;
	}

	@Override
	public boolean containsAllInts(int... xs) {
		for (int x : xs)
			if (!containsInt(x))
				return false;

		return true;
	}

	@Override
	public boolean removeInt(int x) {
		for (int i = 0; i < size; i++)
			if (contents[i] == x) {
				uncheckedRemove(i);
				modCount++;
				return true;
			}

		return false;
	}

	@Override
	public boolean containsInt(int x) {
		for (int i = 0; i < size; i++)
			if (contents[i] == x)
				return true;

		return false;
	}

	@Override
	public boolean removeAllInts(int... xs) {
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (Arrayz.contains(xs, contents[i])) {
				uncheckedRemove(i--);
				modified = true;
			}

		if (modified)
			modCount++;
		return modified;
	}

	@Override
	public boolean retainAllInts(int... xs) {
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (!Arrayz.contains(xs, contents[i])) {
				uncheckedRemove(i--);
				modified = true;
			}

		if (modified)
			modCount++;
		return modified;
	}

	@Override
	public boolean removeIntsIf(IntPredicate filter) {
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
	public void forEachInt(IntConsumer consumer) {
		for (int i = 0; i < size; i++)
			consumer.accept(contents[i]);
	}

	private void growIfNecessaryBy(int grow) {
		int newSize = size + grow;
		if (newSize > contents.length) {
			int newCapacity = newSize + (newSize >> 1);
			int[] copy = new int[newCapacity];
			System.arraycopy(contents, 0, copy, 0, size);
			contents = copy;
		}
	}

	private void uncheckedAdd(int index, int x) {
		growIfNecessaryBy(1);
		System.arraycopy(contents, index, contents, index + 1, size++ - index);
		contents[index] = x;
	}

	protected void uncheckedAdd(int index, IntIterable xs, int xsSize) {
		if (xs instanceof ArrayIntList) {
			System.arraycopy(((ArrayIntList) xs).contents, 0, contents, index, xsSize);
		} else {
			IntIterator iterator = xs.iterator();
			for (int i = 0; i < xsSize; i++)
				contents[i + index] = iterator.nextInt();
		}
		size += xsSize;
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

	private class ListIter implements IntListIterator {
		private int nextIndex;
		private int currentIndex;
		private final int from;
		private int to;
		private boolean addOrRemove;
		private boolean nextOrPrevious;

		private int expectedModCount = modCount;

		public ListIter(int index) {
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
		public int nextInt() {
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
		public int previousInt() {
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
				throw new IllegalStateException("nextInt() or previousInt() not called");

			uncheckedRemove((nextIndex = currentIndex--) + from);

			addOrRemove = true;
			to--;
			modCount++;
			expectedModCount++;
		}

		@Override
		public void set(int x) {
			checkForCoModification();
			if (addOrRemove)
				throw new IllegalStateException("add() or remove() called");
			if (!nextOrPrevious)
				throw new IllegalStateException("nextInt() or previousInt() not called");

			contents[currentIndex + from] = x;

			modCount++;
			expectedModCount++;
		}

		@Override
		public void add(int x) {
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

	private class SubList implements IntList {
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
		public IntIterator iterator() {
			return listIterator();
		}

		@Override
		public IntListIterator listIterator(int index) {
			return new ListIter(index, from, to) {
				@Override
				public void add(int x) {
					super.add(x);
					ArrayIntList.SubList.this.to++;
				}

				@Override
				public void remove() {
					super.remove();
					ArrayIntList.SubList.this.to--;
				}
			};
		}

		public int size() {
			return to - from;
		}
	}
}
