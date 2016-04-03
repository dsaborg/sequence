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

package org.d2ab.iterator.longs;

import org.d2ab.function.longs.LongBiPredicate;

import java.util.NoSuchElementException;

public class ForwardPeekingFilteringLongIterator extends DelegatingLongIterator<Long, LongIterator> {
	private long lastNext;
	private final LongBiPredicate predicate;

	private long next;
	private boolean hasNext;
	private long following;
	private boolean hasFollowing;
	private boolean started;

	public ForwardPeekingFilteringLongIterator(LongIterator iterator, long lastNext, LongBiPredicate predicate) {
		super(iterator);
		this.lastNext = lastNext;
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
			following = hasFollowing ? iterator.next() : lastNext;
		} while (!(hasNext = predicate.test(next, following)) && hasFollowing);

		return hasNext;
	}

	@Override
	public long nextLong() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		hasNext = false;
		return next;
	}
}
