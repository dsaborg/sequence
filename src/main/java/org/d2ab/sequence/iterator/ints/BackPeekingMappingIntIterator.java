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

package org.d2ab.sequence.iterator.ints;

import org.d2ab.collection.iterator.IntIterator;
import org.d2ab.collection.iterator.UnaryIntIterator;

import java.util.function.IntBinaryOperator;

/**
 * An iterator over ints that also maps each element by looking at the current AND the previous element.
 */
public class BackPeekingMappingIntIterator extends UnaryIntIterator {
	private final int firstPrevious;
	private final IntBinaryOperator mapper;
	private boolean hasPrevious;
	private int previous = -1;

	public BackPeekingMappingIntIterator(IntIterator iterator, int firstPrevious, IntBinaryOperator mapper) {
		super(iterator);
		this.firstPrevious = firstPrevious;
		this.mapper = mapper;
	}

	@Override
	public int nextInt() {
		int next = iterator.nextInt();

		int result = mapper.applyAsInt(hasPrevious ? previous : firstPrevious, next);
		previous = next;
		hasPrevious = true;
		return result;
	}
}
