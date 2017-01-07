package org.d2ab.collection.chars;

import org.d2ab.iterator.chars.ArrayCharIterator;

import java.util.NoSuchElementException;

public class ArrayCharListIterator extends ArrayCharIterator implements CharListIterator {
	public ArrayCharListIterator(char... values) {
		super(values);
	}

	@Override
	public boolean hasPrevious() {
		return index > 0;
	}

	@Override
	public char previousChar() {
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
