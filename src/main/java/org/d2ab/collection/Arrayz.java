/*
 * Copyright 2016 Daniel Skogquist Åborg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.d2ab.collection;

import org.d2ab.util.Doubles;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utilities for arrays, similar to {@link Arrays} with a few extras like iterators and {@link #forEach}.
 */
public abstract class Arrayz {
	Arrayz() {
	}

	/**
	 * Perform the given action once for each item in the given array.
	 */
	public static <T> void forEach(T[] array, Consumer<? super T> action) {
		for (T item : array)
			action.accept(item);
	}

	/**
	 * Reverse the given array in place.
	 *
	 * @return the given array, reversed.
	 */
	@SafeVarargs
	public static <T> T[] reverse(T... array) {
		int length = array.length;
		int half = length / 2;
		for (int i = 0, j = length - 1; i < half; i++, j--) {
			T temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}
		return array;
	}

	/**
	 * Reverse the given array in place.
	 *
	 * @return the given array, reversed.
	 */
	public static double[] reverse(double... array) {
		int length = array.length;
		int half = length / 2;
		for (int i = 0, j = length - 1; i < half; i++, j--) {
			double temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}
		return array;
	}

	/**
	 * Reverse the given array in place.
	 *
	 * @return the given array, reversed.
	 */
	public static int[] reverse(int... array) {
		int length = array.length;
		int half = length / 2;
		for (int i = 0, j = length - 1; i < half; i++, j--) {
			int temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}
		return array;
	}

	/**
	 * Reverse the given array in place.
	 *
	 * @return the given array, reversed.
	 */
	public static long[] reverse(long... array) {
		int length = array.length;
		int half = length / 2;
		for (int i = 0, j = length - 1; i < half; i++, j--) {
			long temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}
		return array;
	}

	/**
	 * Reverse the given array in place.
	 *
	 * @return the given array, reversed.
	 */
	public static char[] reverse(char... array) {
		int length = array.length;
		int half = length / 2;
		for (int i = 0, j = length - 1; i < half; i++, j--) {
			char temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}
		return array;
	}

	/**
	 * @return true if the given array contains the given target object, false otherwise.
	 */
	public static boolean contains(Object[] elements, Object target) {
		for (Object element : elements)
			if (Objects.equals(element, target))
				return true;
		return false;
	}

	/**
	 * @return true if the given array contains the given target {@code char}, false otherwise.
	 */
	public static boolean contains(char[] items, char target) {
		for (char item : items)
			if (item == target)
				return true;
		return false;
	}

	/**
	 * @return true if the given array contains the given target {@code int}, false otherwise.
	 */
	public static boolean contains(int[] items, int target) {
		for (int item : items)
			if (item == target)
				return true;
		return false;
	}

	/**
	 * @return true if the given array contains the given target {@code long}, false otherwise.
	 */
	public static boolean contains(long[] items, long target) {
		for (long item : items)
			if (item == target)
				return true;
		return false;
	}

	/**
	 * @return true if the given array contains exactly the given target {@code double}, false otherwise.
	 */
	public static boolean containsExactly(double[] items, double target) {
		for (double item : items)
			if (item == target)
				return true;
		return false;
	}

	/**
	 * @return true if the given array contains the given target {@code double} within the given precision, false
	 * otherwise.
	 */
	public static boolean contains(double[] items, double target, double precision) {
		for (double item : items)
			if (Doubles.eq(item, target, precision))
				return true;
		return false;
	}

	/**
	 * Fill the given array with the given value.
	 *
	 * @return the given array, for call chaining.
	 */
	public static <T> T[] fill(T[] array, T value) {
		Arrays.fill(array, value);
		return array;
	}

	/**
	 * @return true if all the items in the given array satisfy the given predicate.
	 */
	public static <T> boolean all(T[] items, Predicate<? super T> predicate) {
		for (T each : Objects.requireNonNull(items, "items"))
			if (!predicate.test(each))
				return false;

		return true;
	}

	public static <T> T[] sort(T[] array, Comparator<? super T> comparator) {
		Arrays.sort(array, comparator);
		return array;
	}

	public static <T> T[] shuffle(T[] array, Random random) {
		for (int i = array.length - 1; i >= 1; i--) {
			int randomIndex = random.nextInt(i + 1);
			if (randomIndex == i)
				continue;

			T temp = array[randomIndex];
			array[randomIndex] = array[i];
			array[i] = temp;
		}
		return array;
	}
}
