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

import java.util.*;

/**
 * A list that is a reverse view over a backing list.
 */
public class ReverseList<T> extends AbstractList<T> {
	private List<T> original;

	private ReverseList(List<T> original) {
		this.original = original;
	}

	public static <T> List<T> from(List<T> original) {
		return new ReverseList<>(original);
	}

	@Override
	public int size() {
		return original.size();
	}

	@Override
	public boolean isEmpty() {
		return original.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return original.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return listIterator();
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return addAll(size(), c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		int start = original.size() - index;
		boolean changed = original.addAll(start, c);
		for (int i = 0, size = c.size(); i < size / 2; i++)
			swap(start + i, start + size - i - 1);
		return changed;
	}

	private void swap(int i, int j) {
		T temp = original.get(i);
		original.set(i, original.get(j));
		original.set(j, temp);
	}

	@Override
	public void sort(Comparator<? super T> c) {
		original.sort(c);
		Collections.reverse(original);
	}

	@Override
	public void clear() {
		original.clear();
	}

	@Override
	public T get(int index) {
		return original.get(original.size() - index - 1);
	}

	@Override
	public T set(int index, T element) {
		return original.set(original.size() - index - 1, element);
	}

	@Override
	public void add(int index, T element) {
		original.add(original.size() - index, element);
	}

	@Override
	public T remove(int index) {
		return original.remove(original.size() - index - 1);
	}

	@Override
	public int indexOf(Object o) {
		int lastIndexOf = original.lastIndexOf(o);
		return lastIndexOf == -1 ? -1 : original.size() - 1 - lastIndexOf;
	}

	@Override
	public int lastIndexOf(Object o) {
		int indexOf = original.indexOf(o);
		return indexOf == -1 ? -1 : original.size() - 1 - indexOf;
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		ListIterator<T> listIterator = original.listIterator(original.size() - index);
		return new ListIterator<T>() {
			@Override
			public boolean hasNext() {
				return listIterator.hasPrevious();
			}

			@Override
			public T next() {
				return listIterator.previous();
			}

			@Override
			public boolean hasPrevious() {
				return listIterator.hasNext();
			}

			@Override
			public T previous() {
				return listIterator.next();
			}

			@Override
			public int nextIndex() {
				return original.size() - listIterator.previousIndex() - 1;
			}

			@Override
			public int previousIndex() {
				return original.size() - listIterator.nextIndex() - 1;
			}

			@Override
			public void remove() {
				listIterator.remove();
			}

			@Override
			public void set(T o) {
				listIterator.set(o);
			}

			@Override
			public void add(T o) {
				listIterator.add(o);
				listIterator.previous();
			}
		};
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return ReverseList.from(original.subList(original.size() - toIndex, original.size() - fromIndex));
	}
}
