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

package org.d2ab.sequence.collection;

import org.d2ab.collection.iterator.Iterators;

import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

/**
 * A {@link List} that provides a filtered view of another {@link List}. All operations are supported except variations
 * of {@link List#add}.
 *
 * @since 1.2
 */
public class FilteredList<T> extends AbstractSequentialList<T> {
	private final List<T> list;
	private final Predicate<? super T> predicate;

	private FilteredList(List<T> list, Predicate<? super T> predicate) {
		this.list = list;
		this.predicate = predicate;
	}

	public static <T> List<T> from(List<T> list, Predicate<? super T> predicate) {
		return new FilteredList<>(list, predicate);
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		ListIterator<T> listIterator = new FilteringListIterator<>(list.listIterator(), predicate);
		while (index-- > 0)
			listIterator.next();
		return listIterator;
	}

	@Override
	public int size() {
		return (int) Iterators.count(iterator());
	}

	@Override
	public boolean add(T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}
}
