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

package org.d2ab.iterator.list;

import org.d2ab.iterator.DelegatingIterator;

import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static org.d2ab.iterator.list.FilteringListIterator.State.*;

/**
 * A {@link ListIterator} that provides a filtered view of another {@link ListIterator}. All operations are supported
 * except {@link #add}.
 *
 * @since 1.2
 */
public class FilteringListIterator<T> extends DelegatingIterator<T, ListIterator<T>, T> implements ListIterator<T> {
	private final Predicate<? super T> predicate;

	private State state = State.INIT;

	private boolean hasCached;
	private T cached;

	private int cursor = 0;

	enum State { INIT, HAS_NEXT, NEXT, HAS_PREVIOUS, PREVIOUS}

	public FilteringListIterator(ListIterator<T> iterator, Predicate<? super T> predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (state == HAS_PREVIOUS && hasCached)
			iterator.next();

		if (state == HAS_NEXT)
			return hasCached;

		state = HAS_NEXT;

		do {
			hasCached = iterator.hasNext();
			if (!hasCached)
				return false;
			cached = iterator.next();
		} while (!predicate.test(cached));

		return hasCached;
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		state = NEXT;

		cursor++;

		return cached;
	}

	@Override
	public boolean hasPrevious() {
		if (state == HAS_NEXT && hasCached)
			iterator.previous();

		if (state == HAS_PREVIOUS)
			return hasCached;

		state = HAS_PREVIOUS;

		do {
			hasCached = iterator.hasPrevious();
			if (!hasCached)
				return false;
			cached = iterator.previous();
		} while (!predicate.test(cached));

		return hasCached;
	}

	@Override
	public T previous() {
		if (!hasPrevious())
			throw new NoSuchElementException();

		state = PREVIOUS;

		cursor--;

		return cached;
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
		if (state != PREVIOUS && state != NEXT)
			throw new IllegalStateException("Next or previous not called");

		iterator.remove();
		if (state == NEXT)
			cursor--;
	}

	@Override
	public void set(T t) {
		if (state != PREVIOUS && state != NEXT)
			throw new IllegalStateException("Next or previous not called");

		if (!predicate.test(t))
			throw new IllegalArgumentException("Invalid element: " + t);

		iterator.set(t);
	}

	@Override
	public void add(T t) {
		throw new UnsupportedOperationException();
	}
}
