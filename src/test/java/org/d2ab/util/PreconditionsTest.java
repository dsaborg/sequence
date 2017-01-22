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
		Preconditions.requireAtLeastZero(0, "zero");
		Preconditions.requireAtLeastZero(1, "one");
		Preconditions.requireAtLeastZero(2, "two");
		Preconditions.requireAtLeastZero(Integer.MAX_VALUE, "max");

		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastZero(-1, "minus one"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastZero(-2, "minus two"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastZero(Integer.MIN_VALUE, "min"));
	}

	@Test
	public void requireOneOrGreater() {
		Preconditions.requireAtLeastOne(1, "one");
		Preconditions.requireAtLeastOne(2, "two");
		Preconditions.requireAtLeastOne(3, "three");
		Preconditions.requireAtLeastOne(Integer.MAX_VALUE, "max");

		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastOne(0, "zero"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastOne(-1, "minus one"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastOne(-2, "minus two"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastOne(Integer.MIN_VALUE, "min"));
	}
}