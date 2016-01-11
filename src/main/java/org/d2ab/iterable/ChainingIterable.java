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
import org.d2ab.utils.MoreArrays;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class ChainingIterable<T> implements Iterable<T> {
	private final Collection<Iterable<T>> iterables = new ArrayList<>();

	public ChainingIterable() {
	}

	public ChainingIterable(@Nonnull Iterable<T> iterable) {
		iterables.add(Objects.requireNonNull(iterable));
	}

	@SafeVarargs
	public ChainingIterable(@Nonnull Iterable<T>... iterables) {
		MoreArrays.forEach(e -> this.iterables.add(Objects.requireNonNull(e)), iterables);
	}

	public static <U> Iterable<U> flatten(@Nonnull Iterable<?> containers) {
		return new ChainingIterable<U>().flatAppend(containers);
	}

	@Nonnull
	public static <T, U> Iterable<U> flatMap(@Nonnull Iterable<? extends T> iterable,
	                                         @Nonnull Function<? super T, ? extends Iterable<U>> mapper) {
		ChainingIterable<U> result = new ChainingIterable<>();
		iterable.forEach(each -> result.append(mapper.apply(each)));
		return result;
	}

	public Iterable<T> flatAppend(@Nonnull Iterable<?> containers) {
		for (Object each : containers)
			append(Iterables.from(each));
		return this;
	}

	@Nonnull
	public Iterable<T> append(@Nonnull Iterable<T> iterable) {
		iterables.add(iterable);
		return this;
	}

	public Iterable<T> append(Iterator<T> iterator) {
		return append(Iterables.from(iterator));
	}

	public Iterable<T> append(T... objects) {
		return append(Iterables.from(objects));
	}

	public Iterable<T> append(Stream<T> stream) {
		return append(Iterables.from(stream));
	}

	@Override
	public Iterator<T> iterator() {
		return new ChainingIterator<>(iterables);
	}

	@Override
	public int hashCode() {
		return iterables.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if ((o == null) || (getClass() != o.getClass()))
			return false;

		ChainingIterable<?> that = (ChainingIterable<?>) o;

		return iterables.equals(that.iterables);
	}

	@Override
	public String toString() {
		return "ChainingIterable" + iterables;
	}
}
