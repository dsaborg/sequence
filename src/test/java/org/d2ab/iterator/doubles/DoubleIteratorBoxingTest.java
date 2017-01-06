package org.d2ab.iterator.doubles;

import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DoubleIteratorBoxingTest extends BaseBoxingTest {
	private final Iterator<Double> empty = DoubleIterator.empty();
	private final Iterator<Double> iterator = DoubleIterator.of(1, 2, 3, 4, 5);

	@Test
	public void iteration() {
		assertThat(empty.hasNext(), is(false));
		expecting(NoSuchElementException.class, empty::next);

		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(1.0));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(2.0));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(3.0));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(4.0));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(5.0));
		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::next);
	}

	@Test
	public void forEachRemainingConsumer() {
		empty.forEachRemaining(x -> fail("should not get called"));

		AtomicInteger i = new AtomicInteger();
		iterator.forEachRemaining(x -> assertThat(x, is((double) (i.getAndIncrement() + 1))));
		assertThat(i.get(), is(5));
	}
}