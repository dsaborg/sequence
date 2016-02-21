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

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.BiPredicate;

/**
 * An {@link Iterator} that swaps any pair of items in the iteration that match the given predicate.
 */
public class SwappingIterator<T> extends ForwardPeekingMappingIterator<T, T> {
	private final BiPredicate<? super T, ? super T> swapPredicate;

	public SwappingIterator(Iterator<T> iterator, BiPredicate<? super T, ? super T> swapPredicate) {
		super(iterator);
		this.swapPredicate = swapPredicate;
	}

	@Override
	protected T mapFollowing(boolean hasFollowing, @Nullable T following) {
		if (!hasFollowing || !swapPredicate.test(next, following)) {
			return following;
		}

		T swap = next;
		next = following;
		return swap;
	}

	@Override
	protected T mapNext(@Nullable T following) {
		return next;
	}
}

