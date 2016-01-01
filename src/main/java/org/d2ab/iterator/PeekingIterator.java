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
import java.util.function.Consumer;

public class PeekingIterator<T> implements Iterator<T> {
	private final Iterator<T> iterator;
	private final Consumer<T> action;

	public PeekingIterator(Iterator<T> iterator, Consumer<T> action) {
		this.iterator = iterator;
		this.action = action;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public T next() {
		T next = iterator.next();
		action.accept(next);
		return next;
	}
}
