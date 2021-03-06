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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

public class ExclusiveStartingIterator<T> extends DelegatingUnaryIterator<T> {
	private final Predicate<? super T> predicate;

	private boolean started;

	public ExclusiveStartingIterator(Iterator<T> iterator, T element) {
		this(iterator, o -> Objects.equals(o, element));
	}

	public ExclusiveStartingIterator(Iterator<T> iterator, Predicate<? super T> predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (!started) {
			while (iterator.hasNext())
				if (predicate.test(iterator.next()))
					break;
			started = true;
		}
		return super.hasNext();
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return iterator.next();
	}
}
