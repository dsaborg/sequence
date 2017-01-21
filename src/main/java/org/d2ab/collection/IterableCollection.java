/*
 * Copyright 2016 Daniel Skogquist Åborg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.d2ab.collection;

import org.d2ab.iterator.Iterators;

import java.util.*;
import java.util.function.Predicate;

/**
 * A {@link Collection} view of an {@link Iterable}, requiring only {@link Iterable#iterator()} to be implemented in
 * order to present a full {@link Collection}. This interface is thus a functional interface of {@link Iterable}'s
 * {@link Iterable#iterator()} method. All methods are implemented through {@link Iterator} traversal of the underlying
 * {@link Iterable}. All methods are supported except {@link #add(Object)} and {@link #addAll(Collection)}.
 */
@FunctionalInterface
public interface IterableCollection<T> extends Collection<T>, SizedIterable<T> {
	static <T> IterableCollection<T> empty() {
		return from(Iterables.empty());
	}

	static <T> IterableCollection<T> of(T t) {
		return from(Iterables.of(t));
	}

	@SafeVarargs
	static <T> IterableCollection<T> of(T... ts) {
		return from(Iterables.of(ts));
	}

	static <T> IterableCollection<T> from(Iterable<T> iterable) {
		if (iterable instanceof SizedIterable)
			return from((SizedIterable<T>) iterable);

		return new IterableCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return iterable.iterator();
			}

			@Override
			public int size() {
				return Iterables.size(iterable);
			}
		};
	}

	static <T> IterableCollection<T> from(SizedIterable<T> iterable) {
		return new IterableCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return iterable.iterator();
			}

			@Override
			public int size() {
				return iterable.size();
			}
		};
	}

	@Override
	default int size() {
		return Iterators.size(iterator());
	}

	@Override
	default boolean isEmpty() {
		return !iterator().hasNext();
	}

	@Override
	default boolean contains(Object o) {
		return Iterators.contains(iterator(), o);
	}

	@Override
	default Object[] toArray() {
		return Iterators.toList(iterator()).toArray();
	}

	@Override
	default <TT> TT[] toArray(TT[] a) {
		return Iterators.toList(iterator()).toArray(a);
	}

	@Override
	default boolean add(T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean remove(Object o) {
		Iterator<T> iterator = iterator();
		while (iterator.hasNext())
			if (Objects.equals(o, iterator.next())) {
				iterator.remove();
				return true;
			}

		return false;
	}

	/**
	 * @return true if this {@code Sequence} contains all of the given items, false otherwise.
	 *
	 * @since 2.2
	 */
	default boolean containsAll(Object... items) {
		return Iterables.containsAll(this, items);
	}

	/**
	 * @return true if this {@code Sequence} contains all of the items in the given {@link Iterable}, false otherwise.
	 *
	 * @since 2.2
	 */
	default boolean containsAll(Iterable<?> items) {
		if (items instanceof Collection)
			return containsAll((Collection<?>) items);

		for (Object item : items)
			if (!contains(item))
				return false;

		return true;
	}

	/**
	 * @return true if this {@code Sequence} contains all of the items in the given {@link Collection}, false
	 * otherwise.
	 */
	@Override
	default boolean containsAll(Collection<?> c) {
		for (Object o : c)
			if (!contains(o))
				return false;

		return true;
	}

	/**
	 * @return true if this {@code Sequence} contains any of the given items, false otherwise.
	 */
	default boolean containsAny(Object... items) {
		return Iterables.containsAny(this, items);
	}

	/**
	 * @return true if this {@code Sequence} contains any of the given items, false otherwise.
	 */
	default boolean containsAny(Iterable<?> items) {
		return Iterables.containsAny(this, items);
	}

	/**
	 * @return true if this {@code Sequence} contains any of the items in the given {@link Collection}, false
	 * otherwise.
	 *
	 * @since 2.2
	 */
	default boolean containsAny(Collection<?> c) {
		for (Object o : c)
			if (contains(o))
				return true;

		return false;
	}

	/**
	 * Add all the items in the given array to this {@code Sequence}.
	 *
	 * @return true if any items were added to this {@code Sequence}.
	 *
	 * @since 2.2
	 */
	@SuppressWarnings("unchecked")
	default boolean addAll(T... items) {
		boolean modified = false;
		for (T t : items)
			modified |= add(t);
		return modified;
	}

	/**
	 * Add all the items in the given {@link Iterable} to this {@code Sequence}.
	 *
	 * @return true if any items were added to this {@code Sequence}.
	 *
	 * @since 2.2
	 */
	default boolean addAll(Iterable<? extends T> iterable) {
		boolean modified = false;
		for (T t : iterable)
			modified |= add(t);
		return modified;
	}

	/**
	 * Add all the items in the given {@link Collection} to this {@link Collection}.
	 *
	 * @return true if any items were added to this {@link Collection}.
	 *
	 * @since 2.2
	 */
	@Override
	default boolean addAll(Collection<? extends T> c) {
		boolean modified = false;
		for (T t : c)
			modified |= add(t);
		return modified;
	}

	/**
	 * Remove all of the given items that are present in this {@link Collection}.
	 *
	 * @return true if any items were removed from this {@link Collection}.
	 *
	 * @since 2.2
	 */
	default boolean removeAll(Object... items) {
		return Iterables.removeAll(this, items);
	}

	/**
	 * Remove all the items in the given {@link Iterable} that are present in this {@link Collection}.
	 *
	 * @return true if any items were removed from this {@link Collection}.
	 *
	 * @since 2.2
	 */
	default boolean removeAll(Iterable<?> items) {
		return Iterables.removeAll(this, items);
	}

	/**
	 * Remove all the items in the given {@link Collection} that are present in this {@link Collection}.
	 *
	 * @return true if any items were removed from this {@link Collection}.
	 */
	@Override
	default boolean removeAll(Collection<?> c) {
		boolean removed = false;
		for (Iterator<T> iterator = iterator(); iterator.hasNext(); ) {
			if (c.contains(iterator.next())) {
				iterator.remove();
				removed = true;
			}
		}
		return removed;
	}

	/**
	 * Remove all items in this {@link Collection} that are not among the given items.
	 *
	 * @return true if any items were removed from this {@link Collection}.
	 *
	 * @since 2.2
	 */
	default boolean retainAll(Object... items) {
		return Iterables.retainAll(this, items);
	}

	/**
	 * Remove all items in this {@link Collection} that are not among the items in the given {@link Iterable}.
	 *
	 * @return true if any items were removed from this {@link Collection}.
	 *
	 * @since 2.2
	 */
	default boolean retainAll(Iterable<?> items) {
		return Iterables.retainAll(this, items);
	}

	/**
	 * Remove all items in this {@link Collection} that are not among the items in the given {@link Collection}.
	 *
	 * @return true if any items were removed from this {@link Collection}.
	 */
	@Override
	default boolean retainAll(Collection<?> c) {
		return Iterables.retainAll(this, c);
	}

	/**
	 * Remove all items in this {@link Collection} that do not match the given {@link Predicate}.
	 *
	 * @return true if any items were removed from this {@link Collection}.
	 */
	default boolean retainIf(Predicate<? super T> condition) {
		boolean modified = false;
		for (Iterator<T> iterator = iterator(); iterator.hasNext(); ) {
			if (!condition.test(iterator.next())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	@Override
	default void clear() {
		Iterables.clear(this);
	}

	@Override
	default Spliterator<T> spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), 0);
	}
}
