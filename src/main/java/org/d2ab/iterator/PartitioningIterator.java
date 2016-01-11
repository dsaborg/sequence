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
package org.d2ab.iterator;

import java.util.*;

public class PartitioningIterator<T> extends DelegatingReferenceIterator<T, List<T>> {
	private final int window;

	private Deque<T> partition = new LinkedList<>();

	public PartitioningIterator(int window) {
		this.window = window;
	}

	@Override
	public List<T> next() {
		if (!hasNext())
			throw new NoSuchElementException();

		List<T> result = new ArrayList<>(partition);
		partition.removeFirst();
		return result;
	}

	@Override
	public boolean hasNext() {
		while (partition.size() < window && iterator.hasNext())
			partition.add(iterator.next());

		return partition.size() == window;
	}
}
