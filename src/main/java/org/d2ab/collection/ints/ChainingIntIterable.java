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

package org.d2ab.collection.ints;

import org.d2ab.iterator.ints.ChainingIntIterator;
import org.d2ab.iterator.ints.IntIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * An {@link IntIterable} that can chain together several {@link IntIterable}s into one unbroken sequence.
 */
public class ChainingIntIterable implements IntIterable {
	private final Collection<IntIterable> iterables = new ArrayList<>();

	public ChainingIntIterable(IntIterable... iterables) {
		Collections.addAll(this.iterables, iterables);
	}

	@Override
	public IntIterator iterator() {
		return new ChainingIntIterator(iterables);
	}
}
