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

import org.d2ab.iterator.ints.IntIterator;

import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link InputStream} that reads {@code byte} values from an {@link IntIterable}. All methods are supported.
 * Values outside of the byte range {@code 0} - {@code 255} inclusive will result in an {@link IOException} being
 * thrown at runtime.
 */
public class IntIterableInputStream extends InputStream {
	private final IntIterable iterable;

	private IntIterator iterator;
	private long position;
	private long mark;

	public IntIterableInputStream(IntIterable iterable) {
		this.iterable = iterable;
		this.iterator = iterable.iterator();
	}

	@Override
	public int read() throws IOException {
		if (iterator == null)
			throw new IOException("closed");

		if (!iterator.hasNext())
			return -1;

		return nextByte();
	}

	private byte nextByte() throws IOException {
		int nextInt = iterator.nextInt();
		position++;
		if (nextInt < 0 || nextInt > 255)
			throw new IOException("Invalid byte value: " + nextInt);
		return (byte) nextInt;
	}

	@Override
	public int read(byte[] buf, int off, int len) throws IOException {
		if (iterator == null)
			throw new IOException("closed");

		if (len == 0)
			return 0;

		if (!iterator.hasNext())
			return -1;

		int index = 0;
		while (index < len && iterator.hasNext())
			buf[off + index++] = nextByte();

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
	public void mark(int readAheadLimit) {
		mark = position;
	}

	@Override
	public int available() throws IOException {
		return 0;
	}

	@Override
	public void reset() throws IOException {
		if (iterator == null)
			throw new IOException("closed");

		iterator = iterable.iterator();

		position = 0;
		skip(mark);
	}

	@Override
	public void close() throws IOException {
		iterator = null;
	}
}
