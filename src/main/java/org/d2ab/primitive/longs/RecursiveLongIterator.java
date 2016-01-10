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

package org.d2ab.primitive.longs;

import java.util.function.LongUnaryOperator;

public class RecursiveLongIterator implements LongIterator {
	private final long seed;
	private final LongUnaryOperator op;
	private long previous;
	private boolean hasPrevious;

	public RecursiveLongIterator(long seed, LongUnaryOperator op) {
		this.seed = seed;
		this.op = op;
	}

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public long nextLong() {
		previous = hasPrevious ? op.applyAsLong(previous) : seed;
		hasPrevious = true;
		return previous;
	}
}
