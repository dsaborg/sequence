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

package org.d2ab.iterator;

import java.util.Iterator;

/**
 * Base class for {@link Iterator}s.
 */
public abstract class BaseIterator<T, U> implements Iterator<U> {
	protected Iterator<? extends T> iterator;

	protected BaseIterator() {
	}

	protected BaseIterator(Iterator<? extends T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	public void skip() {
		Iterators.skip(iterator);
	}

	public void skip(long steps) {
		Iterators.skip(iterator, steps);
	}
}
