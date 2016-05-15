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

package org.d2ab.sequence.iterator.doubles;

import org.d2ab.collection.DoubleComparator;
import org.d2ab.collection.iterator.DoubleIterator;
import org.d2ab.collection.iterator.UnaryDoubleIterator;

import java.util.NoSuchElementException;
import java.util.function.DoublePredicate;

public class ExclusiveTerminalDoubleIterator extends UnaryDoubleIterator {
	private final DoublePredicate terminal;

	private double next;
	private boolean hasNext;

	public ExclusiveTerminalDoubleIterator(DoubleIterator iterator, double terminal, double accuracy) {
		this(iterator, d -> DoubleComparator.compare(d, terminal, accuracy));
	}

	public ExclusiveTerminalDoubleIterator(DoubleIterator iterator, DoublePredicate terminal) {
		super(iterator);
		this.terminal = terminal;
	}

	@Override
	public boolean hasNext() {
		if (!hasNext && iterator.hasNext()) {
			next = iterator.next();
			hasNext = true;
		}
		return hasNext && !terminal.test(next);
	}

	@Override
	public double nextDouble() {
		if (!hasNext())
			throw new NoSuchElementException();

		hasNext = false;
		return next;
	}
}
