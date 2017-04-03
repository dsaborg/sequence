package org.d2ab.sequence;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static org.d2ab.util.Preconditions.requireFinite;

abstract class ReorderedSequence<T> implements Sequence<T> {
	protected final Sequence<T> parent;

	protected ReorderedSequence(Sequence<T> parent) {
		this.parent = requireFinite(requireNonNull(parent), "Infinite Sequence");
	}

	@Override
	public abstract Iterator<T> iterator();

	protected abstract Sequence<T> withParent(Sequence<T> parent);

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
	public Sequence<T> filter(Predicate<? super T> predicate) {
		return withParent(parent.filter(predicate));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Sequence<U> filter(Class<U> targetClass) {
		return withParent((Sequence) parent.filter(targetClass));
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
}
