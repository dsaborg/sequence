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

package org.d2ab.primitive.doubles;

import java.util.function.DoubleConsumer;

public class PeekingDoubleIterator implements DoubleIterator {
	private final DoubleIterator iterator;
	private final DoubleConsumer action;

	public PeekingDoubleIterator(DoubleIterator iterator, DoubleConsumer action) {
		this.iterator = iterator;
		this.action = action;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public double nextDouble() {
		double next = iterator.nextDouble();
		action.accept(next);
		return next;
	}
}
