package org.d2ab.util;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class StrictTest {
	@Test
	public void constructor() {
		new Strict() {
			// for test coverage
		};
	}

	@Test
	public void unsetReset() {
		boolean lenient = !Boolean.getBoolean(Strict.STRICT_PROPERTY);

		assertThat(Strict.LENIENT, is(lenient));

		Strict.unset();
		assertThat(Strict.LENIENT, is(true));

		Strict.unset();
		assertThat(Strict.LENIENT, is(true));

		Strict.reset();
		assertThat(Strict.LENIENT, is(true));

		Strict.reset();
		assertThat(Strict.LENIENT, is(lenient));
	}

	@Test
	public void check() {
		boolean lenient = !Boolean.getBoolean(Strict.STRICT_PROPERTY);

		assumeThat(lenient, is(false));

		boolean failed;
		try {
			Strict.check();
			failed = false;
		} catch (AssertionError expected) {
			failed = true;
		}

		assertThat(failed, is(true));

		Strict.unset();
		Strict.check();
		Strict.reset();
	}
}
