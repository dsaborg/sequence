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

import org.d2ab.util.Pair;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class RightFlatteningPairIterator<L, R, RR> extends DelegatingReferenceIterator<Pair<L, R>, Pair<L, RR>> {
	private final Function<? super Pair<L, R>, ? extends Iterable<RR>> mapper;

	private Iterator<RR> rightIterator = Iterators.empty();
	private Pair<L, R> pair;

	public RightFlatteningPairIterator(Iterator<Pair<L, R>> iterator,
	                                   Function<? super Pair<L, R>, ? extends Iterable<RR>> mapper) {
		super(iterator);
		this.mapper = mapper;
	}

	@Override
	public boolean hasNext() {
		while (!rightIterator.hasNext() && iterator.hasNext()) {
			pair = iterator.next();
			rightIterator = mapper.apply(pair).iterator();
		}
		return rightIterator.hasNext();
	}

	@Override
	public Pair<L, RR> next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return Pair.of(pair.getLeft(), rightIterator.next());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
