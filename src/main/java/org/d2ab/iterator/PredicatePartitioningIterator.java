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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;

/**
 * Base class for iterators the can peek at the following item of each item in the iteration.
 */
public class PredicatePartitioningIterator<T> extends DelegatingReferenceIterator<T, List<T>> {
	private final BiPredicate<? super T, ? super T> predicate;
	private T next;
	private boolean hasNext;

	public PredicatePartitioningIterator(Iterator<T> iterator, BiPredicate<? super T, ? super T> predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (!hasNext && super.hasNext()) {
			next = iterator.next();
			hasNext = true;
		}
		return hasNext;
	}

	@Override
	public List<T> next() {
		if (!hasNext())
			throw new NoSuchElementException();

		List<T> buffer = new ArrayList<>();
		do {
			buffer.add(next);
			hasNext = iterator.hasNext();
			if (!hasNext)
				break;
			T following = iterator.next();
			boolean split = predicate.test(next, following);
			next = following;
			if (split)
				break;
		} while (hasNext);
		return buffer;
	}
}
