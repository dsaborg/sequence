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

package org.d2ab.collection;

import org.d2ab.iterator.ints.IntIterator;

import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A primitive specialization of {@link SortedSet} for {@code int} values.
 */
public interface IntSortedSet extends SortedSet<Integer>, IntSet {
	@Override
	default IntComparator comparator() {
		return null;
	}

	@Override
	default IntSortedSet subSet(Integer fromElement, Integer toElement) {
		return subSet((int) fromElement, (int) toElement);
	}

	default IntSortedSet subSet(int fromElement, int toElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	default IntSortedSet headSet(Integer toElement) {
		return headSet((int) toElement);
	}

	default IntSortedSet headSet(int toElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	default IntSortedSet tailSet(Integer fromElement) {
		return tailSet((int) fromElement);
	}

	default IntSortedSet tailSet(int fromElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	default Integer first() {
		return firstInt();
	}

	default int firstInt() {
		return iterator().nextInt();
	}

	@Override
	default Integer last() {
		return lastInt();
	}

	default int lastInt() {
		IntIterator iterator = iterator();
		int last;
		do
			last = iterator.nextInt();
		while (iterator.hasNext());
		return last;
	}

	@Override
	default Spliterator.OfInt spliterator() {
		return Spliterators.spliterator(iterator(), size(),
		                                Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED |
		                                Spliterator.NONNULL);
	}
}
