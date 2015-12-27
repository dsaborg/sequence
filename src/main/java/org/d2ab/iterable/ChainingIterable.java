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

package org.d2ab.iterable;

import org.d2ab.iterator.ChainingIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class ChainingIterable<T> implements Iterable<T> {
	private final List<Iterable<T>> iterables = new ArrayList<>();

	public ChainingIterable() {
	}

	@SafeVarargs
	public ChainingIterable(Iterable<T>... iterables) {
		this.iterables.addAll(asList(iterables));
	}

	public static <U, T> ChainingIterable<U> from(Iterable<T> iterablesIteratorsOrArrays) {
		return new ChainingIterable<U>().appendAll(iterablesIteratorsOrArrays);
	}

	public <U> ChainingIterable<T> appendAll(Iterable<U> iterablesIteratorsOrArrays) {
		for (U each : iterablesIteratorsOrArrays) {
			if (each == null || each instanceof Iterable)
				append((Iterable<T>) each);
			else if (each instanceof Iterator)
				append((Iterator<T>) each);
			else if (each instanceof Stream)
				append((Stream<T>) each);
			else if (each instanceof Object[])
				append((T[]) each);
			else
				throw new ClassCastException("Required an Iterable, Iterator or Array but got: " + each.getClass());
		}
		return this;
	}

	public ChainingIterable<T> append(Iterable<T> iterable) {
		iterables.add(iterable);
		return this;
	}

	public ChainingIterable<T> append(Iterator<T> iterator) {
		iterables.add(() -> iterator);
		return this;
	}

	public ChainingIterable<T> append(T... objects) {
		iterables.add(asList(objects));
		return this;
	}

	public ChainingIterable<T> append(Stream<T> stream) {
		iterables.add(stream::iterator);
		return this;
	}

	@Override
	public Iterator<T> iterator() {
		return new ChainingIterator<>(iterables);
	}
}
