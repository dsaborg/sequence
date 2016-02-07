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

package org.d2ab.iterator.longs;

import java.util.function.LongBinaryOperator;

/**
 * An iterator over longs that also maps each element by looking at the current AND the previous element.
 */
public class BackPeekingMappingLongIterator extends UnaryLongIterator {
	private final long firstPrevious;
	private final LongBinaryOperator mapper;

	private boolean hasPrevious;
	private long previous;

	public BackPeekingMappingLongIterator(long firstPrevious, LongBinaryOperator mapper) {
		this.firstPrevious = firstPrevious;
		this.mapper = mapper;
	}

	@Override
	public long nextLong() {
		long next = iterator.nextLong();

		long result = mapper.applyAsLong(hasPrevious ? previous : firstPrevious, next);
		previous = next;
		hasPrevious = true;
		return result;
	}
}
