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

package org.d2ab.iterator.doubles;

import org.d2ab.iterable.doubles.DoubleIterable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;

public class ChainingDoubleIterator extends UnaryDoubleIterator {
	private final Iterator<DoubleIterable> iterables;

	public ChainingDoubleIterator(DoubleIterable... iterables) {
		this(asList(iterables));
	}

	public ChainingDoubleIterator(Iterable<DoubleIterable> iterables) {
		super(DoubleIterator.EMPTY);
		this.iterables = iterables.iterator();
	}

	@Override
	public boolean hasNext() {
		while (!iterator.hasNext() && iterables.hasNext())
			iterator = iterables.next().iterator();
		return iterator.hasNext();
	}

	@Override
	public double nextDouble() {
		if (!hasNext())
			throw new NoSuchElementException();

		return iterator.nextDouble();
	}
}
