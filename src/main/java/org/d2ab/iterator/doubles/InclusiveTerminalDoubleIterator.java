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

package org.d2ab.iterator.doubles;

import org.d2ab.util.primitive.Doubles;

import java.util.NoSuchElementException;
import java.util.function.DoublePredicate;

public class InclusiveTerminalDoubleIterator extends UnaryDoubleIterator {
	private final DoublePredicate terminal;

	private double previous;
	private boolean hasPrevious;

	public InclusiveTerminalDoubleIterator(DoubleIterator iterator, double terminal, double accuracy) {
		this(iterator, d -> Doubles.equal(d, terminal, accuracy));
	}

	public InclusiveTerminalDoubleIterator(DoubleIterator iterator, DoublePredicate terminal) {
		super(iterator);
		this.terminal = terminal;
	}

	@Override
	public double nextDouble() {
		if (!hasNext())
			throw new NoSuchElementException();

		hasPrevious = true;
		return previous = iterator.next();
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext() && (!hasPrevious || !terminal.test(previous));
	}
}
