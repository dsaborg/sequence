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
package org.d2ab.primitive.chars;

import java.util.BitSet;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

public class DistinctCharIterator implements CharIterator {
	private static final int THRESHOLD = 256;
	private CharIterator iterator;
	private Set<Character> seenHigh = new HashSet<Character>();
	private BitSet seenLow = new BitSet(THRESHOLD);
	private char next;
	private boolean gotNext;

	public DistinctCharIterator(CharIterator iterator) {
		this.iterator = iterator;
	}

	@Override
	public char nextChar() {
		if (!hasNext())
			throw new NoSuchElementException();

		gotNext = false;
		return next;
	}

	@Override
	public boolean hasNext() {
		if (gotNext)
			return true;

		while (!gotNext && iterator.hasNext()) {
			char next = iterator.nextChar();
			if (next < THRESHOLD)
				gotNext = add(seenLow, next);
			else
				gotNext = seenHigh.add(next);
			if (gotNext)
				this.next = next;
		}

		return gotNext;
	}

	private static boolean add(BitSet bitSet, int index) {
		boolean cleared = !bitSet.get(index);
		if (cleared) {
			bitSet.set(index);
		}
		return cleared;
	}
}
