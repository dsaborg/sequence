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
			return Iterators.unmodifiable(sort(parent.toList(), comparator));
	}

	protected PriorityQueue<T> toNaturalOrderPriorityQueue() {
		if (sizeType().known())
			return new PriorityQueue<>(parent);
		else
			return parent.toCollection(PriorityQueue::new);
	}

	protected PriorityQueue<T> toReverseOrderPriorityQueue() {
		return parent.collectInto(new PriorityQueue<>(comparator));
	}

	protected Sequence<T> withParent(Sequence<T> parent) {
		return new SortedSequence<>(parent, comparator);
	}

	@SuppressWarnings("unchecked")
	private <U extends Comparable<U>> Sequence<U> comparable(Sequence<?> sequence) {
		return (Sequence<U>) sequence;
	}

	@SuppressWarnings("unchecked")
	private Optional<T> firstMin(Sequence<T> sequence) {
		if (comparator == null) {
			return (Optional<T>) comparable(sequence).reduce((a, b) -> a.compareTo(b) <= 0 ? a : b);
		} else {
			return sequence.reduce((a, b) -> comparator.compare(a, b) <= 0 ? a : b);
		}
	}

	@SuppressWarnings("unchecked")
	private Optional<T> lastMax(Sequence<T> sequence) {
		if (comparator == null) {
			return (Optional<T>) comparable(sequence).reduce((a, b) -> a.compareTo(b) > 0 ? a : b);
		} else {
			return sequence.reduce((a, b) -> comparator.compare(a, b) > 0 ? a : b);
		}
	}

	@Override
	public Optional<T> first() {
		return firstMin(parent);
	}

	@Override
	public Optional<T> last() {
		return lastMax(parent);
	}

	@Override
	public Optional<T> first(Predicate<? super T> predicate) {
		return firstMin(parent.filter(predicate));
	}

	@Override
	public Optional<T> last(Predicate<? super T> predicate) {
		return lastMax(parent.filter(predicate));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Optional<U> first(Class<U> targetClass) {
		return (Optional<U>) firstMin((Sequence<T>) parent.filter(targetClass));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Optional<U> last(Class<U> targetClass) {
		return (Optional<U>) lastMax((Sequence<T>) parent.filter(targetClass));
	}

	@Override
	public Sequence<T> sorted() {
		if (comparator == null || comparator == naturalOrder())
			return this;
		else
			return parent.sorted();
	}

	@Override
	public Sequence<T> sorted(Comparator<? super T> comparator) {
		if (this.comparator == comparator ||
		    (this.comparator == null || this.comparator == naturalOrder()) &&
		    (comparator == null || comparator == naturalOrder()))
			return this;
		else
			return parent.sorted(comparator);
	}

	@Override
	public Sequence<T> reverse() {
		return parent.reverse().sorted(reverseOrder(comparator));
	}
}
