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

package org.d2ab.iterator.doubles;

import org.d2ab.collection.doubles.DoubleList;
import org.d2ab.iterator.DelegatingIterator;
import org.d2ab.sequence.DoubleSequence;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.DoublePredicate;

/**
 * An {@link Iterator} that can batch up another iterator by comparing two items in sequence and deciding whether
 * to split up in a batch on those items.
 */
public class SplittingDoubleIterator extends DelegatingIterator<Double, DoubleIterator, DoubleSequence> {
	private final DoublePredicate predicate;

	public SplittingDoubleIterator(DoubleIterator iterator, double element) {
		this(iterator, d -> d == element);
	}

	public SplittingDoubleIterator(DoubleIterator iterator, DoublePredicate predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public DoubleSequence next() {
		if (!hasNext())
			throw new NoSuchElementException();

		DoubleList buffer = DoubleList.create();
		while (iterator.hasNext()) {
			double next = iterator.nextDouble();
			if (predicate.test(next))
				break;
			buffer.addDouble(next);
		}

		return DoubleSequence.from(buffer);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
