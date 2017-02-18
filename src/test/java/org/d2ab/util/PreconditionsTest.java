package org.d2ab.util;

import org.junit.Test;

import static org.d2ab.test.Tests.expecting;

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
	public void requireAtLeast() {
		Preconditions.requireAtLeast(17, "seventeen", 17);
		Preconditions.requireAtLeast(18, "seventeen", 17);
		Preconditions.requireAtLeast(Integer.MAX_VALUE, "max", 17);

		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtLeast(16, "sixteen", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtLeast(15, "fifteen", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtLeast(0, "zero", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtLeast(-1, "minus one", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtLeast(Integer.MIN_VALUE, "max", 17));
	}

	@Test
	public void requireAtLeastWithName() {
		Preconditions.requireAtLeast(17, "seventeen", 17, "seventeen");
		Preconditions.requireAtLeast(18, "seventeen", 17, "seventeen");
		Preconditions.requireAtLeast(Integer.MAX_VALUE, "max", 17, "seventeen");

		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtLeast(16, "sixteen", 17, "seventeen"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtLeast(15, "fifteen", 17, "seventeen"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtLeast(0, "zero", 17, "seventeen"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtLeast(-1, "minus one", 17, "seventeen"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtLeast(Integer.MIN_VALUE, "max", 17, "seventeen"));
	}

	@Test
	public void requireNotEqual() {
		Preconditions.requireNotEqual(16, "sixteen", 17);
		Preconditions.requireNotEqual(18, "sixteen", 17);
		Preconditions.requireNotEqual(0, "zero", 17);
		Preconditions.requireNotEqual(Integer.MIN_VALUE, "min", 17);
		Preconditions.requireNotEqual(Integer.MAX_VALUE, "max", 17);

		expecting(IllegalArgumentException.class, () -> Preconditions.requireNotEqual(17, "seventeen", 17));
	}

	@Test
	public void requireNotEqualWithName() {
		Preconditions.requireNotEqual(16, "sixteen", 17, "seventeen");
		Preconditions.requireNotEqual(18, "sixteen", 17, "seventeen");
		Preconditions.requireNotEqual(0, "zero", 17, "seventeen");
		Preconditions.requireNotEqual(Integer.MIN_VALUE, "min", 17, "seventeen");
		Preconditions.requireNotEqual(Integer.MAX_VALUE, "max", 17, "seventeen");

		expecting(IllegalArgumentException.class,
		          () -> Preconditions.requireNotEqual(17, "seventeen", 17, "seventeen"));
	}

	@Test
	public void requireAtMost() {
		Preconditions.requireAtMost(16, "sixteen", 17);
		Preconditions.requireAtMost(17, "seventeen", 17);
		Preconditions.requireAtMost(0, "zero", 17);
		Preconditions.requireAtMost(-1, "minus one", 17);
		Preconditions.requireAtMost(Integer.MIN_VALUE, "min", 17);

		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtMost(18, "eighteen", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtMost(19, "nineteen", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtMost(Integer.MAX_VALUE, "max", 17));
	}

	@Test
	public void requireAtMostWithName() {
		Preconditions.requireAtMost(16, "sixteen", 17, "threshold");
		Preconditions.requireAtMost(17, "seventeen", 17, "threshold");
		Preconditions.requireAtMost(0, "zero", 17, "threshold");
		Preconditions.requireAtMost(-1, "minus one", 17, "threshold");
		Preconditions.requireAtMost(Integer.MIN_VALUE, "min", 17, "threshold");

		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtMost(18, "eighteen", 17, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtMost(19, "nineteen", 17, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAtMost(Integer.MAX_VALUE, "max", 17, "threshold"));
	}

	@Test
	public void requireAboveDouble() {
		Preconditions.requireAbove(17.00001, "just above seventeen", 17.0);
		Preconditions.requireAbove(18.0, "eighteen", 17.0);
		Preconditions.requireAbove(19.0, "nineteen", 17.0);
		Preconditions.requireAbove(Integer.MAX_VALUE, "max", 17.0);

		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(17.0, "seventeen", 17.0));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(16.0, "sixteen", 17.0));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(15.0, "fifteen", 17.0));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(0.0, "zero", 17.0));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(-1.0, "minus one", 17.0));
	}

	@Test
	public void requireAboveDoubleWithName() {
		Preconditions.requireAbove(17.00001, "just above seventeen", 17.0, "threshold");
		Preconditions.requireAbove(18.0, "eighteen", 17.0, "threshold");
		Preconditions.requireAbove(19.0, "nineteen", 17.0, "threshold");
		Preconditions.requireAbove(Integer.MAX_VALUE, "max", 17.0, "threshold");

		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(17.0, "seventeen", 17.0, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(16.0, "sixteen", 17.0, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(15.0, "fifteen", 17.0, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(0.0, "zero", 17.0, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(-1.0, "minus one", 17.0, "threshold"));
	}

	@Test
	public void requireAboveLong() {
		Preconditions.requireAbove(18, "eighteen", 17);
		Preconditions.requireAbove(19, "nineteen", 17);
		Preconditions.requireAbove(Integer.MAX_VALUE, "max", 17);

		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(17, "seventeen", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(16, "sixteen", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(15, "fifteen", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(0, "zero", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(-1, "minus one", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(Integer.MIN_VALUE, "min", 17));
	}

	@Test
	public void requireAboveLongWithName() {
		Preconditions.requireAbove(18, "eighteen", 17, "threshold");
		Preconditions.requireAbove(19, "nineteen", 17, "threshold");
		Preconditions.requireAbove(Integer.MAX_VALUE, "max", 17, "threshold");

		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(17, "seventeen", 17, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(16, "sixteen", 17, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(15, "fifteen", 17, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(0, "zero", 17, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(-1, "minus one", 17, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireAbove(Integer.MIN_VALUE, "min", 17, "threshold"));
	}

	@Test
	public void requireBelow() {
		Preconditions.requireBelow(15, "fifteen", 17);
		Preconditions.requireBelow(16, "sixteen", 17);
		Preconditions.requireBelow(0, "zero", 17);
		Preconditions.requireBelow(-1, "minus one", 17);
		Preconditions.requireBelow(Integer.MIN_VALUE, "min", 17);

		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireBelow(17, "seventeen", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireBelow(18, "eighteen", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireBelow(19, "nineteen", 17));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireBelow(Integer.MAX_VALUE, "max", 17));
	}

	@Test
	public void requireBelowWithName() {
		Preconditions.requireBelow(15, "fifteen", 17, "threshold");
		Preconditions.requireBelow(16, "sixteen", 17, "threshold");
		Preconditions.requireBelow(0, "zero", 17, "threshold");
		Preconditions.requireBelow(-1, "minus one", 17, "threshold");
		Preconditions.requireBelow(Integer.MIN_VALUE, "min", 17, "threshold");

		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireBelow(17, "seventeen", 17, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireBelow(18, "eighteen", 17, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireBelow(19, "nineteen", 17, "threshold"));
		expecting(IllegalArgumentException.class, () ->
				Preconditions.requireBelow(Integer.MAX_VALUE, "max", 17, "threshold"));
	}

	@Test
	public void requireSizeWithinBounds() {
		Preconditions.requireSizeWithinBounds(0, "zero", 17, "size");
		Preconditions.requireSizeWithinBounds(1, "zero", 17, "size");
		Preconditions.requireSizeWithinBounds(16, "sixteen", 17, "size");
		Preconditions.requireSizeWithinBounds(17, "seventeen", 17, "size");

		expecting(IndexOutOfBoundsException.class, () ->
				Preconditions.requireSizeWithinBounds(-1, "minus one", 17, "size"));
		expecting(IndexOutOfBoundsException.class, () ->
				Preconditions.requireSizeWithinBounds(-2, "minus two", 17, "size"));
		expecting(IndexOutOfBoundsException.class, () ->
				Preconditions.requireSizeWithinBounds(18, "eighteen", 17, "size"));
		expecting(IndexOutOfBoundsException.class, () ->
				Preconditions.requireSizeWithinBounds(19, "nineteen", 17, "size"));
		expecting(IndexOutOfBoundsException.class, () ->
				Preconditions.requireSizeWithinBounds(Integer.MIN_VALUE, "min", 17, "size"));
		expecting(IndexOutOfBoundsException.class, () ->
				Preconditions.requireSizeWithinBounds(Integer.MAX_VALUE, "max", 17, "size"));
	}
}