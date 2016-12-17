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

import org.d2ab.collection.ChainedList;
import org.d2ab.collection.FilteredList;
import org.d2ab.collection.MappedList;
import org.d2ab.collection.ReverseList;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * A {@link Sequence} backed by a {@link List}. Implements certain operations on {@link Sequence} in a more performant
 * way due to the {@link List} backing. This class should normally not be used directly as e.g.
 * {@link Sequence#from(Iterable)} and other methods return this class directly where appropriate.
 */
public class ListSequence<T> extends AbstractSequentialList<T> implements Sequence<T> {
	private List<T> list;

	public static <T> ListSequence<T> empty() {
		return from(emptyList());
	}

	public static <T> ListSequence<T> of(T item) {
		return from(singletonList(item));
	}

	@SuppressWarnings("unchecked")
	public static <T> ListSequence<T> of(T... items) {
		return from(Arrays.asList(items));
	}

	public static <T> ListSequence<T> from(List<T> list) {
		return new ListSequence<>(list);
	}

	@SafeVarargs
	public static <T> ListSequence<T> concat(List<T>... lists) {
		return from(ChainedList.from(lists));
	}

	public static <T> ListSequence<T> concat(List<List<T>> lists) {
		return from(ChainedList.from(lists));
	}

	public static <T> ListSequence<T> withCapacity(int capacity) { return new ListSequence<>(capacity); }

	public ListSequence() {
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
	public ListIterator<T> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public Spliterator<T> spliterator() {
		return Spliterators.spliterator(this, 0);
	}

	@Override
	public ListSequence<T> subList(int from, int to) {
		return new ListSequence<>(list.subList(from, to));
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
	public Stream<T> stream() {
		return list.stream();
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
		return from(new ReverseList<>(list));
	}

	@Override
	public Sequence<T> filter(Predicate<? super T> predicate) {
		return from(new FilteredList<>(list, predicate));
	}

	@Override
	public <U> Sequence<U> map(Function<? super T, ? extends U> mapper) {
		return from(MappedList.from(list, mapper));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Sequence<T> append(Iterable<T> iterable) {
		if (iterable instanceof List)
			return from(ChainedList.from(list, (List<T>) iterable));
		
		return Sequence.concat(this, iterable);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Sequence<T> append(T... items) {
		return append(Arrays.asList(items));
	}
}
