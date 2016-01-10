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

package org.d2ab.primitive.ints;

import java.util.NoSuchElementException;
import java.util.Objects;

public class ExclusiveTerminalIntIterator implements IntIterator {
	private final IntIterator iterator;
	private final int terminal;
	private int next;
	private boolean hasNext;

	public ExclusiveTerminalIntIterator(IntIterator iterator, int terminal) {
		this.iterator = iterator;
		this.terminal = terminal;
	}

	@Override
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();

		hasNext = false;
		return next;
	}

	@Override
	public boolean hasNext() {
		if (!hasNext && iterator.hasNext()) {
			next = iterator.next();
			hasNext = true;
		}
		return hasNext && !Objects.equals(next, terminal);
	}
}
