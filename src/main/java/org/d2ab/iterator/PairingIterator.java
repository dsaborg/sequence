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

import org.d2ab.sequence.Pair;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PairingIterator<T> implements Iterator<Pair<T, T>> {
	private Iterator<T> iterator;
	private T previous;
	private boolean gotPrevious;

	public PairingIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		boolean hasNext = iterator.hasNext();
		if (gotPrevious || !hasNext) {
			return hasNext;
		}

		previous = iterator.next();
		gotPrevious = true;
		return iterator.hasNext();
	}

	@Override
	public Pair<T, T> next() {
		if (!iterator.hasNext())
			throw new NoSuchElementException();

		T next = iterator.next();
		Pair result = Pair.of(previous, next);
		previous = next;
		return result;
	}
}
