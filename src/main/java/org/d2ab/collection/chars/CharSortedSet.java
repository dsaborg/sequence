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

import org.d2ab.iterator.chars.CharIterator;

import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A primitive specialization of {@link SortedSet} for {@code char} values.
 */
public interface CharSortedSet extends SortedSet<Character>, CharSet {
	@Override
	default CharComparator comparator() {
		return null;
	}

	@Override
	default CharSortedSet subSet(Character fromElement, Character toElement) {
		return subSet((char) fromElement, (char) toElement);
	}

	default CharSortedSet subSet(char fromElement, char toElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	default CharSortedSet headSet(Character toElement) {
		return headSet((char) toElement);
	}

	default CharSortedSet headSet(char toElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	default CharSortedSet tailSet(Character fromElement) {
		return tailSet((char) fromElement);
	}

	default CharSortedSet tailSet(char fromElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	default Character first() {
		return firstChar();
	}

	default char firstChar() {
		return iterator().nextChar();
	}

	@Override
	default Character last() {
		return lastChar();
	}

	default char lastChar() {
		CharIterator iterator = iterator();
		char last;
		do
			last = iterator.nextChar();
		while (iterator.hasNext());
		return last;
	}

	@Override
	default Spliterator.OfInt intSpliterator() {
		return Spliterators.spliterator(intIterator(), size(),
		                                Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED |
		                                Spliterator.NONNULL);
	}
}
