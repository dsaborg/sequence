package org.d2ab.util;

/**
 * Utilities for checking preconditions.
 */
public abstract class Preconditions {
	Preconditions() {
	}

	public static void requireZeroOrGreater(int value, String name) {
		requireGreaterOrEqual(0, value, name);
	}

	public static void requireOneOrGreater(int value, String name) {
		requireGreaterOrEqual(1, value, name);
	}

	public static void requireGreaterOrEqual(int threshold, int value, String name) {
		if (value < threshold)
			throw new IllegalArgumentException("Expected " + name + " to be >= " + threshold + ": " + value);
	}
}
