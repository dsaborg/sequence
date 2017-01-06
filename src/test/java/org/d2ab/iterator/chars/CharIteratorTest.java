package org.d2ab.iterator.chars;

import org.d2ab.function.CharConsumer;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class CharIteratorTest {
	private final CharIterator empty = CharIterator.empty();
	private final CharIterator iterator = CharIterator.of('a', 'b', 'c', 'd', 'e');

	@Test
	public void iteration() {
		assertThat(empty.hasNext(), is(false));
		expecting(NoSuchElementException.class, empty::nextChar);

		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextChar(), is('a'));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextChar(), is('b'));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextChar(), is('c'));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextChar(), is('d'));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextChar(), is('e'));
		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::nextChar);
	}

	@Test
	public void skip() {
		assertThat(empty.skip(), is(false));
		assertThat(empty.hasNext(), is(false));
		expecting(NoSuchElementException.class, empty::nextChar);

		assertThat(iterator.skip(), is(true));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.nextChar(), is('b'));
	}

	@Test
	public void forEachRemainingCharConsumer() {
		empty.forEachRemaining((CharConsumer) x -> fail("should not get called"));

		AtomicInteger i = new AtomicInteger();
		iterator.forEachRemaining((CharConsumer) x -> assertThat(x, is((char) (i.getAndIncrement() + 'a'))));
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