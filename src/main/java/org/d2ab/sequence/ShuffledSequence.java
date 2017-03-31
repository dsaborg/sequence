package org.d2ab.sequence;

import org.d2ab.collection.Lists;
import org.d2ab.iterator.Iterators;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
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

	@Override
	public Iterator<T> iterator() {
		return Iterators.unmodifiable(Lists.shuffle(original.toList(), randomSupplier.get()));
	}

	@Override
	public Optional<T> first() {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex == -1)
			return super.first();

		return original.at(randomIndex);
	}

	@Override
	public Optional<T> last() {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex == -1)
			return super.last();

		return original.at(randomIndex);
	}

	@Override
	public Optional<T> at(int index) {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex == -1)
			return super.at(index);

		return original.at(randomIndex);
	}

	@Override
	public Optional<T> removeFirst() {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex == -1)
			return super.removeFirst();

		return original.removeAt(randomIndex);
	}

	@Override
	public Optional<T> removeLast() {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex == -1)
			return super.removeLast();

		return original.removeAt(randomIndex);
	}

	@Override
	public Optional<T> removeAt(int index) {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex == -1)
			return super.removeAt(index);

		return original.removeAt(randomIndex);
	}

	private int randomIndexIfKnown() {
		int size = sizeIfKnown();
		if (size == -1)
			return -1;

		return randomSupplier.get().nextInt(size);
	}

	@Override
	protected Sequence<T> newInstance(Sequence<T> sequence) {
		return new ShuffledSequence<>(sequence, randomSupplier);
	}

	@Override
	public Sequence<T> sorted() {
		return original.sorted();
	}

	@Override
	public Sequence<T> sorted(Comparator<? super T> comparator) {
		return original.sorted(comparator);
	}
}
