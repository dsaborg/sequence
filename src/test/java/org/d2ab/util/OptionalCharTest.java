package org.d2ab.util;

import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class OptionalCharTest {
	private final OptionalChar empty = OptionalChar.empty();
	private final OptionalChar set = OptionalChar.of('q');

	@Test
	public void getAsChar() {
		expecting(NoSuchElementException.class, empty::getAsChar);
		assertThat(set.getAsChar(), is('q'));
	}

	@Test
	public void isPresent() {
		assertThat(empty.isPresent(), is(false));
		assertThat(set.isPresent(), is(true));
	}

	@Test
	public void ifPresent() {
		empty.ifPresent(x -> { throw new IllegalStateException(); });

		AtomicInteger counter = new AtomicInteger();
		set.ifPresent(x -> {
			assertThat(x, is('q'));
			counter.getAndIncrement();
		});
		assertThat(counter.get(), is(1));
	}

	@Test
	public void orElse() {
		assertThat(empty.orElse('v'), is('v'));
		assertThat(set.orElse('v'), is('q'));
	}

	@Test
	public void orElseGet() {
		assertThat(empty.orElseGet(() -> 'v'), is('v'));
		assertThat(set.orElseGet(() -> 'v'), is('q'));
	}

	@Test
	public void orElseThrow() {
		expecting(IllegalStateException.class, () -> empty.orElseThrow(IllegalStateException::new));
		assertThat(set.orElseThrow(IllegalStateException::new), is('q'));
	}

	@Test
	public void testEquals() {
		assertThat(empty, is(equalTo(empty)));
		assertThat(empty, is(equalTo(OptionalChar.empty())));
		assertThat(empty, is(not(equalTo(set))));
		assertThat(empty, is(not(equalTo(new Object()))));
		assertThat(empty, is(not(equalTo(null))));

		assertThat(set, is(equalTo(set)));
		assertThat(set, is(equalTo(OptionalChar.of('q'))));
		assertThat(set, is(not(equalTo(OptionalChar.of('p')))));
		assertThat(set, is(not(equalTo(empty))));
		assertThat(set, is(not(equalTo(new Object()))));
		assertThat(set, is(not(equalTo(null))));
	}

	@Test
	public void testHashCode() {
		assertThat(empty.hashCode(), is(0));
		assertThat(set.hashCode(), is(144));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("OptionalChar.empty"));
		assertThat(set.toString(), is("OptionalChar[q]"));
	}
}