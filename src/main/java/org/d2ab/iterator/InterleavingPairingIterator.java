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

import org.d2ab.sequence.Pair;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An {@link Iterator} that interleaves the streams of two {@link Iterator}s with each other into a stream of
 * {@link Pair}s.
 */
public class InterleavingPairingIterator<T, U> implements Iterator<Pair<T, U>> {
	@Nonnull
	private final Iterator<? extends T> first;
	@Nonnull
	private final Iterator<? extends U> second;

	public InterleavingPairingIterator(@Nonnull Iterator<? extends T> first, @Nonnull Iterator<? extends U> second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean hasNext() {
		return first.hasNext() || second.hasNext();
	}

	@Override
	public Pair<T, U> next() {
		if (!hasNext())
			throw new NoSuchElementException();

		T nextFirst = first.hasNext() ? first.next() : null;
		U nextSecond = second.hasNext() ? second.next() : null;
		return Pair.of(nextFirst, nextSecond);
	}
}
