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
import java.util.function.ObjIntConsumer;

public class IndexPeekingIterator<T> extends DelegatingUnaryIterator<T> {
	private final ObjIntConsumer<? super T> action;
	private int index;

	public IndexPeekingIterator(Iterator<T> iterator, ObjIntConsumer<? super T> action) {
		super(iterator);
		this.action = action;
	}

	@Override
	public T next() {
		T next = iterator.next();
		action.accept(next, index++);
		return next;
	}
}
