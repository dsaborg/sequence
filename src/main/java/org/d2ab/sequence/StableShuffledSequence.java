package org.d2ab.sequence;

import org.d2ab.collection.Arrayz;
import org.d2ab.collection.Lists;
import org.d2ab.iterator.ShufflingArrayIterator;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link Sequence} which provides a randomly shuffled of another {@link Sequence}, based on a
 * {@link Random} instance supplied by a supplier, which is accessed on each iteration of the resulting
 * {@link Sequence}. Provides optimizations for certain operations.
 */
class StableShuffledSequence<T> extends ReorderedSequence<T> {
	private final Supplier<? extends Random> randomSupplier;

	StableShuffledSequence(Sequence<T> parent, Supplier<? extends Random> randomSupplier) {
		super(parent);
		this.randomSupplier = requireNonNull(randomSupplier);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator() {
		return (Iterator<T>) new ShufflingArrayIterator<>(toArray(), randomSupplier.get());
	}

	@Override
	protected Sequence<T> withParent(Sequence<T> parent) {
		return new StableShuffledSequence<>(parent, randomSupplier);
	}

	@Override
	public Object[] toArray() {
		return Arrayz.shuffle(parent.toArray(), randomSupplier.get());
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> constructor) {
		return Arrayz.shuffle(parent.toArray(constructor), randomSupplier.get());
	}

	@Override
	public List<T> toList() {
		return Lists.shuffle(parent.toList(), randomSupplier.get());
	}

	@Override
	public List<T> toList(Supplier<? extends List<T>> constructor) {
		return Lists.shuffle(parent.toList(constructor), randomSupplier.get());
	}
}
