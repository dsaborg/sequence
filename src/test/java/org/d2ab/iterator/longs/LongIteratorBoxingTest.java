package org.d2ab.iterator.longs;

import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class LongIteratorBoxingTest extends BaseBoxingTest {
	private final Iterator<Long> empty = LongIterator.empty();
	private final Iterator<Long> iterator = LongIterator.of(1, 2, 3, 4, 5);

	@Test
	public void iteration() {
		assertThat(empty.hasNext(), is(false));
		expecting(NoSuchElementException.class, empty::next);

		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(1L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(2L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(3L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(4L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(5L));
		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::next);
	}

	@Test
	public void forEachRemainingConsumer() {
		empty.forEachRemaining(x -> fail("should not get called"));

		AtomicInteger i = new AtomicInteger();
		iterator.forEachRemaining(x -> assertThat(x, is((long) (i.getAndIncrement() + 1))));
		assertThat(i.get(), is(5));
	}
}