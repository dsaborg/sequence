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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.*;

/**
 * A {@link Sequence} backed by a {@link List}. Implements certain operations on {@link Sequence} in a more performant
 * way due to the {@link List} backing. This class should normally not be used directly as e.g.
 * {@link Sequence#from(Iterable)} and other methods return this class directly where appropriate.
 */
public class ListSequence<T> implements Sequence<T> {
	private final List<T> list;

	/**
	 * @return an immutable empty {@code ListSequence}.
	 */
	static <T> Sequence<T> empty() {
		return from(emptyList());
	}

	/**
	 * @return an immutable {@code ListSequence} of the given element.
	 */
	static <T> Sequence<T> of(T item) {
		return from(singletonList(item));
	}

	/**
	 * @return an immutable {@code ListSequence} of the given elements.
	 */
	@SuppressWarnings("unchecked")
	static <T> Sequence<T> of(T... items) {
		return from(unmodifiableList(Arrays.asList(items)));
	}

	/**
	 * @return a {@code ListSequence} backed by the given {@link List}. Updates to the backing list is reflected in the
	 * returned {@link ListSequence}.
	 */
	static <T> Sequence<T> from(List<T> list) {
		return new ListSequence<>(list);
	}

	/**
	 * @return a {@code ListSequence} backed by the concatenation of the given {@link List}s. Updates to the backing
	 * lists is reflected in the returned {@link ListSequence}.
	 */
	@SafeVarargs
	static <T> Sequence<T> concat(List<T>... lists) {
		return from(ChainedList.concat(lists));
	}

	/**
	 * @return a {@code ListSequence} backed by the concatenation of the given {@link List}s. Updates to the backing
	 * lists is reflected in the returned {@link ListSequence}.
	 */
	static <T> Sequence<T> concat(List<List<T>> lists) {
		return from(ChainedList.concat(lists));
	}

	/**
	 * @return a new empty mutable {@code ListSequence}.
	 */
	static <T> Sequence<T> create() {
		return new ListSequence<>();
	}

	/**
	 * @return a new empty mutable {@code ListSequence} with the given initial capacity.
	 */
	static <T> Sequence<T> withCapacity(int capacity) {
		return new ListSequence<>(capacity);
	}

	/**
	 * @return a new mutable {@code ListSequence} initialized with the given elements.
	 */
	@SafeVarargs
	static <T> Sequence<T> createOf(T... ts) {
		ListSequence<T> result = new ListSequence<>(ts.length);
		result.addAll(Arrays.asList(ts));
		return result;
	}

	/**
	 * @return a new mutable {@code ListSequence} initialized with the elements in the given {@link Collection}.
	 */
	static <T> Sequence<T> createFrom(Collection<? extends T> c) {
		ListSequence<T> result = new ListSequence<>(c.size());
		result.addAll(c);
		return result;
	}

	/**
	 * @return a new mutable {@code ListSequence} initialized with the elements in the given {@link Iterable}.
	 */
	static <T> Sequence<T> createFrom(Iterable<? extends T> iterable) {
		ListSequence<T> result = new ListSequence<>();
		iterable.forEach(result::add);
		return result;
	}

	/**
	 * @return a new mutable {@code ListSequence} initialized with the remaining elements in the given
	 * {@link Iterator}.
	 */
	static <T> Sequence<T> createFrom(Iterator<? extends T> iterator) {
		ListSequence<T> result = new ListSequence<>();
		iterator.forEachRemaining(result::add);
		return result;
	}

	/**
	 * Create a new empty mutable {@code ListSequence}.
	 */
	private ListSequence() {
		this(new ArrayList<>());
	}

	private ListSequence(int capacity) {
		this(new ArrayList<>(capacity));
	}

	private ListSequence(List<T> list) {
		this.list = list;
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	@Override
	public List<T> asList() {
		return list;
	}

	@Override
	public List<T> toList() {
		return new ArrayList<>(list);
	}

	@Override
	public <U extends Collection<T>> U collectInto(U collection) {
		collection.addAll(list);
		return collection;
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object item) {
		return list.contains(item);
	}

	@Override
	public boolean add(T t) {
		return list.add(t);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return list.addAll(c);
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		return list.removeIf(filter);
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	@Override
	public Stream<T> stream() {
		return list.stream();
	}

	@Override
	public Stream<T> parallelStream() {
		return list.parallelStream();
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		list.forEach(action);
	}

	@Override
	public Spliterator<T> spliterator() {
		return list.spliterator();
	}

	@Override
	public Optional<T> at(int index) {
		if (index >= list.size())
			return Optional.empty();

		return Optional.of(list.get(index));
	}

	@Override
	public Optional<T> last() {
		if (list.size() < 1)
			return Optional.empty();

		return Optional.of(list.get(list.size() - 1));
	}

	@Override
	public Sequence<T> reverse() {
		return from(ReverseList.from(list));
	}

	@Override
	public Sequence<T> filter(Predicate<? super T> predicate) {
		return from(FilteredList.from(list, predicate));
	}

	@Override
	public <U> Sequence<U> map(Function<? super T, ? extends U> mapper) {
		return from(MappedList.from(list, mapper));
	}

	@Override
	public <U> Sequence<U> biMap(Function<? super T, ? extends U> mapper, Function<? super U, ? extends T>
			backMapper) {
		return from(BiMappedList.from(list, mapper, backMapper));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Sequence<T> append(Iterable<T> iterable) {
		if (iterable instanceof List)
			return from(ChainedList.concat(list, (List<T>) iterable));

		return Sequence.concat(this, iterable);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Sequence<T> append(T... items) {
		return append(Arrays.asList(items));
	}
}
