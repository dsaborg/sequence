package org.d2ab.util;

import org.junit.Test;

import static org.d2ab.test.Tests.expecting;

/**
 * Created by Daniel on 2017-01-22.
 */
public class PreconditionsTest {
	@Test
	public void constructor() {
		new Preconditions() {
			// test coverage
		};
	}

	@Test
	public void requireZeroOrGreater() {
		Preconditions.requireZeroOrGreater(0, "zero");
		Preconditions.requireZeroOrGreater(1, "one");
		Preconditions.requireZeroOrGreater(2, "two");
		Preconditions.requireZeroOrGreater(Integer.MAX_VALUE, "max");

		expecting(IllegalArgumentException.class, () -> Preconditions.requireZeroOrGreater(-1, "minus one"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireZeroOrGreater(-2, "minus two"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireZeroOrGreater(Integer.MIN_VALUE, "min"));
	}

	@Test
	public void requireOneOrGreater() {
		Preconditions.requireOneOrGreater(1, "one");
		Preconditions.requireOneOrGreater(2, "two");
		Preconditions.requireOneOrGreater(3, "three");
		Preconditions.requireOneOrGreater(Integer.MAX_VALUE, "max");

		expecting(IllegalArgumentException.class, () -> Preconditions.requireOneOrGreater(0, "zero"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireOneOrGreater(-1, "minus one"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireOneOrGreater(-2, "minus two"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireOneOrGreater(Integer.MIN_VALUE, "min"));
	}
}