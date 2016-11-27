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

import org.d2ab.collection.Arrayz;
import org.d2ab.iterator.longs.LongIterator;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;

/**
 * A {@link LongList} backed by a long-array, supporting all {@link LongList}-methods by modifying and/or replacing the
 * underlying array.
 */
public class ArrayLongList implements LongList {
	private long[] contents;
	private int size;

	/**
	 * @return a new mutable {@code ArrayLongList} initialized with a copy of the given contents.
	 *
	 * @deprecated Use {@link #create(long...)} instead.
	 */
	@Deprecated
	public static ArrayLongList of(long... xs) {
		return create(xs);
	}

	/**
	 * @return a new empty mutable {@code ArrayLongList}.
	 *
	 * @see #withCapacity(int)
	 *
	 * @since 2.1
	 */
	public static ArrayLongList create() {
		return new ArrayLongList();
	}

	/**
	 * @return a new mutable {@code ArrayLongList} initialized with a copy of the given contents.
	 *
	 * @see #ArrayLongList(long[])
	 * @see #ArrayLongList(LongCollection)
	 *
	 * @since 2.1
	 */
	public static ArrayLongList create(long... xs) {
		return new ArrayLongList(xs);
	}

	/**
	 * @return a new mutable {@code ArrayLongList} with the given initial capacity.
	 *
	 * @since 2.1
	 */
	public static ArrayLongList withCapacity(int capacity) {
		return new ArrayLongList(capacity);
	}

	/**
	 * Create a new mutable {@code ArrayLongList}.
	 *
	 * @since 2.0
	 *
	 * @deprecated Use {@link #create()} instead.
	 */
	@Deprecated
	public ArrayLongList() {
		this(10);
	}

	/**
	 * Create a new mutable {@code ArrayLongList} with the given initial capacity.
	 *
	 * @since 2.0
	 *
	 * @deprecated Use {@link #withCapacity(int)} instead.
	 */
	@Deprecated
	public ArrayLongList(int capacity) {
		this.contents = new long[capacity];
	}

	public ArrayLongList(LongCollection xs) {
		this();
		addAllLongs(xs);
	}

	public ArrayLongList(long[] xs) {
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
	public long[] toLongArray() {
		return Arrays.copyOfRange(contents, 0, size);
	}

	@Override
	public LongIterator iterator() {
		return listIterator();
	}

	@Override
	public LongListIterator listIterator(int index) {
		rangeCheckForAdd(index);
		return new ListIter(index);
	}

	@Override
	public void sortLongs() {
		Arrays.sort(contents, 0, size);
	}

	@Override
	public int binarySearch(long x) {
		return Arrays.binarySearch(contents, 0, size, x);
	}

	@Override
	public void replaceAllLongs(LongUnaryOperator operator) {
		for (int i = 0; i < size; i++)
			contents[i] = operator.applyAsLong(contents[i]);
	}

	@Override
	public long getLong(int index) {
		rangeCheck(index);
		return contents[index];
	}

	@Override
	public long setLong(int index, long x) {
		rangeCheck(index);
		long previous = contents[index];
		contents[index] = x;
		return previous;
	}

	@Override
	public void addLongAt(int index, long x) {
		rangeCheckForAdd(index);
		uncheckedAdd(index, x);
	}

	@Override
	public long removeLongAt(int index) {
		rangeCheck(index);
		long previous = contents[index];
		uncheckedRemove(index);
		return previous;
	}

	@Override
	public int lastIndexOfLong(long x) {
		for (int i = size - 1; i >= 0; i--)
			if (contents[i] == x)
				return i;

		return -1;
	}

	@Override
	public int indexOfLong(long x) {
		for (int i = 0; i < size; i++)
			if (contents[i] == x)
				return i;

		return -1;
	}

	@Override
	public Spliterator.OfLong spliterator() {
		return Arrays.spliterator(contents, 0, size);
	}

	@Override
	public boolean addLong(long x) {
		growIfNecessaryBy(1);
		contents[size++] = x;
		return true;
	}

	@Override
	public boolean addAllLongs(long... xs) {
		if (xs.length == 0)
			return false;

		growIfNecessaryBy(xs.length);
		System.arraycopy(xs, 0, contents, size, xs.length);
		size += xs.length;
		return true;
	}

	@Override
	public boolean addAllLongs(LongCollection xs) {
		if (xs.isEmpty())
			return false;

		if (xs instanceof ArrayLongList) {
			ArrayLongList axs = (ArrayLongList) xs;

			growIfNecessaryBy(axs.size);
			System.arraycopy(axs.contents, 0, contents, size, axs.size);
			size += axs.size;

			return true;
		} else {
			xs.forEachLong(this::addLong);
			return true;
		}
	}

	@Override
	public boolean addAllLongsAt(int index, long... xs) {
		if (xs.length == 0)
			return false;

		rangeCheckForAdd(index);
		growIfNecessaryBy(xs.length);
		System.arraycopy(contents, index, contents, index + xs.length, size - index);
		System.arraycopy(xs, 0, contents, index, xs.length);
		size += xs.length;
		return true;
	}

	@Override
	public boolean addAllLongsAt(int index, LongCollection xs) {
		if (xs.size() == 0)
			return false;

		rangeCheckForAdd(index);
		growIfNecessaryBy(xs.size());
		System.arraycopy(contents, index, contents, index + xs.size(), size - index);

		if (xs instanceof ArrayLongList) {
			ArrayLongList il = (ArrayLongList) xs;
			System.arraycopy(il.contents, 0, contents, index, il.size);
		} else {
			LongIterator iterator = xs.iterator();
			for (int i = index; i < xs.size(); i++)
				contents[i] = iterator.nextLong();
		}

		size += xs.size();

		return true;
	}

	@Override
	public boolean containsAllLongs(long... xs) {
		for (long x : xs)
			if (!containsLong(x))
				return false;

		return true;
	}

	@Override
	public boolean removeLong(long x) {
		for (int i = 0; i < size; i++)
			if (contents[i] == x)
				return uncheckedRemove(i);

		return false;
	}

	@Override
	public boolean containsLong(long x) {
		for (int i = 0; i < size; i++)
			if (contents[i] == x)
				return true;

		return false;
	}

	@Override
	public boolean removeAllLongs(long... xs) {
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (Arrayz.contains(xs, contents[i]))
				modified |= uncheckedRemove(i--);
		return modified;
	}

	@Override
	public boolean retainAllLongs(long... xs) {
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (!Arrayz.contains(xs, contents[i]))
				modified |= uncheckedRemove(i--);
		return modified;
	}

	@Override
	public boolean removeLongsIf(LongPredicate filter) {
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (filter.test(contents[i]))
				modified |= uncheckedRemove(i--);
		return modified;
	}

	@Override
	public void forEachLong(LongConsumer consumer) {
		for (int i = 0; i < size; i++)
			consumer.accept(contents[i]);
	}

	private void growIfNecessaryBy(int grow) {
		int newSize = size + grow;
		if (newSize > contents.length) {
			int newCapacity = newSize + (newSize >> 1);
			long[] copy = new long[newCapacity];
			System.arraycopy(contents, 0, copy, 0, size);
			contents = copy;
		}
	}

	private boolean uncheckedAdd(int index, long x) {
		growIfNecessaryBy(1);
		System.arraycopy(contents, index, contents, index + 1, size++ - index);
		contents[index] = x;
		return true;
	}

	private boolean uncheckedRemove(int index) {
		System.arraycopy(contents, index + 1, contents, index, size-- - index - 1);
		return true;
	}

	private void rangeCheck(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index: " + index + " size: " + size);
	}

	private void rangeCheckForAdd(int index) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("index: " + index + " size: " + size);
	}

	private class ListIter implements LongListIterator {
		protected int nextIndex;
		protected int currentIndex;
		protected boolean addOrRemove;
		protected boolean nextOrPrevious;

		private ListIter(int index) {
			this.nextIndex = index;
			this.currentIndex = index - 1;
		}

		@Override
		public boolean hasNext() {
			return nextIndex < size;
		}

		@Override
		public long nextLong() {
			addOrRemove = false;
			nextOrPrevious = true;
			return contents[currentIndex = nextIndex++];
		}

		@Override
		public boolean hasPrevious() {
			return nextIndex > 0;
		}

		@Override
		public long previousLong() {
			addOrRemove = false;
			nextOrPrevious = true;
			return contents[currentIndex = --nextIndex];
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
			if (addOrRemove)
				throw new IllegalStateException("add() or remove() called");
			if (!nextOrPrevious)
				throw new IllegalStateException("nextLong() or previousLong() not called");

			uncheckedRemove(nextIndex = currentIndex--);
			addOrRemove = true;
		}

		@Override
		public void set(long x) {
			if (addOrRemove)
				throw new IllegalStateException("add() or remove() called");
			if (!nextOrPrevious)
				throw new IllegalStateException("nextLong() or previousLong() not called");

			contents[currentIndex] = x;
		}

		@Override
		public void add(long x) {
			uncheckedAdd(currentIndex = nextIndex++, x);
			addOrRemove = true;
		}
	}
}
