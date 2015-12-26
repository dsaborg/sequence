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

import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;

public class ConcatenatingIterator<T> implements Iterator<T> {
	private final Iterator<? extends Iterable<? extends T>> iterables;
	private Iterator<? extends T> iterator;

	@SafeVarargs
	public ConcatenatingIterator(Iterable<? extends T>... iterables) {
		this(asList(iterables));
	}

	public ConcatenatingIterator(Iterable<? extends Iterable<? extends T>> iterables) {
		this.iterables = iterables.iterator();
	}

	@Override
	public boolean hasNext() {
		if ((iterator == null || !iterator.hasNext()) && iterables.hasNext()) {
			iterator = iterables.next().iterator();
		}
		return iterator != null && iterator.hasNext();
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		return iterator.next();
	}
}
