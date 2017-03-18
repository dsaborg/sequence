package org.d2ab.util;

import org.d2ab.collection.SizedIterable;

import static org.d2ab.collection.SizedIterable.SizeType.INFINITE;

/**
 * Utilities for checking preconditions.
 */
public abstract class Preconditions {
	Preconditions() {
	}

	public static void requireAtLeastZero(long value, String name) {
		requireAtLeast(value, name, 0);
	}

	public static void requireAtLeastOne(long value, String name) {
		requireAtLeast(value, name, 1);
	}

	public static void requireAtLeast(long value, String name, long threshold) {
		if (value < threshold)
			throw new IllegalArgumentException("Expected " + name + " to be >= " + threshold + ": " + value);
	}

	public static void requireAtLeast(long value, String name, long threshold, String thresholdName) {
		if (value < threshold)
			throw new IllegalArgumentException("Expected " + name + " to be >= " +
			                                   thresholdName + " (" + threshold + "): " + value);
	}

	public static void requireAbove(double value, String name, double threshold) {
		if (value <= threshold)
			throw new IllegalArgumentException("Expected " + name + " to be > " + threshold + ": " + value);
	}

	public static void requireAbove(double value, String name, double threshold, String thresholdName) {
		if (value <= threshold)
			throw new IllegalArgumentException("Expected " + name + " to be > " +
			                                   thresholdName + " (" + threshold + "): " + value);
	}

	public static void requireAbove(long value, String name, long threshold) {
		if (value <= threshold)
			throw new IllegalArgumentException("Expected " + name + " to be > " + threshold + ": " + value);
	}

	public static void requireAbove(long value, String name, long threshold, String thresholdName) {
		if (value <= threshold)
			throw new IllegalArgumentException("Expected " + name + " to be > " +
			                                   thresholdName + " (" + threshold + "): " + value);
	}

	public static void requireBelow(long value, String name, long threshold) {
		if (value >= threshold)
			throw new IllegalArgumentException("Expected " + name + " to be < " +
			                                   threshold + ": " + value);
	}

	public static void requireBelow(long value, String name, long threshold, String thresholdName) {
		if (value >= threshold)
			throw new IllegalArgumentException("Expected " + name + " to be < " +
			                                   thresholdName + " (" + threshold + "): " + value);
	}

	public static void requireAtMost(long value, String name, long threshold) {
		if (value > threshold)
			throw new IllegalArgumentException("Expected " + name + " to be <= " +
			                                   threshold + ": " + value);
	}

	public static void requireAtMost(long value, String name, long threshold, String thresholdName) {
		if (value > threshold)
			throw new IllegalArgumentException("Expected " + name + " to be <= " +
			                                   thresholdName + " (" + threshold + "): " + value);
	}

	public static void requireNotEqual(long value, String name, long exception) {
		if (value == exception)
			throw new IllegalArgumentException("Expected " + name + " to be != " + exception);
	}

	public static void requireNotEqual(long value, String name, long exception, String exceptionName) {
		if (value == exception)
			throw new IllegalArgumentException("Expected " + name + " to be != " +
			                                   exceptionName + " (" + exception + ")");
	}

	public static void requireSizeWithinBounds(int size, String name, int maxSize, String maxSizeName) {
		if (size < 0 || size > maxSize)
			throw new IndexOutOfBoundsException("Expected " + name + " to be >= " + 0 + " and <= " +
			                                    maxSizeName + " (" + maxSize + "): " + size);
	}

	public static <S extends SizedIterable<T>, T> S requireFinite(S iterable, String message) {
		if (iterable.sizeType() == INFINITE)
			throw new IllegalStateException(message);

		return iterable;
	}
}
