package org.da2ab.sequence;

import java.util.Iterator;
import java.util.function.Function;

public class MappingIterator<T, U> implements Iterator<U> {
	private final Iterator<T> iterator;
	private final Function<? super T, ? extends U> mapper;

	public MappingIterator(Iterator<T> iterator, Function<? super T, ? extends U> mapper) {
		this.iterator = iterator;
		this.mapper = mapper;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public U next() {
		return mapper.apply(iterator.next());
	}
}
