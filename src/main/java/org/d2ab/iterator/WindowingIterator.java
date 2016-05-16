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

import java.util.*;

public abstract class WindowingIterator<T, S> extends DelegatingReferenceIterator<T, S> {
	private final int window;
	private final int step;

	private Deque<T> partition = new LinkedList<>();
	private boolean started;

	public WindowingIterator(Iterator<T> iterator, int window, int step) {
		super(iterator);
		this.window = window;
		this.step = step;
	}

	@Override
	public boolean hasNext() {
		while (partition.size() < window && iterator.hasNext())
			partition.add(iterator.next());

		return partition.size() == window ||
		       partition.size() > 0 && (!started || partition.size() > window - step && !iterator.hasNext());
	}

	@Override
	public S next() {
		if (!hasNext())
			throw new NoSuchElementException();

		List<T> next = new ArrayList<>(partition);

		if (step < partition.size()) {
			for (int i = 0; i < step && !partition.isEmpty(); i++)
				partition.removeFirst();
		} else {
			for (int i = partition.size(); i < step && iterator.hasNext(); i++)
				iterator.next();
			partition.clear();
		}

		started = true;
		return toSequence(next);
	}

	protected abstract S toSequence(List<T> list);

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
