package org.d2ab.sequence;

import java.util.*;

public class PartitioningIterator<T> implements Iterator<List<T>> {
	private final Iterator<T> iterator;
	private final int window;
	private Deque<T> partition = new LinkedList<>();

	public PartitioningIterator(Iterator<T> iterator, int window) {
		this.iterator = iterator;
		this.window = window;
	}

	@Override
	public boolean hasNext() {
		while (partition.size() < window && iterator.hasNext())
			partition.add(iterator.next());

		return partition.size() == window;
	}

	@Override
	public List<T> next() {
		if (!hasNext())
			throw new NoSuchElementException();

		List<T> result = new ArrayList<>(partition);
		partition.removeFirst();
		return result;
	}
}
