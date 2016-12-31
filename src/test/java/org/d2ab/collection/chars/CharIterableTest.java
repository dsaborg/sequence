package org.d2ab.collection.chars;

import org.d2ab.collection.ints.IntList;
import org.d2ab.iterator.IterationException;
import org.d2ab.iterator.chars.CharIterator;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.NoSuchElementException;

import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class CharIterableTest {
	CharIterable empty = CharIterable.of();
	CharIterable iterable = CharIterable.from(CharList.create('a', 'b', 'c', 'd', 'e'));

	@Test
	public void isEmpty() {
		assertThat(empty.isEmpty(), is(true));
		assertThat(iterable.isEmpty(), is(false));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty, is(emptyIterable()));

		iterable.clear();
		assertThat(iterable, is(emptyIterable()));
	}

	@Test
	public void intStream() {
		assertThat(empty.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           CoreMatchers.is(emptyIterable()));

		assertThat(iterable.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void parallelIntStream() {
		assertThat(empty.parallelIntStream()
		                .collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           CoreMatchers.is(emptyIterable()));

		assertThat(iterable.parallelIntStream()
		                   .collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void read() {
		Reader reader = new StringReader("abcde");

		CharIterable iterable = CharIterable.read(reader);
		twice(() -> assertThat(iterable, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void readWithIOException() throws IOException {
		Reader reader = spy(new StringReader("abcde"));
		doThrow(IOException.class).when(reader).read();

		CharIterable iterable = CharIterable.read(reader);
		twice(() -> expecting(IterationException.class, () -> iterable.iterator().next()));
	}

	@Test
	public void readEmpty() {
		Reader reader = new StringReader("");

		CharIterable iterable = CharIterable.read(reader);
		twice(() -> assertThat(iterable, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> iterable.iterator().next());
	}

	@Test
	public void readWithMark() throws IOException {
		Reader reader = new StringReader("abcde");
		assertThat((char) reader.read(), is('a'));

		reader.mark(0);

		CharIterable iterable = CharIterable.read(reader);
		twice(() -> assertThat(iterable, containsChars('b', 'c', 'd', 'e')));
	}

	@Test
	public void readWithMarkFailingReset() throws IOException {
		Reader reader = new StringReader("abcde") {
			@Override
			public void reset() throws IOException {
				throw new IOException("test");
			}
		};

		CharIterable iterable = CharIterable.read(reader);
		assertThat(iterable, containsChars('a', 'b', 'c', 'd', 'e'));
		assertThat(iterable, is(emptyIterable()));
	}

	@Test
	public void readAlreadyBegun() throws IOException {
		Reader reader = new StringReader("abcde");
		assertThat((char) reader.read(), is('a'));

		CharIterable iterable = CharIterable.read(reader);
		assertThat(iterable, containsChars('b', 'c', 'd', 'e'));
		assertThat(iterable, containsChars('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void asReaderReadSingleChars() throws Exception {
		Reader reader = iterable.asReader();

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
		Reader reader = iterable.asReader();

		assertThat(reader.ready(), is(true));

		assertThat((char) reader.read(), is('a'));
		assertThat((char) reader.read(), is('b'));
		assertThat((char) reader.read(), is('c'));
		assertThat((char) reader.read(), is('d'));
		assertThat((char) reader.read(), is('e'));
		assertThat(reader.read(), is(-1));

		assertThat(reader.ready(), is(true));

		reader.close();
		expecting(IOException.class, reader::ready);
	}

	@Test
	public void asReaderMarkSupported() throws Exception {
		Reader reader = iterable.asReader();

		assertThat(reader.markSupported(), is(true));

		assertThat((char) reader.read(), is('a'));
		assertThat((char) reader.read(), is('b'));
		assertThat((char) reader.read(), is('c'));
		assertThat((char) reader.read(), is('d'));
		assertThat((char) reader.read(), is('e'));
		assertThat(reader.read(), is(-1));

		assertThat(reader.markSupported(), is(true));

		reader.close();
		assertThat(reader.markSupported(), is(true));
	}

	@Test
	public void asReaderReset() throws Exception {
		Reader reader = iterable.asReader();

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
		Reader reader = iterable.asReader();

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

		reader.close();
		expecting(IOException.class, reader::reset);
	}

	@Test
	public void asReaderMarkAndResetSingleUseIterator() throws Exception {
		Reader reader = CharIterable.once(CharIterator.of('a', 'b', 'c', 'd', 'e')).asReader();

		assertThat((char) reader.read(), is('a'));
		assertThat((char) reader.read(), is('b'));

		reader.mark(17);
		assertThat((char) reader.read(), is('c'));
		assertThat((char) reader.read(), is('d'));
		assertThat((char) reader.read(), is('e'));
		assertThat(reader.read(), is(-1));

		expecting(IllegalStateException.class, reader::reset);
	}

	@Test
	public void asReaderReadMultipleChars() throws Exception {
		Reader reader = iterable.asReader();
		char[] cbuf = new char[10];

		assertThat(reader.read(cbuf, 0, 0), is(0));
		assertArrayEquals(new char[]{'\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0'}, cbuf);

		assertThat(reader.read(cbuf, 2, 8), is(5));
		assertArrayEquals(new char[]{'\0', '\0', 'a', 'b', 'c', 'd', 'e', '\0', '\0', '\0'}, cbuf);

		assertThat(reader.read(cbuf, 0, 2), is(-1));

		reader.close();
		expecting(IOException.class, () -> reader.read(cbuf, 0, 2));
	}

	@Test
	public void asReaderSkip() throws Exception {
		Reader reader = iterable.asReader();

		assertThat((char) reader.read(), is('a'));
		assertThat((char) reader.read(), is('b'));

		assertThat(reader.skip(2), is(2L));
		assertThat((char) reader.read(), is('e'));

		assertThat(reader.skip(2), is(0L));

		reader.close();
		expecting(IOException.class, () -> reader.skip(2));
	}

	@Test
	public void asReaderVeryLargeSkip() throws Exception {
		Reader reader = iterable.asReader();
		assertThat(reader.skip(Integer.MAX_VALUE * 2L), is(5L));
	}

	@Test
	public void asReaderClose() throws Exception {
		Reader reader = iterable.asReader();

		assertThat((char) reader.read(), is('a'));
		assertThat((char) reader.read(), is('b'));
		reader.close();

		expecting(IOException.class, reader::read);
	}
}
