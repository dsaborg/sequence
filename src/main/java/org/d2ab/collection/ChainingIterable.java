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

package org.d2ab.collection;

import org.d2ab.iterator.ChainingIterator;
import org.d2ab.iterator.MappingIterator;

import java.util.Iterator;
import java.util.function.Function;

public class ChainingIterable<T> implements Iterable<T> {
	private final Iterable<Iterable<T>> iterables;

	public ChainingIterable(Iterable<Iterable<T>> iterables) {
		this.iterables = iterables;
	}

	public static <T> Iterable<T> empty() {
		return new ChainingIterable<>(Iterables.empty());
	}

	@SafeVarargs
	public static <T> Iterable<T> concat(Iterable<T>... iterables) {
		return new ChainingIterable<>(Iterables.of(iterables));
	}

	public static <T, U> Iterable<U> flatten(Iterable<? extends T> containers,
	                                         Function<? super T, ? extends Iterable<U>> mapper) {
		return new ChainingIterable<>(() -> new MappingIterator<>(containers.iterator(), mapper));
	}

	@Override
	public Iterator<T> iterator() {
		return new ChainingIterator<>(iterables);
	}
}
