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

import javax.annotation.Nullable;
import java.util.*;

/**
 * An {@link Iterator} that interleaves the streams of two other {@link Iterator}s with each other.
 */
public class InterleavingIterator<T> implements Iterator<T> {
	private final List<Iterator<T>> iterators = new ArrayList<>();
	private int current;

	@SafeVarargs
	public InterleavingIterator(Iterable<T>... iterables) {
		for (Iterable<T> iterable : iterables) {
			Iterator<T> iterator = Objects.requireNonNull(iterable).iterator();
			iterators.add(Objects.requireNonNull(iterator));
		}
	}

	@Override
	public boolean hasNext() {
		for (Iterator iterator : iterators)
			if (iterator.hasNext())
				return true;
		return false;
	}

	@Override
	@Nullable
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		while (!iterators.get(current).hasNext())
			advance();

		Iterator<T> iterator = iterators.get(current);
		advance();
		return iterator.next();
	}

	private void advance() {
		current = (current + 1) % iterators.size();
	}
}
