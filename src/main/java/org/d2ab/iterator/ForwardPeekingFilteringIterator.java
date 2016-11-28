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
import java.util.function.BiPredicate;

public class ForwardPeekingFilteringIterator<T> extends DelegatingUnaryIterator<T> {
	private final T replacement;
	private final BiPredicate<? super T, ? super T> predicate;

	private T next;
	private boolean hasNext;
	private T following;
	private boolean hasFollowing;
	private boolean started;

	public ForwardPeekingFilteringIterator(Iterator<T> iterator, T replacement,
	                                       BiPredicate<? super T, ? super T> predicate) {
		super(iterator);
		this.replacement = replacement;
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (hasNext)
			return true;
		if (!started) {
			if (iterator.hasNext()) {
				following = iterator.next();
				hasFollowing = true;
			}
			started = true;
		}
		if (!hasFollowing)
			return false;

		do {
			next = following;
			hasFollowing = iterator.hasNext();
			following = hasFollowing ? iterator.next() : replacement;
		} while (!(hasNext = predicate.test(next, following)) && hasFollowing);

		return hasNext;
	}

	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		hasNext = false;
		return next;
	}
}
