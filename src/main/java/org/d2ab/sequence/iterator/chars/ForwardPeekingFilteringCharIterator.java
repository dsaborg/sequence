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

package org.d2ab.sequence.iterator.chars;

import org.d2ab.collection.iterator.CharIterator;
import org.d2ab.collection.iterator.DelegatingCharIterator;
import org.d2ab.function.CharBiPredicate;

import java.util.NoSuchElementException;

public class ForwardPeekingFilteringCharIterator extends DelegatingCharIterator<Character, CharIterator> {
	private char lastNext;
	private final CharBiPredicate predicate;

	private char next;
	private boolean hasNext;
	private char following;
	private boolean hasFollowing;
	private boolean started;

	public ForwardPeekingFilteringCharIterator(CharIterator iterator, char lastNext, CharBiPredicate predicate) {
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
				following = iterator.nextChar();
				hasFollowing = true;
			}
			started = true;
		}
		if (!hasFollowing)
			return false;

		do {
			next = following;
			hasFollowing = iterator.hasNext();
			following = hasFollowing ? iterator.nextChar() : lastNext;
		} while (!(hasNext = predicate.test(next, following)) && hasFollowing);

		return hasNext;
	}

	@Override
	public char nextChar() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		hasNext = false;
		return next;
	}
}
