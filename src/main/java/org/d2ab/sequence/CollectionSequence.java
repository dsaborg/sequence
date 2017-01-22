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

package org.d2ab.sequence;

import org.d2ab.collection.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A {@link Sequence} backed by a {@link Collection}. Implements certain operations on {@link Sequence} in a more
 * performant way due to the {@link Collection} backing. This class should not be used directly as {@link
 * Sequence#from(Iterable)} and other factory methods return this class directly where appropriate.
 */
public class CollectionSequence<T> implements Sequence<T> {
	private final Collection<T> collection;

	/**
	 * @return a {@code CollectionSequence} backed by the given {@link Collection}. Updates to the backing
	 * collection is
	 * reflected in the returned {@link CollectionSequence}.
	 */
	static <T> Sequence<T> from(Collection<T> collection) {
		return new CollectionSequence<>(collection);
	}

	/**
	 * @return a {@code CollectionSequence} backed by the concatenation of the given {@link Collection}s. Updates to
	 * the
	 * backing collections is reflected in the returned {@link CollectionSequence}.
	 */
	static <T> Sequence<T> concat(Collection<Collection<T>> collections) {
		return from(ChainedCollection.concat(collections));
	}

	private CollectionSequence(Collection<T> collection) {
		this.collection = collection;
	}

	@Override
	public Iterator<T> iterator() {
		return collection.iterator();
	}

	@Override
	public List<T> toList() {
		return new ArrayList<>(collection);
	}

	@Override
	public <U extends Collection<T>> U collectInto(U collection) {
		collection.addAll(this.collection);
		return collection;
	}

	@Override
	public void clear() {
		collection.clear();
	}

	@Override
	public int size() {
		return collection.size();
	}

	@Override
	public Stream<T> stream() {
		return collection.stream();
	}

	@Override
	public Stream<T> parallelStream() {
		return collection.parallelStream();
	}

	@Override
	public Spliterator<T> spliterator() {
		return collection.spliterator();
	}

	@Override
	public boolean isEmpty() {
		return collection.isEmpty();
	}

	@Override
	public boolean add(T t) {
		return collection.add(t);
	}

	@Override
	public boolean remove(Object o) {
		return collection.remove(o);
	}

	@Override
	public boolean contains(Object item) {
		return collection.contains(item);
	}

	public List<T> asList() {
		return Collectionz.asList(collection);
	}

	@Override
	public Sequence<T> filter(Predicate<? super T> predicate) {
		return from(FilteredCollection.from(collection, predicate));
	}

	@Override
	public <U> Sequence<U> map(Function<? super T, ? extends U> mapper) {
		return from(MappedCollection.from(collection, mapper));
	}

	@Override
	public <U> Sequence<U> biMap(Function<? super T, ? extends U> mapper, Function<? super U, ? extends T>
			backMapper) {
		return from(BiMappedCollection.from(collection, mapper, backMapper));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Sequence<T> append(Iterable<T> iterable) {
		if (iterable instanceof Collection)
			return from(ChainedCollection.concat(collection, (Collection<T>) iterable));

		return Sequence.concat(this, iterable);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Sequence<T> append(T... items) {
		return append(Lists.of(items));
	}
}
