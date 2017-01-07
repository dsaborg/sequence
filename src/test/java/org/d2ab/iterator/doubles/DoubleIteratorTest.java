package org.d2ab.iterator.doubles;

import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleConsumer;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DoubleIteratorTest {
	private final DoubleIterator empty = DoubleIterator.empty();
	private final DoubleIterator iterator = DoubleIterator.of(1, 2, 3, 4, 5);

	@Test
	public void iteration() {
		assertThat(empty.hasNext(), is(false));
		expecting(NoSuchElementException.class, empty::nextDouble);

		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextDouble(), is(1.0));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextDouble(), is(2.0));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextDouble(), is(3.0));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextDouble(), is(4.0));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextDouble(), is(5.0));
		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::nextDouble);
	}

	@Test
	public void skip() {
		assertThat(empty.skip(), is(false));
		assertThat(empty.hasNext(), is(false));
		expecting(NoSuchElementException.class, empty::nextDouble);

		assertThat(iterator.skip(), is(true));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextDouble(), is(2.0));
	}

	@Test
	public void forEachRemainingDoubleConsumer() {
		empty.forEachRemaining((DoubleConsumer) x -> fail("should not get called"));

		AtomicInteger i = new AtomicInteger();
		iterator.forEachRemaining((DoubleConsumer) x -> assertThat(x, is((double) (i.getAndIncrement() + 1))));
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