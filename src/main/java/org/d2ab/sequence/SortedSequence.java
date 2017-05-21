package org.d2ab.sequence;

import org.d2ab.collection.Arrayz;
import org.d2ab.collection.Lists;
import org.d2ab.iterator.ArrayIterator;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;
import static org.d2ab.function.BinaryOperators.firstMinBy;
import static org.d2ab.function.BinaryOperators.lastMaxBy;

/**
 * An implementation of {@link Sequence} which provides a sorted view of another {@link Sequence} based on a given
 * comparator. Provides optimizations for certain operations. The {@code comparator} may be null to indicate natural
 * ordering.
 */
class SortedSequence<T> extends ReorderedSequence<T> {
	private final Comparator<? super T> comparator;

	SortedSequence(Sequence<T> parent, Comparator<? super T> comparator) {
		super(parent);
		this.comparator = comparator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator() {
		if (comparator == null || comparator == naturalOrder())
			return Sequence.generate(toNaturalOrderPriorityQueue()::poll).untilNull().iterator();
		else if (comparator == reverseOrder())
			return Sequence.generate(toReverseOrderPriorityQueue()::poll).untilNull().iterator();
		else
			return new ArrayIterator<>(Arrayz.sort((T[]) parent.toArray(), comparator));
	}

	protected PriorityQueue<T> toNaturalOrderPriorityQueue() {
		if (!sizeType().known())
			return parent.toCollection(PriorityQueue::new);

		return new PriorityQueue<>(parent);
	}

	protected PriorityQueue<T> toReverseOrderPriorityQueue() {
		return parent.collectInto(new PriorityQueue<>(comparator));
	}

	protected Sequence<T> withParent(Sequence<T> parent) {
		return new SortedSequence<>(parent, comparator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] toArray() {
		return Arrayz.sort(parent.toArray(), (Comparator) comparator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A> A[] toArray(IntFunction<A[]> constructor) {
		return (A[]) Arrayz.sort(parent.toArray(constructor), (Comparator) comparator);
	}

	@Override
	public List<T> toList() {
		return Lists.sort(parent.toList(), comparator);
	}

	@Override
	public List<T> toList(Supplier<? extends List<T>> constructor) {
		return Lists.sort(parent.toList(constructor), comparator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<T> min(Comparator<? super T> comparator) {
		requireNonNull(comparator, "comparator");
		return parent.min(comparator.thenComparing((Comparator) mandatoryComparator()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<T> max(Comparator<? super T> comparator) {
		requireNonNull(comparator, "comparator");
		return parent.max(comparator.thenComparing((Comparator) mandatoryComparator()));
	}

	@SuppressWarnings("unchecked")
	private Comparator<? super T> mandatoryComparator() {
		return (Comparator<? super T>) (comparator == null ? naturalOrder() : comparator);
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

	@SuppressWarnings("unchecked")
	private Optional<T> firstMin(Sequence<T> sequence) {
		return sequence.reduce(firstMinBy(comparator));
	}

	@SuppressWarnings("unchecked")
	private Optional<T> lastMax(Sequence<T> sequence) {
		return sequence.reduce(lastMaxBy(comparator));
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
