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

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * A {@link Collection} view of an {@link Iterable}, requiring only {@link Iterable#iterator()} to be implemented in
 * order to present a full {@link Collection}. This interface is thus a functional interface of {@link Iterable}'s
 * {@link Iterable#iterator()} method. All methods are implemented through {@link Iterator} traversal of the underlying
 * {@link Iterable}. All methods are supported except {@link #add(Object)} and {@link #addAll(Collection)}.
 */
@FunctionalInterface
public interface IterableCollection<T> extends Collection<T> {
	static <T> Collection<T> empty() {
		return (IterableCollection<T>) Iterators::empty;
	}

	static <T> Collection<T> of(T t) {
		return from(Iterables.of(t));
	}

	@SafeVarargs
	static <T> Collection<T> of(T... ts) {
		return from(Iterables.of(ts));
	}

	static <T> Collection<T> from(Iterable<T> iterable) {
		return (IterableCollection<T>) iterable::iterator;
	}

	@Override
	default int size() {
		return Iterators.count(iterator());
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
	default <T1> T1[] toArray(T1[] a) {
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

	@Override
	default boolean containsAll(Collection<?> c) {
		for (Object o : c)
			if (!contains(o))
				return false;
		return true;
	}

	@Override
	default boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return removeIf(c::contains);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return removeIf(o -> !c.contains(o));
	}

	@Override
	default void clear() {
		Iterables.removeAll(this);
	}
}
