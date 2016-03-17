package org.d2ab.util.primitive;

/**
 * Utility methods for {@code double} primitives.
 */
public class Doubles {
	private Doubles() {
	}

	public static boolean equal(double value, double comparison, double accuracy) {
		return value - accuracy <= comparison && value + accuracy >= comparison;
	}
}
