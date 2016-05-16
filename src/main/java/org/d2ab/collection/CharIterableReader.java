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

import org.d2ab.iterator.chars.CharIterator;

import java.io.IOException;
import java.io.Reader;

/**
 * A {@link Reader} that reads {@code char} values from a {@link CharIterable}. All methods are supported.
 */
public class CharIterableReader extends Reader {
	private final CharIterable iterable;

	private CharIterator iterator;
	private long position;
	private long mark;

	public CharIterableReader(CharIterable iterable) {
		this.iterable = iterable;
		this.iterator = iterable.iterator();
	}

	@Override
	public int read() throws IOException {
		if (iterator == null)
			throw new IOException("closed");

		if (!iterator.hasNext())
			return -1;

		position++;

		return iterator.nextChar();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		if (iterator == null)
			throw new IOException("closed");

		if (len == 0)
			return 0;

		if (!iterator.hasNext())
			return -1;

		int index = 0;
		while (index < len && iterator.hasNext())
			cbuf[off + index++] = iterator.nextChar();

		position += index;

		return index;
	}

	@Override
	public long skip(long n) throws IOException {
		if (iterator == null)
			throw new IOException("closed");

		long skipped = 0;
		while (n > Integer.MAX_VALUE) {
			skipped += iterator.skip(Integer.MAX_VALUE);
			n -= Integer.MAX_VALUE;
		}
		skipped += iterator.skip((int) n);

		position += skipped;

		return skipped;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public void mark(int readAheadLimit) throws IOException {
		mark = position;
	}

	@Override
	public boolean ready() throws IOException {
		if (iterator == null)
			throw new IOException("closed");

		return true;
	}

	@Override
	public void reset() throws IOException {
		if (iterator == null)
			throw new IOException("closed");

		iterator = iterable.iterator();
		position = 0;

		long skip = mark;
		while (skip > Integer.MAX_VALUE) {
			position += iterator.skip(Integer.MAX_VALUE);
			skip -= Integer.MAX_VALUE;
		}
		position += iterator.skip((int) skip);
	}

	@Override
	public void close() throws IOException {
		iterator = null;
	}
}
