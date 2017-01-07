package org.d2ab.collection.doubles;

import org.d2ab.iterator.doubles.ArrayDoubleIterator;

import java.util.NoSuchElementException;

public class ArrayDoubleListIterator extends ArrayDoubleIterator implements DoubleListIterator {
	public ArrayDoubleListIterator(double... values) {
		super(values);
	}

	@Override
	public boolean hasPrevious() {
		return index > 0;
	}

	@Override
	public double previousDouble() {
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
