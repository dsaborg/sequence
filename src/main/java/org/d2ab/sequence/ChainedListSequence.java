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

import org.d2ab.iterator.ChainingIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

/**
 * A {@link Sequence} of multiple {@link List}s strung together in a chain.
 */
public class ChainedListSequence<T> implements Sequence {
	private final List<List<T>> lists = new ArrayList<>();

	public ChainedListSequence(Iterable<List<T>> lists) {
		lists.forEach(this.lists::add);
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> from(List<T>... lists) {
		return from(asList(lists));
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> from(Iterable<List<T>> lists) {
		return new ChainedListSequence(lists);
	}

	@Override
	public Iterator iterator() {
		return new ChainingIterator<>(lists);
	}

	@Override
	public List toList() {
		List<T> list = new ArrayList<>((int) count());
		lists.forEach(list::addAll);
		return list;
	}

	@Override
	public long count() {
		long count = 0;
		for (List<T> list : lists)
			count += list.size();
		return count;
	}

	@Override
	public Optional get(long index) {
		for (List<T> list : lists) {
			if (list.size() > index)
				return Optional.of(list.get((int) index));
			index -= list.size();
		}
		return Optional.empty();
	}
}
