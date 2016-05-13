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

package org.d2ab.collection.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A {@link ListIterator} based on a regular {@link Iterator} which supports both forwards and backwards traversal
 * by caching the previous values. {@link ListIterator#remove()} is only supported if the {@link Iterator} supports it,
 * and not after moving backwards in the iterator.
 */
public class CachingListIterator<T> implements ListIterator<T> {
	private final Iterator<T> iterator;
	private final List<T> previous = new ArrayList<>();

	private int cursor;

	public CachingListIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return cursor < previous.size() || iterator.hasNext();
	}

	@Override
	public T next() {
		if (cursor < previous.size())
			return previous.get(cursor++);

		cursor++;

		T next = iterator.next();
		previous.add(next);
		return next;
	}

	@Override
	public boolean hasPrevious() {
		return cursor > 0;
	}

	@Override
	public T previous() {
		return previous.get(--cursor);
	}

	@Override
	public int nextIndex() {
		return cursor;
	}

	@Override
	public int previousIndex() {
		return cursor - 1;
	}

	@Override
	public void remove() {
		if (cursor < previous.size())
			throw new IllegalStateException("Cannot remove after previous");
		iterator.remove();
		previous.remove(--cursor);
	}

	@Override
	public void set(T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(T t) {
		throw new UnsupportedOperationException();
	}
}
