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

import org.d2ab.iterator.FilteringListIterator;
import org.d2ab.iterator.Iterators;

import java.util.AbstractSequentialList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

import static org.d2ab.iterator.Iterators.skip;

/**
 * A {@link List} that provides a filtered view of another {@link List}. All operations are supported except variations
 * of {@link List#add}.
 *
 * @since 1.2
 */
public class FilteredList<T> extends AbstractSequentialList<T> {
	private final List<T> list;
	private final Predicate<? super T> predicate;

	public static <T> List<T> from(List<T> list, Predicate<? super T> predicate) {
		return new FilteredList<>(list, predicate);
	}

	public FilteredList(List<T> list, Predicate<? super T> predicate) {
		this.list = list;
		this.predicate = predicate;
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		ListIterator<T> listIterator = new FilteringListIterator<>(list.listIterator(), predicate);
		int skipped = skip(listIterator, index);
		if (skipped != index)
			throw new IndexOutOfBoundsException("index: " + index + " size: " + skipped);

		return listIterator;
	}

	@Override
	public int size() {
		return Iterators.count(iterator());
	}

	@Override
	public boolean add(T t) {
		if (!predicate.test(t))
			throw new IllegalArgumentException(String.valueOf(t));

		return list.add(t);
	}
}
