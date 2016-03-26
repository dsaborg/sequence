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

import org.d2ab.iterable.ChainingIterable;
import org.d2ab.iterator.ChainingIterator;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * A {@link Sequence} of multiple {@link List}s strung together in a chain.
 */
public class ChainedListSequence<T> implements Sequence<T> {
	private final List<List<T>> lists = new ArrayList<>();

	public ChainedListSequence(List<List<T>> lists) {
		this.lists.addAll(lists);
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> empty() {
		return from();
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> of(T item) {
		return from(singletonList(item));
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> of(T... items) {
		return from(asList(items));
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> from(List<T>... lists) {
		return from(asList(lists));
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> from(List<List<T>> lists) {
		return new ChainedListSequence(lists);
	}

	@Override
	public Iterator<T> iterator() {
		return new ChainingIterator<>(lists);
	}

	@Override
	public <U extends Collection<T>> U collectInto(U collection) {
		lists.forEach(collection::addAll);
		return collection;
	}

	@Override
	public long count() {
		long count = 0;
		for (List<T> list : lists)
			count += list.size();
		return count;
	}

	@Override
	public Optional<T> get(long index) {
		for (List<T> list : lists) {
			if (list.size() > index)
				return Optional.of(list.get((int) index));
			index -= list.size();
		}
		return Optional.empty();
	}

	@Override
	public Sequence<T> append(Iterable<T> iterable) {
		if (iterable instanceof List) {
			ChainedListSequence<T> newSequence = new ChainedListSequence<>(lists);
			newSequence.lists.add((List<T>) iterable);
			return newSequence;
		}

		return new ChainingIterable<>(this, iterable)::iterator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Sequence<T> append(T... items) {
		ChainedListSequence<T> newSequence = new ChainedListSequence<>(lists);
		newSequence.lists.add(asList(items));
		return newSequence;
	}
}
