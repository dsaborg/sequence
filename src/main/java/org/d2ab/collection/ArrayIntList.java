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

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

/**
 * An {@link IntList} backed by an int-array, supporting all {@link IntList}-methods by modifying and/or replacing the
 * underlying array.
 */
public class ArrayIntList implements IntList {
	private int[] contents;
	private int size;

	public static ArrayIntList of(int... contents) {
		return new ArrayIntList(contents);
	}

	public ArrayIntList() {
		this(10);
	}

	public ArrayIntList(int capacity) {
		this.contents = new int[capacity];
	}

	/**
	 * Private to avoid conflict with standard int-taking capacity constructor.
	 * Use {@link #of(int...)} for public access.
	 *
	 * @see #ArrayIntList(int)
	 * @see #of(int...)
	 */
	private ArrayIntList(int... contents) {
		this.contents = Arrays.copyOf(contents, contents.length);
		this.size = contents.length;
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
	public IntList subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void replaceAllInts(IntUnaryOperator operator) {
		for (int x = 0; x < size; x++)
			contents[x] = operator.applyAsInt(contents[x]);
	}

	@Override
	public int getAt(int index) {
		rangeCheck(index);
		return contents[index];
	}

	@Override
	public int setAt(int index, int element) {
		rangeCheck(index);
		int previous = contents[index];
		contents[index] = element;
		return previous;
	}

	@Override
	public void addAt(int index, int element) {
		rangeCheckForAdd(index);
		uncheckedAdd(index, element);
	}

	@Override
	public int removeAt(int index) {
		rangeCheck(index);
		int previous = contents[index];
		uncheckedRemove(index);
		return previous;
	}

	@Override
	public int lastIndexOf(int i) {
		for (int x = size - 1; x >= 0; x--)
			if (contents[x] == i)
				return x;

		return -1;
	}

	@Override
	public int indexOf(int i) {
		for (int x = 0; x < size; x++)
			if (contents[x] == i)
				return x;

		return -1;
	}

	@Override
	public Spliterator.OfInt spliterator() {
		return Arrays.spliterator(contents, 0, size);
	}

	@Override
	public boolean addInt(int i) {
		growIfNecessary(1);
		contents[size++] = i;
		return true;
	}

	@Override
	public boolean addAll(int... is) {
		if (is.length == 0)
			return false;

		growIfNecessary(is.length);
		System.arraycopy(is, 0, contents, size, is.length);
		size += is.length;
		return true;
	}

	@Override
	public boolean addAllAt(int index, int... is) {
		if (is.length == 0)
			return false;

		rangeCheckForAdd(index);
		growIfNecessary(is.length);
		System.arraycopy(contents, index, contents, index + is.length, size - index);
		System.arraycopy(is, 0, contents, index, is.length);
		size += is.length;
		return true;
	}

	public boolean addAllAt(int index, IntCollection c) {
		if (c.size() == 0)
			return false;

		rangeCheckForAdd(index);
		growIfNecessary(c.size());
		System.arraycopy(contents, index, contents, index + c.size(), size - index);
		IntIterator iterator = c.iterator();
		for (int x = index; x < c.size(); x++)
			contents[x] = iterator.nextInt();
		size += c.size();
		return true;
	}

	@Override
	public boolean containsAll(int... is) {
		for (int i : is)
			if (!containsInt(i))
				return false;

		return true;
	}

	@Override
	public boolean removeInt(int i) {
		for (int x = 0; x < size; x++)
			if (contents[x] == i)
				return uncheckedRemove(x);

		return false;
	}

	@Override
	public boolean containsInt(int i) {
		for (int x = 0; x < size; x++)
			if (contents[x] == i)
				return true;

		return false;
	}

	@Override
	public boolean removeAll(int... is) {
		boolean modified = false;
		for (int x = 0; x < size; x++)
			if (Arrayz.contains(is, contents[x]))
				modified |= uncheckedRemove(x--);
		return modified;
	}

	@Override
	public boolean retainAll(int... is) {
		boolean modified = false;
		for (int x = 0; x < size; x++)
			if (!Arrayz.contains(is, contents[x]))
				modified |= uncheckedRemove(x--);
		return modified;
	}

	@Override
	public boolean removeIntsIf(IntPredicate filter) {
		boolean modified = false;
		for (int x = 0; x < size; x++)
			if (filter.test(contents[x]))
				modified |= uncheckedRemove(x--);
		return modified;
	}

	@Override
	public void forEachInt(IntConsumer consumer) {
		for (int x = 0; x < size; x++)
			consumer.accept(contents[x]);
	}

	private void growIfNecessary(int grow) {
		int newSize = size + grow;
		if (newSize > contents.length) {
			int newLength = newSize + ((newSize) >> 1);
			int[] copy = new int[newLength];
			System.arraycopy(contents, 0, copy, 0, size);
			contents = copy;
		}
	}

	private boolean uncheckedAdd(int index, int element) {
		growIfNecessary(1);
		System.arraycopy(contents, index, contents, index + 1, size++ - index);
		contents[index] = element;
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

	private class ListIter implements IntListIterator {
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
		public int nextInt() {
			addOrRemove = false;
			nextOrPrevious = true;
			return contents[currentIndex = nextIndex++];
		}

		@Override
		public boolean hasPrevious() {
			return nextIndex > 0;
		}

		@Override
		public int previousInt() {
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
				throw new IllegalStateException("nextInt() or previousInt() not called");

			uncheckedRemove(nextIndex = currentIndex--);
			addOrRemove = true;
		}

		@Override
		public void set(int i) {
			if (addOrRemove)
				throw new IllegalStateException("add() or remove() called");
			if (!nextOrPrevious)
				throw new IllegalStateException("nextInt() or previousInt() not called");

			contents[currentIndex] = i;
		}

		@Override
		public void add(int i) {
			uncheckedAdd(currentIndex = nextIndex++, i);
			addOrRemove = true;
		}
	}
}
