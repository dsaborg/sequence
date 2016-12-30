package org.d2ab.collection.ints;

import org.d2ab.iterator.ints.ArrayIntIterator;

import java.util.NoSuchElementException;

public class ArrayIntListIterator extends ArrayIntIterator implements IntListIterator {
	public ArrayIntListIterator(int... xs) {
		super(xs);
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
