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

package org.d2ab.sequence;

import org.d2ab.iterable.Iterables;
import org.d2ab.iterator.ChainingIterator;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class ChainingSequence<T> implements Sequence<T> {
	private final Collection<Iterable<? extends T>> iterables = new ArrayList<>();

	public ChainingSequence() {
	}

	public ChainingSequence(@Nonnull Iterable<T> iterable) {
		iterables.add(Objects.requireNonNull(iterable));
	}

	@SafeVarargs
	public ChainingSequence(@Nonnull Iterable<T>... iterables) {
		asList(iterables).forEach(e -> this.iterables.add(Objects.requireNonNull(e)));
	}

	public static <U> ChainingSequence<U> flatten(@Nonnull Iterable<?> containers) {
		return new ChainingSequence<U>().flatAppend(containers);
	}

	@Nonnull
	public static <T, U> ChainingSequence<U> flatMap(@Nonnull Iterable<? extends T> iterable,
	                                                 @Nonnull Function<? super T, ? extends Iterable<U>> mapper) {
		ChainingSequence<U> result = new ChainingSequence<>();
		iterable.forEach(each -> result.append(mapper.apply(each)));
		return result;
	}

	public ChainingSequence<T> flatAppend(@Nonnull Iterable<?> containers) {
		for (Object each : containers)
			append(Iterables.from(each));
		return this;
	}

	@Nonnull
	public ChainingSequence<T> append(@Nonnull Iterable<T> iterable) {
		iterables.add(iterable);
		return this;
	}

	public ChainingSequence<T> append(Iterator<T> iterator) {
		return append(Iterables.from(iterator));
	}

	public ChainingSequence<T> append(T... objects) {
		return append(Iterables.from(objects));
	}

	public ChainingSequence<T> append(Stream<T> stream) {
		return append(Iterables.from(stream));
	}

	@Override
	public Iterator<T> iterator() {
		return new ChainingIterator<>(iterables);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ChainingSequence<?> that = (ChainingSequence<?>) o;

		return iterables.equals(that.iterables);
	}

	@Override
	public int hashCode() {
		return iterables.hashCode();
	}

	@Override
	public String toString() {
		return "ChainingSequence" + iterables;
	}
}
