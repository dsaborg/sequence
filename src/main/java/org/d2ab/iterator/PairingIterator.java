/*
 * Copyright 2015 Daniel Skogquist Åborg
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

import javax.annotation.Nullable;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

public abstract class PairingIterator<T, E extends Entry<T, T>> extends DelegatingReferenceIterator<T, E> {
	private T previous;
	private boolean hasPrevious;
	private boolean started;

	@Override
	public boolean hasNext() {
		if (!hasPrevious) {
			boolean hasNext = iterator.hasNext();

			// First time, get the first element so we have a base for the first pair
			if (hasNext) {
				previous = iterator.next();
				hasPrevious = true;
			}

			return hasNext;
		}

		// Ensure first element is processed with a trailing null if iterator contains only one item
		return !started || iterator.hasNext();
	}

	@Override
	public E next() {
		if (!hasNext())
			throw new NoSuchElementException();

		T next = iterator.hasNext() ? iterator.next() : null;
		E result = makeEntry(previous, next);
		previous = next;
		started = true;
		return result;
	}

	protected abstract E makeEntry(T previous, @Nullable T next);
}
