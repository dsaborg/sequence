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

package org.d2ab.iterator.chars;

import org.d2ab.iterator.MappedIterator;
import org.d2ab.sequence.CharSeq;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class WindowingCharIterator extends MappedIterator<Character, CharIterator, CharSeq> {
	private final int window;
	private final int step;

	private char[] partition;
	private int size;
	private boolean started;

	public WindowingCharIterator(CharIterator iterator, int window, int step) {
		super(iterator);
		this.window = window;
		this.step = step;
		this.partition = new char[window];
	}

	@Override
	public boolean hasNext() {
		while (size < window && iterator.hasNext())
			partition[size++] = iterator.nextChar();

		return size == window || size > 0 && (!started || size > window - step && !iterator.hasNext());
	}

	@Override
	public CharSeq next() {
		if (!hasNext())
			throw new NoSuchElementException();

		CharSeq next = CharSeq.of(Arrays.copyOf(partition, size));

		if (step < partition.length) {
			System.arraycopy(partition, step, partition, 0, partition.length - step);
			size = partition.length - step;
		} else {
			for (int i = partition.length; i < step && iterator.hasNext(); i++)
				iterator.nextChar();
			size = 0;
		}

		started = true;
		return next;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
