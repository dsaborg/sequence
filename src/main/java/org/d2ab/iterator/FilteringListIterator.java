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

import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static org.d2ab.iterator.FilteringListIterator.State.*;

/**
 * A {@link ListIterator} that provides a filtered view of another {@link ListIterator}. All operations are supported
 * except {@link #add}.
 *
 * @since 1.2
 */
public class FilteringListIterator<T> extends DelegatingTransformingIterator<T, ListIterator<T>, T>
		implements ListIterator<T> {
	private final Predicate<? super T> predicate;

	private State state = INIT;
	private boolean nextOrPrevious;
	private boolean addOrRemove;

	private boolean hasNextOrPreviousCached;
	private T cachedNextOrPrevious;

	private int cursor;

	enum State {INIT, HAS_NEXT, NEXT, HAS_PREVIOUS, PREVIOUS}

	public FilteringListIterator(ListIterator<T> iterator, Predicate<? super T> predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (state == HAS_NEXT)
			return hasNextOrPreviousCached;

		if (state == HAS_PREVIOUS && hasNextOrPreviousCached)
			iterator.next();

		hasNextOrPreviousCached = false;
		while (iterator.hasNext()) {
			T maybeNext = iterator.next();
			if (predicate.test(maybeNext)) {
				cachedNextOrPrevious = maybeNext;
				hasNextOrPreviousCached = true;
				break;
			}
		}

		state = HAS_NEXT;
		return hasNextOrPreviousCached;
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		state = NEXT;
		addOrRemove = false;
		nextOrPrevious = true;
		hasNextOrPreviousCached = false;
		cursor++;

		return cachedNextOrPrevious;
	}

	@Override
	public boolean hasPrevious() {
		if (state == HAS_PREVIOUS)
			return hasNextOrPreviousCached;

		if (state == HAS_NEXT && hasNextOrPreviousCached)
			iterator.previous();

		hasNextOrPreviousCached = false;
		while (iterator.hasPrevious()) {
			T maybePrevious = iterator.previous();
			if (predicate.test(maybePrevious)) {
				cachedNextOrPrevious = maybePrevious;
				hasNextOrPreviousCached = true;
				break;
			}
		}

		state = HAS_PREVIOUS;
		return hasNextOrPreviousCached;
	}

	@Override
	public T previous() {
		if (!hasPrevious())
			throw new NoSuchElementException();

		state = PREVIOUS;
		addOrRemove = false;
		nextOrPrevious = true;
		hasNextOrPreviousCached = false;
		cursor--;

		return cachedNextOrPrevious;
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
		if (!nextOrPrevious)
			throw new IllegalStateException("next or previous not called");
		if (addOrRemove)
			throw new IllegalStateException("add or remove called");

		iterator.remove();
		if (state == NEXT)
			cursor--;

		addOrRemove = true;
	}

	@Override
	public void set(T t) {
		if (!nextOrPrevious)
			throw new IllegalStateException("next or previous not called");
		if (addOrRemove)
			throw new IllegalStateException("add or remove called");

		if (!predicate.test(t))
			throw new IllegalArgumentException(String.valueOf(t));

		iterator.set(t);
	}

	@Override
	public void add(T t) {
		if ((state == HAS_NEXT || state == HAS_PREVIOUS) && hasNextOrPreviousCached)
			throw new IllegalStateException("cannot add immediately after hasNext or hasPrevious");

		if (!predicate.test(t))
			throw new IllegalArgumentException(String.valueOf(t));

		iterator.add(t);
		cursor++;

		addOrRemove = true;
	}
}
