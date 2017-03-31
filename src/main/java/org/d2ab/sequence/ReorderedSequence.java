package org.d2ab.sequence;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static org.d2ab.util.Preconditions.requireFinite;

abstract class ReorderedSequence<T> implements Sequence<T> {
	protected final Sequence<T> original;

	protected ReorderedSequence(Sequence<T> original) {
		this.original = requireFinite(requireNonNull(original), "Infinite Sequence");
	}

	@Override
	public abstract Iterator<T> iterator();

	protected abstract Sequence<T> newInstance(Sequence<T> original);

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
	public Optional<T> arbitrary(Predicate<? super T> predicate) {
		return original.arbitrary(predicate);
	}

	@Override
	public <U> Optional<U> arbitrary(Class<U> targetClass) {
		return original.arbitrary(targetClass);
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
