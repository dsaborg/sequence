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

package org.d2ab.iterator.ints;

import org.d2ab.collection.ints.IntList;
import org.d2ab.iterator.DelegatingTransformingIterator;
import org.d2ab.sequence.IntSequence;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.IntPredicate;

/**
 * An {@link Iterator} that can batch up another iterator by comparing two items in sequence and deciding whether
 * to split up in a batch on those items.
 */
public class SplittingIntIterator extends DelegatingTransformingIterator<Integer, IntIterator, IntSequence> {
	private final IntPredicate predicate;

	public SplittingIntIterator(IntIterator iterator, int element) {
		this(iterator, i -> i == element);
	}

	public SplittingIntIterator(IntIterator iterator, IntPredicate predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public IntSequence next() {
		if (!hasNext())
			throw new NoSuchElementException();

		IntList buffer = IntList.create();
		while (iterator.hasNext()) {
			int next = iterator.nextInt();
			if (predicate.test(next))
				break;

			buffer.addInt(next);
		}

		return IntSequence.from(buffer);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
