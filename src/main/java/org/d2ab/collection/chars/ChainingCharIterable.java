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

package org.d2ab.collection.chars;

import org.d2ab.iterator.chars.ChainingCharIterator;
import org.d2ab.iterator.chars.CharIterator;

import java.util.Collection;

import static java.util.Arrays.asList;

/**
 * A {@link CharIterable} that can chain together several {@link CharIterable}s into one unbroken sequence.
 */
public class ChainingCharIterable implements CharIterable {
	private final Collection<CharIterable> iterables;

	public ChainingCharIterable(CharIterable... iterables) {
		this.iterables = asList(iterables);
	}

	@Override
	public CharIterator iterator() {
		return new ChainingCharIterator(iterables);
	}
}
