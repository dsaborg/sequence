package org.d2ab.sequence;

import java.util.*;

public class SortingIterator<T> implements Iterator<T> {
	private Iterator<T> iterator;
	private Comparator<? super T> comparator;
	private Iterator<T> sortedIterator;

	public SortingIterator(Iterator<T> iterator) {
		this(iterator, (Comparator<? super T>) Comparator.naturalOrder());
	}

	public SortingIterator(Iterator<T> iterator, Comparator<? super T> comparator) {
		this.iterator = iterator;
		this.comparator = comparator;
	}

	@Override
	public boolean hasNext() {
		if (sortedIterator == null) {
			List<T> elements = new ArrayList<T>();
			while (iterator.hasNext())
				elements.add(iterator.next());
			elements.sort(comparator);
			sortedIterator = elements.iterator();
		}
		return sortedIterator.hasNext();
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return sortedIterator.next();
	}
}
