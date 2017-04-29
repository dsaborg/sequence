package org.d2ab.sequence;

import org.d2ab.collection.Arrayz;
import org.d2ab.collection.Lists;
import org.d2ab.iterator.ShufflingArrayIterator;

import java.util.*;
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
	public Optional<T> min(Comparator<? super T> comparator) {
		List<T> mins = mins(parent, comparator);
		return Optional.of(mins.get(random.nextInt(mins.size())));
	}

	private List<T> mins(Sequence<T> sequence, Comparator<? super T> comparator) {
		List<T> mins = new ArrayList<>();
		sequence.reduce((r, x) -> {
			int compare = comparator.compare(r, x);

			if (compare <= 0 && mins.isEmpty())
				mins.add(r);

			if (compare > 0)
				mins.clear();

			if (compare >= 0)
				mins.add(x);

			return compare <= 0 ? r : x;
		});
		return mins;
	}

	@Override
	public Optional<T> max(Comparator<? super T> comparator) {
		List<T> maxes = maxes(parent, comparator);
		return Optional.of(maxes.get(random.nextInt(maxes.size())));
	}

	private List<T> maxes(Sequence<T> sequence, Comparator<? super T> comparator) {
		List<T> maxes = new ArrayList<>();
		sequence.reduce((r, x) -> {
			int compare = comparator.compare(r, x);

			if (compare >= 0 && maxes.isEmpty())
				maxes.add(r);

			if (compare < 0)
				maxes.clear();

			if (compare <= 0)
				maxes.add(x);

			return compare >= 0 ? r : x;
		});
		return maxes;
	}

	@Override
	public Optional<T> first() {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex != -1)
			return parent.at(randomIndex);

		return super.first();
	}

	@Override
	public Optional<T> last() {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex != -1)
			return parent.at(randomIndex);

		return super.last();
	}

	@Override
	public Optional<T> at(int index) {
		int size = sizeIfKnown();
		if (size == -1)
			return super.at(index);

		if (index >= size)
			return Optional.empty();

		int randomIndex = random.nextInt(size);
		return parent.at(randomIndex);
	}

	@Override
	public Optional<T> removeFirst() {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex != -1)
			return parent.removeAt(randomIndex);

		return super.removeFirst();
	}

	@Override
	public Optional<T> removeLast() {
		int randomIndex = randomIndexIfKnown();
		if (randomIndex != -1)
			return parent.removeAt(randomIndex);

		return super.removeLast();
	}

	@Override
	public Optional<T> removeAt(int index) {
		int size = sizeIfKnown();
		if (size == -1)
			return super.removeAt(index);

		if (index >= size)
			return Optional.empty();

		int randomIndex = random.nextInt(size);
		return parent.removeAt(randomIndex);
	}

	private int randomIndexIfKnown() {
		int size = sizeIfKnown();
		if (size == -1)
			return -1;

		return random.nextInt(size);
	}
}
