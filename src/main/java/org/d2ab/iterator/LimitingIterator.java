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

public class LimitingIterator<T> extends DelegatingUnaryIterator<T> {
	private final int limit;
	private int count;

	public LimitingIterator(Iterator<T> iterator, int limit) {
		super(iterator);
		this.limit = limit;
	}

	@Override
	public boolean hasNext() {
		return count < limit && iterator.hasNext();
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		T next = iterator.next();
		count++;
		return next;
	}
}
