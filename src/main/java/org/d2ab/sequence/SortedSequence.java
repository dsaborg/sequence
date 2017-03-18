package org.d2ab.sequence;

import org.d2ab.iterator.Iterators;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.naturalOrder;
import static org.d2ab.collection.Lists.sort;
import static org.d2ab.util.Preconditions.requireFinite;

/**
 * An implementation of {@link Sequence} which provides a sorted view of another {@link Sequence} based on a given
 * comparator. Provides optimizations for certain operations. The {@code comparator} may be null to indicate natural
 * ordering.
 */
public class SortedSequence<T> implements Sequence<T> {
	private final Sequence<T> original;
	private final Comparator<? super T> comparator;

	public SortedSequence(Sequence<T> original, Comparator<? super T> comparator) {
		this.original = requireFinite(original, "Infinite Sequence");
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

	protected Sequence<T> newInstance(Sequence<T> original) {
		return new SortedSequence<>(original, comparator);
	}

	@Override
	public SizeType sizeType() {
		return original.sizeType();
	}

	@Override
	public int size() {
		return original.size();
	}

	@Override
	public boolean isEmpty() {
		return original.isEmpty();
	}

	@Override
	public Sequence<T> filter(Predicate<? super T> predicate) {
		return newInstance(original.filter(predicate));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Sequence<U> filter(Class<U> targetClass) {
		return newInstance((Sequence) original.filter(targetClass));
	}

	@Override
	public Optional<T> arbitrary() {
		return original.arbitrary();
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
	public Optional<T> arbitrary(Predicate<? super T> predicate) {
		return original.filter(predicate).arbitrary();
	}

	@Override
	public Optional<T> first(Predicate<? super T> predicate) {
		return original.filter(predicate).min(mandatoryComparator());
	}

	@Override
	public Optional<T> last(Predicate<? super T> predicate) {
		return original.filter(predicate).max(mandatoryComparator());
	}

	@Override
	public <U> Optional<U> arbitrary(Class<U> targetClass) {
		return original.filter(targetClass).arbitrary();
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
	public Set<T> toSet() {
		return original.toSet();
	}

	@Override
	public SortedSet<T> toSortedSet() {
		return original.toSortedSet();
	}

	@Override
	public SortedSet<T> toSortedSet(Comparator<? super T> comparator) {
		return original.toSortedSet(comparator);
	}

	@Override
	public <K, V> Map<K, V> toMap() {
		return original.toMap();
	}

	@Override
	public <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper,
	                              Function<? super T, ? extends V> valueMapper) {
		return original.toMap(keyMapper, valueMapper);
	}

	@Override
	public <K, V> SortedMap<K, V> toSortedMap() {
		return original.toSortedMap();
	}

	@Override
	public <K, V> SortedMap<K, V> toSortedMap(Function<? super T, ? extends K> keyMapper,
	                                          Function<? super T, ? extends V> valueMapper) {
		return original.toSortedMap(keyMapper, valueMapper);
	}

	@Override
	public <K, V> SortedMap<K, V> toSortedMap(Comparator<? super K> comparator) {
		return original.toSortedMap(comparator);
	}

	@Override
	public <K, V> SortedMap<K, V> toSortedMap(Comparator<? super K> comparator,
	                                          Function<? super T, ? extends K> keyMapper,
	                                          Function<? super T, ? extends V> valueMapper) {
		return original.toSortedMap(comparator, keyMapper, valueMapper);
	}

	@Override
	public Optional<T> min() {
		return original.min();
	}

	@Override
	public Optional<T> max() {
		return original.max();
	}

	@Override
	public Optional<T> min(Comparator<? super T> comparator) {
		return original.min(comparator);
	}

	@Override
	public Optional<T> max(Comparator<? super T> comparator) {
		return original.max(comparator);
	}

	@Override
	public boolean all(Predicate<? super T> predicate) {
		return original.all(predicate);
	}

	@Override
	public boolean none(Predicate<? super T> predicate) {
		return original.none(predicate);
	}

	@Override
	public boolean any(Predicate<? super T> predicate) {
		return original.any(predicate);
	}

	@Override
	public boolean all(Class<?> target) {
		return original.all(target);
	}

	@Override
	public boolean none(Class<?> targetClass) {
		return original.none(targetClass);
	}

	@Override
	public boolean any(Class<?> target) {
		return original.any(target);
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

	@Override
	public Sequence<T> shuffle() {
		return original.shuffle();
	}

	@Override
	public Sequence<T> shuffle(Random random) {
		return original.shuffle(random);
	}

	@Override
	public Sequence<T> shuffle(Supplier<? extends Random> randomSupplier) {
		return original.shuffle(randomSupplier);
	}
}
