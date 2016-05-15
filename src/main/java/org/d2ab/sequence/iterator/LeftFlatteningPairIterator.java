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

package org.d2ab.sequence.iterator;

import org.d2ab.collection.iterator.DelegatingReferenceIterator;
import org.d2ab.collection.iterator.Iterators;
import org.d2ab.util.Pair;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class LeftFlatteningPairIterator<L, R, LL> extends DelegatingReferenceIterator<Pair<L, R>, Pair<LL, R>> {
	private final Function<? super Pair<L, R>, ? extends Iterable<LL>> mapper;

	private Iterator<LL> leftIterator = Iterators.empty();
	private Pair<L, R> pair;

	public LeftFlatteningPairIterator(Iterator<Pair<L, R>> iterator,
	                                  Function<? super Pair<L, R>, ? extends Iterable<LL>> mapper) {
		super(iterator);
		this.mapper = mapper;
	}

	@Override
	public boolean hasNext() {
		while (!leftIterator.hasNext() && iterator.hasNext()) {
			pair = iterator.next();
			leftIterator = mapper.apply(pair).iterator();
		}
		return leftIterator.hasNext();
	}

	@Override
	public Pair<LL, R> next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return Pair.of(leftIterator.next(), pair.getRight());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
