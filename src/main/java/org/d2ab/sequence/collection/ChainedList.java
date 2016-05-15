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

import org.d2ab.collection.iterator.ChainingIterator;

import java.util.*;

import static java.util.Collections.singletonList;

/**
 * A {@link List} of multiple {@link List}s strung together in a chain.
 */
public class ChainedList<T> extends AbstractList<T> {
	private final List<List<T>> lists;

	@SuppressWarnings("unchecked")
	public static <T> List<T> empty() {
		return from();
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> of(T item) {
		return from(singletonList(item));
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> of(T... items) {
		return from(Arrays.asList(items));
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> from(List<T>... lists) {
		return from(Arrays.asList(lists));
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> from(List<List<T>> lists) {
		return new ChainedList<>(lists);
	}

	private ChainedList(List<List<T>> lists) {
		this.lists = lists;
	}

	@Override
	public Iterator<T> iterator() {
		return new ChainingIterator<>(lists);
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return new ChainedListIterator<>(lists, index);
	}

	@Override
	public T get(int index) {
		for (List<T> list : lists) {
			if (list.size() > index)
				return list.get(index);
			index -= list.size();
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public T set(int index, T element) {
		for (List<T> list : lists) {
			if (list.size() > index)
				return list.set(index, element);
			index -= list.size();
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public void add(int index, T element) {
		for (List<T> list : lists) {
			if (index <= list.size()) {
				list.add(index, element);
				return;
			}
			index -= list.size();
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public T remove(int index) {
		for (List<T> list : lists) {
			if (list.size() > index)
				return list.remove(index);
			index -= list.size();
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		for (List<T> list : lists) {
			if (index <= list.size())
				return list.addAll(index, c);
			index -= list.size();
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public int size() {
		int size = 0;
		for (List<T> list : lists)
			size += list.size();
		return size;
	}

	@Override
	public void clear() {
		lists.forEach(List::clear);
	}

	@Override
	public boolean isEmpty() {
		for (List<T> list : lists)
			if (!list.isEmpty())
				return false;
		return true;
	}
}
