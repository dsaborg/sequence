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

package org.d2ab.sequence.iterator;

import org.d2ab.collection.iterator.DelegatingReferenceIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class PairingIterator<T, E> extends DelegatingReferenceIterator<T, E> {
	private final int step;

	private T previous;
	private boolean hasPrevious;

	public PairingIterator(Iterator<T> iterator, int step) {
		super(iterator);
		this.step = step;
	}

	@Override
	public boolean hasNext() {
		return hasPrevious && step > 1 || iterator.hasNext();
	}

	@Override
	public E next() {
		if (!hasNext())
			throw new NoSuchElementException();

		// First time, get the first element so we have a base for the first pair
		if (!hasPrevious && iterator.hasNext()) {
			previous = iterator.next();
			hasPrevious = true;
		}

		boolean hasNext = iterator.hasNext();
		T next = hasNext ? iterator.next() : null;
		E result = pair(previous, next);

		previous = next;
		hasPrevious = hasNext;

		for (int i = 1; i < step && hasPrevious; i++) {
			if (iterator.hasNext()) {
				previous = iterator.next();
			} else {
				previous = null;
				hasPrevious = false;
			}
		}

		return result;
	}

	protected abstract E pair(T first, T second);

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
