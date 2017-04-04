package org.d2ab.sequence;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static org.d2ab.util.Preconditions.requireFinite;

abstract class StableReorderedSequence<T> implements Sequence<T> {
	protected final Sequence<T> parent;

	protected StableReorderedSequence(Sequence<T> parent) {
		this.parent = requireFinite(requireNonNull(parent), "Infinite Sequence");
	}

	@Override
	public abstract Iterator<T> iterator();

	protected abstract Sequence<T> withParent(Sequence<T> parent);

	@Override
	public abstract Object[] toArray();

	@Override
	public abstract <A> A[] toArray(IntFunction<A[]> constructor);

	@Override
	public abstract List<T> toList();

	@Override
	public abstract List<T> toList(Supplier<? extends List<T>> constructor);

	@Override
	public SizeType sizeType() {
		return parent.sizeType();
	}

	@Override
	public int size() {
		return parent.size();
	}

	@Override
	public boolean isEmpty() {
		return parent.isEmpty();
	}

	@Override
	public Optional<T> arbitrary() {
		return parent.arbitrary();
	}

	@Override
	public Optional<T> arbitrary(Predicate<? super T> predicate) {
		return parent.arbitrary(predicate);
	}

	@Override
	public <U> Optional<U> arbitrary(Class<U> targetClass) {
		return parent.arbitrary(targetClass);
	}

	@Override
	public Optional<T> removeArbitrary() {
		return parent.removeArbitrary();
	}

	@Override
	public Optional<T> removeArbitrary(Predicate<? super T> predicate) {
		return parent.removeArbitrary(predicate);
	}

	@Override
	public <U> Optional<U> removeArbitrary(Class<U> targetClass) {
		return parent.removeArbitrary(targetClass);
	}

	@Override
	public Set<T> toSet() {
		return parent.toSet();
	}

	@Override
	public SortedSet<T> toSortedSet() {
		return parent.toSortedSet();
	}

	@Override
	public SortedSet<T> toSortedSet(Comparator<? super T> comparator) {
		return parent.toSortedSet(comparator);
	}

	@Override
	public <K, V> Map<K, V> toMap() {
		return parent.toMap();
	}

	@Override
	public <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper,
	                              Function<? super T, ? extends V> valueMapper) {
		return parent.toMap(keyMapper, valueMapper);
	}

	@Override
	public <K, V> SortedMap<K, V> toSortedMap() {
		return parent.toSortedMap();
	}

	@Override
	public <K, V> SortedMap<K, V> toSortedMap(Function<? super T, ? extends K> keyMapper,
	                                          Function<? super T, ? extends V> valueMapper) {
		return parent.toSortedMap(keyMapper, valueMapper);
	}

	@Override
	public <K, V> SortedMap<K, V> toSortedMap(Comparator<? super K> comparator) {
		return parent.toSortedMap(comparator);
	}

	@Override
	public <K, V> SortedMap<K, V> toSortedMap(Comparator<? super K> comparator,
	                                          Function<? super T, ? extends K> keyMapper,
	                                          Function<? super T, ? extends V> valueMapper) {
		return parent.toSortedMap(comparator, keyMapper, valueMapper);
	}

	@Override
	public Optional<T> min() {
		return parent.min();
	}

	@Override
	public Optional<T> max() {
		return parent.max();
	}

	@Override
	public Optional<T> min(Comparator<? super T> comparator) {
		return parent.min(comparator);
	}

	@Override
	public Optional<T> max(Comparator<? super T> comparator) {
		return parent.max(comparator);
	}

	@Override
	public boolean all(Predicate<? super T> predicate) {
		return parent.all(predicate);
	}

	@Override
	public boolean none(Predicate<? super T> predicate) {
		return parent.none(predicate);
	}

	@Override
	public boolean any(Predicate<? super T> predicate) {
		return parent.any(predicate);
	}

	@Override
	public boolean all(Class<?> target) {
		return parent.all(target);
	}

	@Override
	public boolean none(Class<?> targetClass) {
		return parent.none(targetClass);
	}

	@Override
	public boolean any(Class<?> target) {
		return parent.any(target);
	}

	@Override
	public Sequence<T> shuffle() {
		return parent.shuffle();
	}

	@Override
	public Sequence<T> shuffle(Random random) {
		return parent.shuffle(random);
	}
}
