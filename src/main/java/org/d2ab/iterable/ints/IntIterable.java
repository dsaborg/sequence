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

import org.d2ab.iterator.IterationException;
import org.d2ab.iterator.ints.ArrayIntIterator;
import org.d2ab.iterator.ints.InputStreamIntIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.sequence.IntSequence;

import java.io.IOException;
import java.io.InputStream;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static java.util.Arrays.asList;

@FunctionalInterface
public interface IntIterable extends Iterable<Integer> {
	/**
	 * Create an {@code IntSequence} from an {@link InputStream} which iterates over the bytes provided in the
	 * input stream as ints. The {@link InputStream} must support {@link InputStream#reset} or the {@code IntSequence}
	 * will only be available to iterate over once. The {@link InputStream} will be reset in between iterations,
	 * if possible. If an {@link IOException} occurs during iteration, an {@link IterationException} will be thrown.
	 * The {@link InputStream} will not be closed by the {@code IntSequence} when iteration finishes, it must be closed
	 * externally when iteration is finished.
	 *
	 * @since 1.1
	 */
	static IntSequence read(InputStream inputStream) {
		return new IntSequence() {
			boolean started;

			@Override
			public IntIterator iterator() {
				if (started)
					try {
						inputStream.reset();
					} catch (IOException e) {
						// do nothing, let input stream exhaust itself
					}
				else
					started = true;

				return new InputStreamIntIterator(inputStream);
			}
		};
	}

	@Override
	IntIterator iterator();

	/**
	 * Performs the given action for each {@code int} in this iterable.
	 */
	@Override
	default void forEach(Consumer<? super Integer> consumer) {
		forEachInt((consumer instanceof IntConsumer) ? (IntConsumer) consumer : consumer::accept);
	}

	/**
	 * Performs the given action for each {@code int} in this iterable.
	 */
	default void forEachInt(IntConsumer consumer) {
		IntIterator iterator = iterator();
		while (iterator.hasNext())
			consumer.accept(iterator.nextInt());
	}

	static IntIterable of(int... integers) {
		return () -> new ArrayIntIterator(integers);
	}

	static IntIterable from(Integer... integers) {
		return from(asList(integers));
	}

	static IntIterable from(Iterable<Integer> iterable) {
		if (iterable instanceof IntIterable)
			return (IntIterable) iterable;

		return () -> IntIterator.from(iterable.iterator());
	}

	static IntIterable once(IntIterator iterator) {
		return () -> iterator;
	}

	static IntIterable once(PrimitiveIterator.OfInt iterator) {
		return once(IntIterator.from(iterator));
	}

	/**
	 * @return this {@code IntIterable} as an {@link InputStream}. Mark and reset is supported, by re-traversing
	 * the iterator to the mark position. Ints outside of the allowed range {@code 0} to {@code 255} will result in
	 * an {@link IOException} being thrown during traversal.
	 *
	 * @since 1.2
	 */
	default InputStream asInputStream() {
		return new InputStream() {
			private long position;
			private long mark;

			private IntIterator iterator = iterator();

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

				long skipped = iterator.skip(n);

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

				iterator = iterator();

				position = iterator.skip(mark);
			}

			@Override
			public void close() throws IOException {
				iterator = null;
			}
		};
	}
}
