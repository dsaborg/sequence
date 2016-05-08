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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Base class for iterators the can peek at the following item of each item in the iteration.
 */
public abstract class ForwardPeekingMappingIterator<T, U> extends DelegatingReferenceIterator<T, U> {
	private final T replacement;
	protected T next;
	protected boolean hasNext;
	private boolean started;

	public ForwardPeekingMappingIterator(Iterator<T> iterator, T replacement) {
		super(iterator);
		this.replacement = replacement;
	}

	@Override
	public boolean hasNext() {
		if (!started) {
			if (iterator.hasNext()) {
				next = iterator.next();
				hasNext = true;
			}
			started = true;
		}
		return hasNext;
	}

	@Override
	public U next() {
		if (!hasNext())
			throw new NoSuchElementException();

		boolean hasFollowing = iterator.hasNext();
		T following = mapFollowing(hasFollowing, hasFollowing ? iterator.next() : replacement);
		U mapped = mapNext(next, following);
		next = following;
		hasNext = hasFollowing;
		return mapped;
	}

	protected abstract T mapFollowing(boolean hasFollowing, T following);

	protected abstract U mapNext(T next, T following);

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
