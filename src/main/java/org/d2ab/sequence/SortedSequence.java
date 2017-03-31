package org.d2ab.sequence;

import org.d2ab.iterator.Iterators;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Predicate;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.naturalOrder;
import static org.d2ab.collection.Lists.sort;

/**
 * An implementation of {@link Sequence} which provides a sorted view of another {@link Sequence} based on a given
 * comparator. Provides optimizations for certain operations. The {@code comparator} may be null to indicate natural
 * ordering.
 */
class SortedSequence<T> extends ReorderedSequence<T> {
	private final Comparator<? super T> comparator;

	SortedSequence(Sequence<T> original, Comparator<? super T> comparator) {
		super(original);
		this.comparator = comparator;
	}

	@Override
	public Iterator<T> iterator() {
		if (comparator == null || comparator == naturalOrder())
			return Sequence.generate(toNaturalOrderPriorityQueue()::poll).untilNull().iterator();
		else if (comparator == reverseOrder())
			return Sequence.generate(toReverseOrderPriorityQueue()::poll).untilNull().iterator();
		else
			return Iterators.unmodifiable(sort(original.toList(), comparator));
	}

	protected PriorityQueue<T> toNaturalOrderPriorityQueue() {
		if (sizeType().known())
			return new PriorityQueue<>(original);
		else
			return original.toCollection(PriorityQueue::new);
	}

	protected PriorityQueue<T> toReverseOrderPriorityQueue() {
		return original.collectInto(new PriorityQueue<>(comparator));
	}

	@SuppressWarnings("RedundantCast")
	protected Comparator<? super T> mandatoryComparator() {
		return comparator == null ? (Comparator) Comparator.naturalOrder() : comparator;
	}

	protected Sequence<T> newInstance(Sequence<T> sequence) {
		return new SortedSequence<>(sequence, comparator);
	}

	@Override
	public Optional<T> first() {
		return original.min(mandatoryComparator());
	}

	@Override
	public Optional<T> last() {
		return original.max(mandatoryComparator());
	}

	@Override
	public Optional<T> first(Predicate<? super T> predicate) {
		return original.filter(predicate).min(mandatoryComparator());
	}

	@Override
	public Optional<T> last(Predicate<? super T> predicate) {
		return original.filter(predicate).max(mandatoryComparator());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Optional<U> first(Class<U> targetClass) {
		return original.filter(targetClass).min((Comparator<? super U>) mandatoryComparator());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Optional<U> last(Class<U> targetClass) {
		return original.filter(targetClass).max((Comparator<? super U>) mandatoryComparator());
	}

	@Override
	public Sequence<T> sorted() {
		if (comparator == null || comparator == naturalOrder())
			return this;
		else
			return original.sorted();
	}

	@Override
	public Sequence<T> sorted(Comparator<? super T> comparator) {
		if (this.comparator == comparator ||
		    (this.comparator == null || this.comparator == naturalOrder()) &&
		    (comparator == null || comparator == naturalOrder()))
			return this;
		else
			return original.sorted(comparator);
	}

	@Override
	public Sequence<T> reverse() {
		return original.sorted(reverseOrder(comparator));
	}
}
