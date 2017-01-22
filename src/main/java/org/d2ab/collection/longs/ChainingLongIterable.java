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

package org.d2ab.collection.longs;

import org.d2ab.collection.Iterables;
import org.d2ab.iterator.longs.ChainingLongIterator;
import org.d2ab.iterator.longs.LongIterator;

/**
 * A {@link LongIterable} that can chain together several {@link LongIterable}s into one unbroken sequence.
 */
public class ChainingLongIterable implements LongIterable {
	private final Iterable<LongIterable> iterables;

	public ChainingLongIterable(LongIterable... iterables) {
		this.iterables = Iterables.of(iterables);
	}

	@Override
	public LongIterator iterator() {
		return new ChainingLongIterator(iterables);
	}
}
