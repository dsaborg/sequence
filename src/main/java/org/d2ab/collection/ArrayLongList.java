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

import org.d2ab.collection.iterator.LongIterator;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;

/**
 * An {@link LongList} backed by a long-array, supporting all {@link LongList}-methods by modifying and/or replacing the
 * underlying array.
 */
public class ArrayLongList implements LongList {
	private long[] array;
	private int offset;
	private int size;
	private int capacity;

	public ArrayLongList() {
		this(new long[10], 0, 0, 10);
	}

	public ArrayLongList(long... is) {
		this(is, is.length);
	}

	public ArrayLongList(long[] array, int size) {
		this(array, 0, size);
	}

	public ArrayLongList(long[] array, int offset, int size) {
		this(array, offset, size, size);
	}

	public ArrayLongList(long[] array, int offset, int size, int capacity) {
		if (offset > array.length || offset < 0)
			throw new IndexOutOfBoundsException("offset: " + offset + ", length: " + array.length);
		if (size > capacity || size < 0)
			throw new IllegalArgumentException("size: " + size + ", capacity: " + capacity);
		if (offset + capacity > array.length || capacity < 0)
			throw new IndexOutOfBoundsException("capacity: " + capacity + ", available: " + (array.length - offset));
		this.array = array;
		this.offset = offset;
		this.size = size;
		this.capacity = capacity;
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
		return Arrays.copyOfRange(array, offset, offset + size);
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
	public LongList subList(int fromIndex, int toIndex) {
		return new ArrayLongList(array, fromIndex, toIndex - fromIndex);
	}

	@Override
	public void replaceAllLongs(LongUnaryOperator operator) {
		for (int x = offset, ceiling = offset + size; x < ceiling; x++)
			array[x] = operator.applyAsLong(array[x]);
	}

	@Override
	public long getAt(int index) {
		rangeCheck(index);
		return array[offset + index];
	}

	@Override
	public long setAt(int index, long element) {
		rangeCheck(index);
		int pos = offset + index;
		long previous = array[pos];
		array[pos] = element;
		return previous;
	}

	@Override
	public void addAt(int index, long element) {
		rangeCheckForAdd(index);
		uncheckedAdd(index, element);
	}

	@Override
	public long removeAt(int index) {
		rangeCheck(index);
		long previous = array[offset + index];
		uncheckedRemove(offset + index);
		return previous;
	}

	@Override
	public int lastIndexOf(long i) {
		for (int x = offset + size - 1; x >= offset; x--)
			if (array[x] == i)
				return x;

		return -1;
	}

	@Override
	public int indexOf(long i) {
		for (int x = offset, ceiling = offset + size; x < ceiling; x++)
			if (array[x] == i)
				return x;

		return -1;
	}

	@Override
	public Spliterator.OfLong spliterator() {
		return Arrays.spliterator(array, offset, offset + size);
	}

	@Override
	public boolean addLong(long i) {
		growIfNecessary(1);
		array[offset + size++] = i;
		return true;
	}

	@Override
	public boolean addAll(long... is) {
		if (is.length == 0)
			return false;

		growIfNecessary(is.length);
		System.arraycopy(is, 0, array, offset + size, is.length);
		size += is.length;
		return true;
	}

	@Override
	public boolean addAllAt(int index, long... is) {
		if (is.length == 0)
			return false;

		rangeCheckForAdd(index);
		growIfNecessary(is.length);
		int pos = offset + index;
		System.arraycopy(array, pos, array, pos + is.length, size - index);
		System.arraycopy(is, 0, array, pos, is.length);
		size += is.length;
		return true;
	}

	@Override
	public boolean containsAll(long... is) {
		for (long i : is)
			if (!containsLong(i))
				return false;

		return true;
	}

	@Override
	public boolean removeLong(long i) {
		for (int x = offset, ceiling = offset + size; x < ceiling; x++)
			if (array[x] == i)
				return uncheckedRemove(x);

		return false;
	}

	@Override
	public boolean containsLong(long i) {
		for (int x = offset, ceiling = offset + size; x < ceiling; x++)
			if (array[x] == i)
				return true;

		return false;
	}

	@Override
	public boolean removeAll(long... is) {
		boolean modified = false;
		for (int x = offset, ceiling = offset + size; x < ceiling; x++)
			if (Arrayz.contains(is, array[x]))
				modified |= uncheckedRemove(x--);
		return modified;
	}

	@Override
	public boolean retainAll(long... is) {
		boolean modified = false;
		for (int x = offset, ceiling = offset + size; x < ceiling; x++)
			if (!Arrayz.contains(is, array[x]))
				modified |= uncheckedRemove(x--);
		return modified;
	}

	@Override
	public boolean removeLongsIf(LongPredicate filter) {
		boolean modified = false;
		for (int x = offset, ceiling = offset + size; x < ceiling; x++)
			if (!filter.test(array[x]))
				modified |= uncheckedRemove(x--);
		return modified;
	}

	@Override
	public void forEachLong(LongConsumer consumer) {
		for (int x = offset, ceiling = offset + size; x < ceiling; x++)
			consumer.accept(array[x]);
	}

	private void growIfNecessary(int grow) {
		int newSize = size + grow;
		if (newSize > capacity) {
			int newLength = newSize + ((newSize) >> 1);
			long[] copy = new long[newLength];
			System.arraycopy(array, offset, copy, 0, size);
			array = copy;
			offset = 0;
			capacity = newLength;
		}
	}

	private boolean uncheckedAdd(int index, long element) {
		growIfNecessary(1);
		int pos = offset + index;
		System.arraycopy(array, pos, array, pos + 1, size++ - (pos - offset));
		array[pos] = element;
		return true;
	}

	private boolean uncheckedRemove(int pos) {
		System.arraycopy(array, pos + 1, array, pos, size-- - (pos - offset) - 1);
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
			return array[offset + (currentIndex = nextIndex++)];
		}

		@Override
		public boolean hasPrevious() {
			return nextIndex > 0;
		}

		@Override
		public long previousLong() {
			addOrRemove = false;
			nextOrPrevious = true;
			return array[offset + (currentIndex = --nextIndex)];
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

			uncheckedRemove(offset + (nextIndex = currentIndex--));
			addOrRemove = true;
		}

		@Override
		public void set(long i) {
			if (addOrRemove)
				throw new IllegalStateException("add() or remove() called");
			if (!nextOrPrevious)
				throw new IllegalStateException("nextLong() or previousLong() not called");

			array[offset + currentIndex] = i;
		}

		@Override
		public void add(long i) {
			uncheckedAdd(currentIndex = nextIndex++, i);
			addOrRemove = true;
		}
	}
}
