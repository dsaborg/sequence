package org.d2ab.collection.longs;

import org.d2ab.iterator.longs.ArrayLongIterator;

import java.util.NoSuchElementException;

public class ArrayLongListIterator extends ArrayLongIterator implements LongListIterator {
	public ArrayLongListIterator(long... xs) {
		super(xs);
	}

	public ArrayLongListIterator(int index, long... xs) {
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
	public long previousLong() {
		if (!hasPrevious())
			throw new NoSuchElementException();

		return values[offset + --index];
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
