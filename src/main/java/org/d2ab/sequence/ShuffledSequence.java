package org.d2ab.sequence;

import org.d2ab.collection.Arrayz;
import org.d2ab.collection.Lists;
import org.d2ab.iterator.ShufflingArrayIterator;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * An implementation of {@link Sequence} which provides a randomly shuffled of another {@link Sequence}. Provides
 * optimizations for certain operations. No guarantees are made as to the order of elements returned by the
 * {@link Sequence} except that each iteration will yield a new order and the ordering is as random as allowed by
 * {@link Random}.
 */
class ShuffledSequence<T> extends ReorderedSequence<T> {
	private static final Random SHARED_RANDOM = new Random();

	private final Random random;

	ShuffledSequence(Sequence<T> parent) {
		this(parent, SHARED_RANDOM);
	}

	public ShuffledSequence(Sequence<T> parent, Random random) {
		super(parent);
		this.random = random;
	}

	@Override
	protected Sequence<T> withParent(Sequence<T> parent) {
		return new ShuffledSequence<>(parent, random);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator() {
		return (Iterator<T>) new ShufflingArrayIterator<>(parent.toArray(), random);
	}

	@Override
	public Object[] toArray() {
		return Arrayz.shuffle(parent.toArray(), random);
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> constructor) {
		return Arrayz.shuffle(parent.toArray(constructor), random);
	}

	@Override
	public List<T> toList() {
		return Lists.shuffle(parent.toList(), random);
	}

	@Override
	public List<T> toList(Supplier<? extends List<T>> constructor) {
		return Lists.shuffle(parent.toList(constructor), random);
	}

	@Override
	public Optional<T> first() {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex == -1)
			return super.first();

		return parent.at(randomIndex);
	}

	@Override
	public Optional<T> last() {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex == -1)
			return super.last();

		return parent.at(randomIndex);
	}

	@Override
	public Optional<T> at(int index) {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex == -1)
			return super.at(index);

		return parent.at(randomIndex);
	}

	@Override
	public Optional<T> removeFirst() {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex == -1)
			return super.removeFirst();

		return parent.removeAt(randomIndex);
	}

	@Override
	public Optional<T> removeLast() {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex == -1)
			return super.removeLast();

		return parent.removeAt(randomIndex);
	}

	@Override
	public Optional<T> removeAt(int index) {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex == -1)
			return super.removeAt(index);

		return parent.removeAt(randomIndex);
	}

	private int randomIndexIfKnown() {
		int size = sizeIfKnown();
		if (size == -1)
			return -1;

		return random.nextInt(size);
	}
}
