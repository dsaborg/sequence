package org.d2ab.collection.ints;

import org.d2ab.iterator.ints.ArrayIntIterator;

import java.util.NoSuchElementException;

public class ArrayIntListIterator extends ArrayIntIterator implements IntListIterator {
	public ArrayIntListIterator(int... xs) {
		this(0, xs);
	}

	public ArrayIntListIterator(int index, int... xs) {
		super(xs);
		if (index < 0 || index > xs.length)
			throw new IndexOutOfBoundsException("size: " + xs.length + " index: " + index);
		this.index = index;
	}

	@Override
	public boolean hasPrevious() {
		return index > 0;
	}

	@Override
	public int previousInt() {
		if (!hasPrevious())
			throw new NoSuchElementException();

		return array[offset + --index];
	}

	@Override
	public int nextIndex() {
		return index;
	}

	@Override
	public int previousIndex() {
		return index - 1;
	}
}
