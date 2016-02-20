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
import java.util.function.BiPredicate;

/**
 * An {@link Iterator} that swaps any pair of items in the iteration that match the given predicate.
 */
public class SwappingIterator<T> extends UnaryReferenceIterator<T> {
	private final BiPredicate<? super T, ? super T> swapper;

	private T next;
	private boolean hasNext;
	private boolean started;

	public SwappingIterator(Iterator<T> iterator, BiPredicate<? super T, ? super T> swapper) {
		super(iterator);
		this.swapper = swapper;
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
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		boolean hasFollowing = iterator.hasNext();
		T following;
		if (hasFollowing) {
			following = iterator.next();
			if (swapper.test(next, following)) {
				T temp = next;
				next = following;
				following = temp;
			}
		} else {
			following = null;
		}

		T result = next;
		next = following;
		hasNext = hasFollowing;
		return result;
	}
}

