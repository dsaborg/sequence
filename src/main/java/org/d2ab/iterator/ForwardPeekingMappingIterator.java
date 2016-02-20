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
import java.util.function.BiFunction;

/**
 * An iterator that maps each element by looking at the next AND the following element.
 */
public class ForwardPeekingMappingIterator<T, U> extends DelegatingReferenceIterator<T, U> {
	private final BiFunction<? super T, ? super T, ? extends U> mapper;

	private T next;
	private boolean hasNext;
	private boolean started;

	public ForwardPeekingMappingIterator(Iterator<T> iterator, BiFunction<? super T, ? super T, ? extends U> mapper) {
		super(iterator);
		this.mapper = mapper;
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
	public U next() {
		if (!hasNext())
			throw new NoSuchElementException();

		boolean hasFollowing = iterator.hasNext();
		T following = hasFollowing ? iterator.next() : null;

		U mapped = mapper.apply(next, following);
		this.next = following;
		hasNext = hasFollowing;
		return mapped;
	}
}
