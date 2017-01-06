package org.d2ab.iterator.longs;

import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongConsumer;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class LongIteratorTest {
	private final LongIterator empty = LongIterator.empty();
	private final LongIterator iterator = LongIterator.of(1, 2, 3, 4, 5);

	@Test
	public void iteration() {
		assertThat(empty.hasNext(), is(false));
		expecting(NoSuchElementException.class, empty::nextLong);

		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextLong(), is(1L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextLong(), is(2L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextLong(), is(3L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextLong(), is(4L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextLong(), is(5L));
		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::nextLong);
	}

	@Test
	public void skip() {
		assertThat(empty.skip(), is(false));
		assertThat(empty.hasNext(), is(false));
		expecting(NoSuchElementException.class, empty::nextLong);

		assertThat(iterator.skip(), is(true));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextLong(), is(2L));
	}

	@Test
	public void forEachRemainingLongConsumer() {
		empty.forEachRemaining((LongConsumer) x -> fail("should not get called"));

		AtomicInteger i = new AtomicInteger();
		iterator.forEachRemaining((LongConsumer) x -> assertThat(x, is((long) (i.getAndIncrement() + 1))));
		assertThat(i.get(), is(5));
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(iterator.size(), is(5));
	}

	@Test
	public void bigCount() {
		assertThat(iterator.size(it -> {
			assertThat(it, is(sameInstance(iterator)));
			return (long) Integer.MAX_VALUE;
		}), is(Integer.MAX_VALUE));

		expecting(IllegalStateException.class, () ->
				iterator.size(it -> {
					assertThat(it, is(sameInstance(iterator)));
					return Integer.MAX_VALUE + 1L;
				}));
	}
}