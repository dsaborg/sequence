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

package org.d2ab.iterator;

import org.d2ab.collection.Lists;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ChainedListIterator<T> implements ListIterator<T> {
	private final List<List<T>> lists;
	private ListIterator<T> listIterator;
	private int cursor;
	private int offset;

	public ChainedListIterator(List<List<T>> lists, int index) {
		this.lists = lists;

		int i = 0;
		for (List<T> list : lists) {
			if (index <= list.size()) {
				listIterator = list.listIterator(index);
				cursor = i;
				break;
			}
			offset += list.size();
			index -= list.size();
			i++;
		}
		if (listIterator == null)
			listIterator = Lists.<T>of().listIterator();
	}

	@Override
	public boolean hasNext() {
		while (!listIterator.hasNext() && cursor < lists.size() - 1) {
			offset += lists.get(cursor).size();
			listIterator = lists.get(++cursor).listIterator();
		}
		return listIterator.hasNext();
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return listIterator.next();
	}

	@Override
	public boolean hasPrevious() {
		while (!listIterator.hasPrevious() && cursor > 0) {
			List<T> list = lists.get(--cursor);
			offset -= list.size();
			listIterator = list.listIterator(list.size());
		}
		return listIterator.hasPrevious();
	}

	@Override
	public T previous() {
		if (!hasPrevious())
			throw new NoSuchElementException();

		return listIterator.previous();
	}

	@Override
	public int nextIndex() {
		return listIterator.nextIndex() + offset;
	}

	@Override
	public int previousIndex() {
		return listIterator.previousIndex() + offset;
	}

	@Override
	public void remove() {
		listIterator.remove();
	}

	@Override
	public void set(T t) {
		listIterator.set(t);
	}

	@Override
	public void add(T t) {
		listIterator.add(t);
	}
}
