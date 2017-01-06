package org.d2ab.iterator.chars;

import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class CharIteratorBoxingTest extends BaseBoxingTest {
	private final Iterator<Character> empty = CharIterator.empty();
	private final Iterator<Character> iterator = CharIterator.of('a', 'b', 'c', 'd', 'e');

	@Test
	public void iteration() {
		assertThat(empty.hasNext(), is(false));
		expecting(NoSuchElementException.class, empty::next);

		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is('a'));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is('b'));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is('c'));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is('d'));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is('e'));
		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::next);
	}

	@Test
	public void forEachRemainingConsumer() {
		empty.forEachRemaining(x -> fail("should not get called"));

		AtomicInteger i = new AtomicInteger();
		iterator.forEachRemaining(x -> assertThat(x, is((char) (i.getAndIncrement() + 'a'))));
		assertThat(i.get(), is(5));
	}
}