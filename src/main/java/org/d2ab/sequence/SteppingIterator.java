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

package org.d2ab.sequence;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SteppingIterator<T> implements Iterator<T> {
	private final Iterator<T> iterator;
	private final int step;
	private boolean gotNext;
	private T next;

	public SteppingIterator(Iterator<T> iterator, int step) {
		this.iterator = iterator;
		this.step = step;
	}

	@Override
	public boolean hasNext() {
		if (gotNext)
			return true;

		if (!iterator.hasNext())
			return false;

		next = iterator.next();

		// skip steps
		int i = step;
		while (--i > 0 && iterator.hasNext())
			iterator.next();
		gotNext = true;

		return true;
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		T result = next;
		next = null;
		gotNext = false;
		return result;
	}
}
