package org.d2ab.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * An array iterator that returns the contents of an array in random order by rearranging it in place.
 */
public class ShufflingArrayIterator<T> implements Iterator<T> {
	private final T[] array;
	private final Random random;
	private int cursor;

	public ShufflingArrayIterator(T[] array, Random random) {
		this.array = array;
		this.random = random;
	}

	@Override
	public boolean hasNext() {
		return cursor < array.length;
	}

	@Override
	public T next() {
		if (cursor >= array.length)
			throw new NoSuchElementException();

		int randomIndex = random.nextInt(array.length - cursor) + cursor;
		if (randomIndex != cursor) {
			T temp = array[cursor];
			array[cursor] = array[randomIndex];
			array[randomIndex] = temp;
		}

		return array[cursor++];
	}
}
