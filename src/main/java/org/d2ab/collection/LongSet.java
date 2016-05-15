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
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A primitive specialization of {@link Set} for {@code long} values.
 */
public interface LongSet extends Set<Long>, LongCollection {
	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default void clear() {
		iterator().removeAll();
	}

	@Override
	default boolean add(Long l) {
		return addLong(l);
	}

	@Override
	default boolean contains(Object o) {
		return containsLong((long) o);
	}

	@Override
	default boolean remove(Object o) {
		return removeLong((long) o);
	}

	@Override
	default Long[] toArray() {
		return Collectionz.toBoxedLongArray(this);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return Collectionz.toArray(this, a);
	}

	@Override
	default boolean addAll(Collection<? extends Long> c) {
		return Collectionz.addAll(this, c);
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return Collectionz.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return Collectionz.removeAll(this, c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return Collectionz.retainAll(this, c);
	}

	@Override
	default Spliterator.OfLong spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.DISTINCT | Spliterator.NONNULL);
	}
}
