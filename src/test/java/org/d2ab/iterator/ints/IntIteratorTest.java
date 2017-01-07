package org.d2ab.iterator.ints;

import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class IntIteratorTest {
	private final IntIterator empty = IntIterator.empty();
	private final IntIterator iterator = IntIterator.of(1, 2, 3, 4, 5);

	@Test
	public void iteration() {
		assertThat(empty.hasNext(), is(false));
		expecting(NoSuchElementException.class, empty::nextInt);

		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextInt(), is(1));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextInt(), is(2));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextInt(), is(3));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextInt(), is(4));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextInt(), is(5));
		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::nextInt);
	}

	@Test
	public void skip() {
		assertThat(empty.skip(), is(false));
		assertThat(empty.hasNext(), is(false));
		expecting(NoSuchElementException.class, empty::nextInt);

		assertThat(iterator.skip(), is(true));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextInt(), is(2));
	}

	@Test
	public void forEachRemainingIntConsumer() {
		empty.forEachRemaining((IntConsumer) x -> fail("should not get called"));

		AtomicInteger i = new AtomicInteger();
		iterator.forEachRemaining((IntConsumer) x -> assertThat(x, is(i.getAndIncrement() + 1)));
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