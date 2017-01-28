package org.d2ab.sequence;

import org.d2ab.collection.SizedIterable;

import java.util.Iterator;
import java.util.function.Function;

/**
 * A {@link Sequence} created from another sequence through a mapping function, which has the same size as the original
 * {@link Sequence}.
 */
public class EquivalentSizeSequence<T, U> implements Sequence<U> {
	private final SizedIterable<T> original;
	private final Function<Iterator<T>, Iterator<U>> converter;

	public EquivalentSizeSequence(SizedIterable<T> original, Function<Iterator<T>, Iterator<U>> converter) {
		this.original = original;
		this.converter = converter;
	}

	@Override
	public Iterator<U> iterator() {
		return converter.apply(original.iterator());
	}

	@Override
	public int size() {
		return original.size();
	}

	@Override
	public SizeType sizeType() {
		return original.sizeType();
	}

	@Override
	public boolean isEmpty() {
		return original.isEmpty();
	}
}
