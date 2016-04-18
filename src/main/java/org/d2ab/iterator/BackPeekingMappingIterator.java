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
 * Base class for iterators that map the next element by also peeking at the previous element.
 */
public abstract class BackPeekingMappingIterator<T, U> extends MappedReferenceIterator<T, U> {
	protected T previous;

	public BackPeekingMappingIterator(Iterator<T> iterator, T replacement) {
		super(iterator);
		this.previous = replacement;
	}

	@Override
	public U next() {
		if (!hasNext())
			throw new NoSuchElementException();

		T next = iterator.next();

		U mapped = map(previous, next);
		previous = next;
		return mapped;
	}

	protected abstract U map(T previous, T next);
}
