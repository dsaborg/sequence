package org.d2ab.iterator;

import org.d2ab.collection.Arrayz;
import org.d2ab.collection.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * An array iterator that returns the contents of an array in random order by rearranging it in place. Produces
 * elements in the same order as {@link Arrayz#shuffle(Object[], Random) and {@link Lists#shuffle(List, Random)}}.
 *
 * @see Arrayz#shuffle(Object[], Random)
 * @see Lists#shuffle(List, Random)
 */
public class ShufflingArrayIterator<T> implements Iterator<T> {
	private static final Random SHARED_RANDOM = new Random();

	private final T[] array;
	private final Random random;
	private int cursor;

	public ShufflingArrayIterator(T[] array) {
		this(array, SHARED_RANDOM);
	}

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
		int length = array.length;
		if (cursor >= length)
			throw new NoSuchElementException();

		if (cursor == length - 1)
			return array[cursor++];

		int i = random.nextInt(length - cursor) + cursor;
		if (i == cursor)
			return array[cursor++];

		T result = array[i];
		array[i] = array[cursor++];
		return result;
	}
}
