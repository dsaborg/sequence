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

package org.d2ab.iterator.longs;

import java.util.NoSuchElementException;

import static org.d2ab.iterator.DelegatingTransformingIterator.State.HAS_NEXT;
import static org.d2ab.iterator.DelegatingTransformingIterator.State.NEXT;

public class SteppingLongIterator extends DelegatingUnaryLongIterator {
	private final int step;

	private boolean skipOnHasNext;

	public SteppingLongIterator(LongIterator iterator, int step) {
		super(iterator);
		this.step = step;
	}

	@Override
	public boolean hasNext() {
		if (skipOnHasNext) {
			iterator.skip(step - 1);
			skipOnHasNext = false;
		}

		state = HAS_NEXT;
		return iterator.hasNext();
	}

	@Override
	public long nextLong() {
		if (!hasNext())
			throw new NoSuchElementException();

		skipOnHasNext = true;
		state = NEXT;
		return iterator.nextLong();
	}

	@Override
	public void remove() {
		if (state == HAS_NEXT)
			throw new IllegalStateException("Cannot remove immediately after calling hasNext()");
		if (state != NEXT)
			throw new IllegalStateException("Can only remove after calling next()");

		super.remove();
	}
}
