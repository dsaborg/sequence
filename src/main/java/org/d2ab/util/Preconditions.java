package org.d2ab.util;

/**
 * Utilities for checking preconditions.
 */
public abstract class Preconditions {
	Preconditions() {
	}

	public static void requireAtLeastZero(int value, String name) {
		requireAtLeast(0, value, name);
	}

	public static void requireAtLeastOne(int value, String name) {
		requireAtLeast(1, value, name);
	}

	public static void requireAtLeast(int threshold, int value, String name) {
		if (value < threshold)
			throw new IllegalArgumentException("Expected " + name + " to be >= " + threshold + ": " + value);
	}

	public static void requireBelow(int threshold, String thresholdName, int value, String name) {
		if (value >= threshold)
			throw new IllegalArgumentException("Expected " + name + " to be < " +
			                                   thresholdName + " (" + threshold + "): " + value);
	}

	public static void requireAtMost(int threshold, String thresholdName, int value, String name) {
		if (value > threshold)
			throw new IllegalArgumentException("Expected " + name + " to be <= " +
			                                   thresholdName + " (" + threshold + "): " + value);
	}

	public static void requireNotEqual(int exception, int value, String name) {
		if (value == exception)
			throw new IllegalArgumentException("Expected " + name + " to be != " + exception);
	}

	public static void requireSizeWithinBounds(int maxSize, String maxSizeName, int size, String name) {
		if (size < 0 || size > maxSize)
			throw new IndexOutOfBoundsException("Expected " + name + " to be >= " + 0 + " and <= " +
			                                    maxSizeName + " (" + maxSize + "): " + size);
	}
}
