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

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class CharIterableTest {
	private final CharIterable abcde = CharIterable.of('a', 'b', 'c', 'd', 'e');

	@Test
	public void read() throws IOException {
		Reader reader = new StringReader("abcde");

		CharIterable iterable = CharIterable.read(reader);
		twice(() -> assertThat(iterable, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void readEmpty() throws IOException {
		Reader reader = new StringReader("");

		CharIterable iterable = CharIterable.read(reader);
		twice(() -> assertThat(iterable, is(emptyIterable())));
	}

	@Test
	public void readWithMark() throws IOException {
		Reader reader = new StringReader("abcde");
		assertThat((char) reader.read(), CoreMatchers.is('a'));

		reader.mark(0);

		CharIterable iterable = CharIterable.read(reader);
		twice(() -> assertThat(iterable, containsChars('b', 'c', 'd', 'e')));
	}

	@Test
	public void readAlreadyBegun() throws IOException {
		Reader reader = new StringReader("abcde");
		assertThat((char) reader.read(), CoreMatchers.is('a'));

		CharIterable iterable = CharIterable.read(reader);
		assertThat(iterable, containsChars('b', 'c', 'd', 'e'));
		assertThat(iterable, containsChars('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void asReaderReadSingleChars() throws Exception {
		Reader reader = abcde.asReader();

		assertThat((char) reader.read(), is('a'));
		assertThat((char) reader.read(), is('b'));
		assertThat((char) reader.read(), is('c'));
		assertThat((char) reader.read(), is('d'));
		assertThat((char) reader.read(), is('e'));
		assertThat(reader.read(), is(-1));
		assertThat(reader.read(), is(-1));
	}

	@Test
	public void asReaderReady() throws Exception {
		Reader reader = abcde.asReader();

		assertThat(reader.ready(), is(true));

		assertThat((char) reader.read(), is('a'));
		assertThat((char) reader.read(), is('b'));
		assertThat((char) reader.read(), is('c'));
		assertThat((char) reader.read(), is('d'));
		assertThat((char) reader.read(), is('e'));
		assertThat(reader.read(), is(-1));

		assertThat(reader.ready(), is(true));
	}

	@Test
	public void asReaderReset() throws Exception {
		Reader reader = abcde.asReader();

		assertThat((char) reader.read(), is('a'));
		assertThat((char) reader.read(), is('b'));

		reader.reset();
		assertThat((char) reader.read(), is('a'));
		assertThat((char) reader.read(), is('b'));
		assertThat((char) reader.read(), is('c'));
		assertThat((char) reader.read(), is('d'));
		assertThat((char) reader.read(), is('e'));
		assertThat(reader.read(), is(-1));
	}

	@Test
	public void asReaderMarkAndReset() throws Exception {
		Reader reader = abcde.asReader();

		assertThat((char) reader.read(), is('a'));
		assertThat((char) reader.read(), is('b'));

		reader.mark(17);
		assertThat((char) reader.read(), is('c'));
		assertThat((char) reader.read(), is('d'));

		reader.reset();
		assertThat((char) reader.read(), is('c'));
		assertThat((char) reader.read(), is('d'));

		reader.reset();
		assertThat((char) reader.read(), is('c'));
		assertThat((char) reader.read(), is('d'));
	}

	@Test
	public void asReaderReadMultipleChars() throws Exception {
		Reader reader = abcde.asReader();
		char[] cbuf = new char[10];

		assertThat(reader.read(cbuf, 2, 8), is(5));
		assertThat(cbuf[0], is('\0'));
		assertThat(cbuf[1], is('\0'));
		assertThat(cbuf[2], is('a'));
		assertThat(cbuf[3], is('b'));
		assertThat(cbuf[4], is('c'));
		assertThat(cbuf[5], is('d'));
		assertThat(cbuf[6], is('e'));
		assertThat(cbuf[7], is('\0'));
		assertThat(cbuf[8], is('\0'));
		assertThat(cbuf[9], is('\0'));

		assertThat(reader.read(cbuf, 0, 0), is(0));
		assertThat(reader.read(cbuf, 0, 2), is(-1));
	}

	@Test
	public void asReaderSkip() throws Exception {
		Reader reader = abcde.asReader();

		assertThat((char) reader.read(), is('a'));
		assertThat((char) reader.read(), is('b'));

		assertThat(reader.skip(2), is(2L));
		assertThat((char) reader.read(), is('e'));

		assertThat(reader.skip(2), is(0L));
	}

	@Test
	public void asReaderClose() throws Exception {
		Reader reader = abcde.asReader();

		assertThat((char) reader.read(), is('a'));
		assertThat((char) reader.read(), is('b'));
		reader.close();

		try {
			reader.read();
			fail("Expected IOException");
		} catch (IOException expected) {
		}
	}
}