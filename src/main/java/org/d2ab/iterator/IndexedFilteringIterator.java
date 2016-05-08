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

import org.d2ab.function.ObjLongPredicate;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IndexedFilteringIterator<T> extends ReferenceIterator<T> {
	private final ObjLongPredicate<? super T> predicate;

	private T next;
	private boolean hasNext;
	private long index;

	public IndexedFilteringIterator(Iterator<T> iterator, ObjLongPredicate<? super T> predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		// already checked?
		if (hasNext)
			return true;

		do {
			// find next matching, bail out if EOF
			hasNext = iterator.hasNext();
			if (!hasNext)
				return false;
			next = iterator.next();
		} while (!predicate.test(next, index++));

		// found matching value
		return true;
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		hasNext = false;
		return next;
	}
}
