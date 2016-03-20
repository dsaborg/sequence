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

package org.d2ab.iterable.ints;

import org.d2ab.iterator.ints.ChainingIntIterator;
import org.d2ab.iterator.ints.IntIterator;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static java.util.Arrays.asList;

/**
 * An {@link IntIterable} that can chain together several {@link IntIterable}s into one unbroken sequence.
 */
public class ChainingIntIterable implements IntIterable {
	private final Collection<IntIterable> iterables = new ArrayList<>();

	public ChainingIntIterable(IntIterable... iterables) {
		asList(iterables).forEach(e -> this.iterables.add(Objects.requireNonNull(e)));
	}

	public ChainingIntIterable append(IntIterable iterable) {
		iterables.add(iterable);
		return this;
	}

	@Override
	public IntIterator iterator() {
		return new ChainingIntIterator(iterables);
	}

	@Override
	public int hashCode() {
		return iterables.hashCode();
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o)
			return true;
		if ((o == null) || (getClass() != o.getClass()))
			return false;

		ChainingIntIterable that = (ChainingIntIterable) o;

		return iterables.equals(that.iterables);
	}

	@Override
	public String toString() {
		return "ChainingIntIterable" + iterables;
	}
}
