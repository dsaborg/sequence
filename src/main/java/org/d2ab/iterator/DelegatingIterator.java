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

package org.d2ab.iterator;

import java.util.Iterator;

/**
 * An {@link Iterator} that delegates to another {@link Iterator} of a specified type.
 */
public abstract class DelegatingIterator<T, I extends Iterator<? extends T>, U> implements Iterator<U> {
	protected I iterator;

	protected DelegatingIterator(I iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public U next() {
		return (U) iterator.next();
	}

	@Override
	public void remove() {
		iterator.remove();
	}
}
