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
	public void requireAtLeastZero() {
		Preconditions.requireAtLeastZero(0, "zero");
		Preconditions.requireAtLeastZero(1, "one");
		Preconditions.requireAtLeastZero(2, "two");
		Preconditions.requireAtLeastZero(Integer.MAX_VALUE, "max");

		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastZero(-1, "minus one"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastZero(-2, "minus two"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastZero(Integer.MIN_VALUE, "min"));
	}

	@Test
	public void requireAtLeastOne() {
		Preconditions.requireAtLeastOne(1, "one");
		Preconditions.requireAtLeastOne(2, "two");
		Preconditions.requireAtLeastOne(3, "three");
		Preconditions.requireAtLeastOne(Integer.MAX_VALUE, "max");

		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastOne(0, "zero"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastOne(-1, "minus one"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastOne(-2, "minus two"));
		expecting(IllegalArgumentException.class, () -> Preconditions.requireAtLeastOne(Integer.MIN_VALUE, "min"));
	}

	@Test
	public void requireNotEqual() {
		Preconditions.requireNotEqual(17, 16, "sixteen");
		Preconditions.requireNotEqual(17, 18, "sixteen");
		Preconditions.requireNotEqual(17, 0, "zero");
		Preconditions.requireNotEqual(17, Integer.MIN_VALUE, "min");
		Preconditions.requireNotEqual(17, Integer.MAX_VALUE, "max");

		expecting(IllegalArgumentException.class, () -> Preconditions.requireNotEqual(17, 17, "seventeen"));
	}

	@Test
	public void requireAtMost() {
		Preconditions.requireAtMost(17, "threshold", 16, "sixteen");
		Preconditions.requireAtMost(17, "threshold", 17, "seventeen");
		Preconditions.requireAtMost(17, "threshold", 0, "zero");
		Preconditions.requireAtMost(17, "threshold", -1, "minus one");
		Preconditions.requireAtMost(17, "threshold", Integer.MIN_VALUE, "min");

		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtMost(17, "threshold", 18, "eighteen"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtMost(17, "threshold", 19, "nineteen"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtMost(17, "threshold", Integer.MAX_VALUE, "max"));
	}

	@Test
	public void requireBelow() {
		Preconditions.requireBelow(17, "threshold", 15, "sixteen");
		Preconditions.requireBelow(17, "threshold", 16, "sixteen");
		Preconditions.requireBelow(17, "threshold", 0, "zero");
		Preconditions.requireBelow(17, "threshold", -1, "minus one");
		Preconditions.requireBelow(17, "threshold", Integer.MIN_VALUE, "min");

		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireBelow(17, "threshold", 17, "seventeen"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireBelow(17, "threshold", 18, "eighteen"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireBelow(17, "threshold", 19, "nineteen"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireBelow(17, "threshold", Integer.MAX_VALUE, "max"));
	}

	@Test
	public void requireSizeWithinBounds() {
		Preconditions.requireSizeWithinBounds(17, "size", 0, "zero");
		Preconditions.requireSizeWithinBounds(17, "size", 1, "zero");
		Preconditions.requireSizeWithinBounds(17, "size", 16, "sixteen");
		Preconditions.requireSizeWithinBounds(17, "size", 17, "seventeen");

		expecting(IndexOutOfBoundsException.class, () ->
				Preconditions.requireSizeWithinBounds(17, "size", -1, "minus one"));
		expecting(IndexOutOfBoundsException.class, () ->
				Preconditions.requireSizeWithinBounds(17, "size", -2, "minus two"));
		expecting(IndexOutOfBoundsException.class, () ->
				Preconditions.requireSizeWithinBounds(17, "size", 18, "eighteen"));
		expecting(IndexOutOfBoundsException.class, () ->
				Preconditions.requireSizeWithinBounds(17, "size", 19, "nineteen"));
		expecting(IndexOutOfBoundsException.class, () ->
				Preconditions.requireSizeWithinBounds(17, "size", Integer.MIN_VALUE, "min"));
		expecting(IndexOutOfBoundsException.class, () ->
				Preconditions.requireSizeWithinBounds(17, "size", Integer.MAX_VALUE, "max"));
	}
}