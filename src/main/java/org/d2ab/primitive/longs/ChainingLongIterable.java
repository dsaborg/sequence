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

package org.d2ab.primitive.longs;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static java.util.Arrays.asList;

/**
 *
 */
public class ChainingLongIterable implements LongIterable {
	private final Collection<LongIterable> iterables = new ArrayList<>();

	public ChainingLongIterable(@Nonnull LongIterable... iterables) {
		asList(iterables).forEach(e -> this.iterables.add(Objects.requireNonNull(e)));
	}

	@Nonnull
	public ChainingLongIterable append(@Nonnull LongIterable iterable) {
		iterables.add(iterable);
		return this;
	}

	@Override
	public LongIterator iterator() {
		return new ChainingLongIterator(iterables);
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

		ChainingLongIterable that = (ChainingLongIterable) o;

		return iterables.equals(that.iterables);
	}

	@Override
	public String toString() {
		return "ChainingLongIterable" + iterables;
	}
}
