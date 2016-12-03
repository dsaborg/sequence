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

import org.d2ab.collection.ChainedCollection;
import org.d2ab.collection.FilteredCollection;
import org.d2ab.collection.MappedCollection;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

/**
 * A {@link Sequence} backed by a {@link Collection}. Implements certain operations on {@link Sequence} in a more performant
 * way due to the {@link Collection} backing. This class should normally not be used directly as e.g.
 * {@link Sequence#from(Iterable)} and other methods return this class directly where appropriate.
 */
public class CollectionSequence<T> implements Sequence<T> {
	private Collection<T> collection;

	public static <T> Sequence<T> empty() {
		return from(emptySet());
	}

	public static <T> Sequence<T> of(T item) {
		return from(singleton(item));
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> of(T... items) {
		return from(Arrays.asList(items));
	}

	public static <T> Sequence<T> from(Collection<T> collection) {
		return new CollectionSequence<>(collection);
	}

	public static <T> Sequence<T> concat(Collection<T>... collections) {
		return from(ChainedCollection.from(collections));
	}

	public static <T> Sequence<T> concat(Collection<Collection<T>> collections) {
		return from(ChainedCollection.from(collections));
	}

	public CollectionSequence() {
		this(new ArrayList<>());
	}

	public CollectionSequence(int capacity) {
		this(new ArrayList<>(capacity));
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

	@Override
	public Sequence<T> filter(Predicate<? super T> predicate) {
		return from(FilteredCollection.from(collection, predicate));
	}

	@Override
	public <U> Sequence<U> map(Function<? super T, ? extends U> mapper) {
		return from(MappedCollection.from(collection, mapper));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Sequence<T> append(Iterable<T> iterable) {
		if (iterable instanceof Collection)
			return from(ChainedCollection.from(collection, (Collection<T>) iterable));
		
		return Sequence.concat(this, iterable);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Sequence<T> append(T... items) {
		return append(Arrays.asList(items));
	}
}
