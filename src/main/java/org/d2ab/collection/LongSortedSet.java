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

import org.d2ab.iterator.longs.LongIterator;

import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A primitive specialization of {@link SortedSet} for {@code long} values.
 */
public interface LongSortedSet extends SortedSet<Long>, LongSet {
	@Override
	default LongComparator comparator() {
		return null;
	}

	@Override
	default LongSortedSet subSet(Long fromElement, Long toElement) {
		return subSet((long) fromElement, (long) toElement);
	}

	default LongSortedSet subSet(long fromElement, long toElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	default LongSortedSet headSet(Long toElement) {
		return headSet((long) toElement);
	}

	default LongSortedSet headSet(long toElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	default LongSortedSet tailSet(Long fromElement) {
		return tailSet((long) fromElement);
	}

	default LongSortedSet tailSet(long fromElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	default Long first() {
		return firstLong();
	}

	default long firstLong() {
		return iterator().nextLong();
	}

	@Override
	default Long last() {
		return lastLong();
	}

	default long lastLong() {
		LongIterator iterator = iterator();
		long last;
		do
			last = iterator.nextLong();
		while (iterator.hasNext());
		return last;
	}

	@Override
	default Spliterator.OfLong spliterator() {
		return Spliterators.spliterator(iterator(), size(),
		                                Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED |
		                                Spliterator.NONNULL);
	}
}
