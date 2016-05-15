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

import org.d2ab.collection.iterator.DoubleIterator;
import org.d2ab.collection.iterator.UnaryDoubleIterator;

import java.util.function.DoubleBinaryOperator;

/**
 * An iterator over doubles that also maps each element by looking at the current AND the previous element.
 */
public class BackPeekingMappingDoubleIterator extends UnaryDoubleIterator {
	private final double firstPrevious;
	private final DoubleBinaryOperator mapper;
	private boolean hasPrevious;
	private double previous = -1;

	public BackPeekingMappingDoubleIterator(DoubleIterator iterator, double firstPrevious,
	                                        DoubleBinaryOperator mapper) {
		super(iterator);
		this.firstPrevious = firstPrevious;
		this.mapper = mapper;
	}

	@Override
	public double nextDouble() {
		double next = iterator.nextDouble();

		double result = mapper.applyAsDouble(hasPrevious ? previous : firstPrevious, next);

		previous = next;
		hasPrevious = true;
		return result;
	}
}
