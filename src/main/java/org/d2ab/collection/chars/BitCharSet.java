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

package org.d2ab.collection.chars;

import org.d2ab.collection.SparseBitSet;
import org.d2ab.iterator.chars.CharIterator;

import java.util.Set;

/**
 * An implementation of {@link CharSortedSet} backed by two {@link SparseBitSet}s for positive and negative values.
 */
public class BitCharSet implements CharSortedSet {
	private final SparseBitSet values = new SparseBitSet();

	public BitCharSet(char... xs) {
		addAllChars(xs);
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public CharIterator iterator() {
		return CharIterator.from(values.iterator());
	}

	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}

	@Override
	public void clear() {
		values.clear();
	}

	@Override
	public boolean addChar(char x) {
		return values.set(x);
	}

	@Override
	public boolean removeChar(char x) {
		return values.clear(x);
	}

	@Override
	public boolean containsChar(char x) {
		return values.get(x);
	}

	@Override
	public char firstChar() {
		return (char) (values.firstLong() + Character.MIN_VALUE);
	}

	@Override
	public char lastChar() {
		return (char) (values.lastLong() + Character.MIN_VALUE);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(size() * 3); // heuristic
		builder.append("[");

		boolean started = false;
		for (CharIterator iterator = iterator(); iterator.hasNext(); ) {
			if (started)
				builder.append(", ");
			else
				started = true;
			builder.append(iterator.nextChar());
		}

		builder.append("]");
		return builder.toString();
	}

	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Set))
			return false;

		Set<?> that = (Set<?>) o;
		return size() == that.size() && containsAll(that);
	}

	public int hashCode() {
		int hashCode = 0;
		for (CharIterator iterator = iterator(); iterator.hasNext(); )
			hashCode += Character.hashCode(iterator.nextChar());
		return hashCode;
	}
}
