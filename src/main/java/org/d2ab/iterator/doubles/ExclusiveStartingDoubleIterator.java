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

import org.d2ab.util.primitive.Doubles;

import java.util.NoSuchElementException;
import java.util.function.DoublePredicate;

public class ExclusiveStartingDoubleIterator extends UnaryDoubleIterator {
	private final DoublePredicate predicate;

	private boolean started;

	public ExclusiveStartingDoubleIterator(DoubleIterator iterator, double element, double accuracy) {
		this(iterator, d -> Doubles.equal(d, element, accuracy));
	}

	public ExclusiveStartingDoubleIterator(DoubleIterator iterator, DoublePredicate predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (!started) {
			while (iterator.hasNext())
				if (predicate.test(iterator.nextDouble()))
					break;
			started = true;
		}
		return super.hasNext();
	}

	@Override
	public double nextDouble() {
		if (!hasNext())
			throw new NoSuchElementException();

		return iterator.nextDouble();
	}
}
