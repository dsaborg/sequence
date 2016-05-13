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

import java.util.Iterator;
import java.util.ListIterator;

/**
 * A {@link ListIterator} based on a regular {@link Iterator} that supports forward traversal only, and removal only.
 */
public class ForwardListIterator<T> implements ListIterator<T> {
	private final Iterator<T> iterator;
	private int cursor;

	public ForwardListIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public T next() {
		cursor++;
		return iterator.next();
	}

	@Override
	public boolean hasPrevious() {
		throw new UnsupportedOperationException();
	}

	@Override
	public T previous() {
		throw new UnsupportedOperationException();
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
		iterator.remove();
		cursor--;
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
