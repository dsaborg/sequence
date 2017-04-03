package org.d2ab.sequence;

import org.d2ab.iterator.ShufflingArrayIterator;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link Sequence} which provides a reverse view of another {@link Sequence}. Provides
 * optimizations for certain operations.
 */
class ShuffledSequence<T> extends ReorderedSequence<T> {
	private final Supplier<? extends Random> randomSupplier;

	ShuffledSequence(Sequence<T> original, Supplier<? extends Random> randomSupplier) {
		super(original);
		this.randomSupplier = requireNonNull(randomSupplier);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator() {
		return new ShufflingArrayIterator<>((T[]) parent.toArray(), randomSupplier.get());
	}

	@Override
	protected Sequence<T> withParent(Sequence<T> parent) {
		return new ShuffledSequence<>(parent, randomSupplier);
	}
}
