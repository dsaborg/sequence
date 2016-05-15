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

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Utilities for arrays, similar to {@link Arrays} with a few extras like iterators and {@link #forEach}.
 */
public class Arrayz {
	private Arrayz() {
	}

	/**
	 * Perform the given action once for each item in the given array.
	 */
	public static <T> void forEach(T[] array, Consumer<? super T> action) {
		for (T item : array)
			action.accept(item);
	}

	/**
	 * Swap the given items in the given array.
	 */
	public static void swap(long[] array, int i, int j) {
		long temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	/**
	 * Swap the given items in the given array.
	 */
	public static void swap(int[] array, int i, int j) {
		int temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	/**
	 * Swap the given items in the given array.
	 */
	public static void swap(char[] array, int i, int j) {
		char temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	/**
	 * Swap the given items in the given array.
	 */
	public static void swap(double[] array, int i, int j) {
		double temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	/**
	 * Swap the given items in the given array.
	 */
	public static void swap(Object[] array, int i, int j) {
		Object temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	/**
	 * Reverse the given array in place.
	 *
	 * @return the given array.
	 */
	public static Object[] reverse(Object... array) {
		for (int i = 0; i < array.length / 2; i++)
			swap(array, i, array.length - i - 1);
		return array;
	}

	/**
	 * Reverse the given array in place.
	 *
	 * @return the given array.
	 */
	public static double[] reverse(double... array) {
		for (int i = 0; i < array.length / 2; i++)
			swap(array, i, array.length - 1 - i);
		return array;
	}

	/**
	 * Reverse the given array in place.
	 *
	 * @return the given array.
	 */
	public static int[] reverse(int... array) {
		for (int i = 0; i < array.length / 2; i++)
			swap(array, i, array.length - 1 - i);
		return array;
	}

	/**
	 * Reverse the given array in place.
	 *
	 * @return the given array.
	 */
	public static long[] reverse(long... array) {
		for (int i = 0; i < array.length / 2; i++)
			swap(array, i, array.length - 1 - i);
		return array;
	}

	/**
	 * Reverse the given array in place.
	 *
	 * @return the given array.
	 */
	public static char[] reverse(char... array) {
		for (int i = 0; i < array.length / 2; i++)
			swap(array, i, array.length - 1 - i);
		return array;
	}

	/**
	 * @return true if the given array contains the given target object, false otherwise.
	 */
	public static <T> boolean contains(T[] elements, T target) {
		for (T element : elements)
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
	 * @return true if the given array contains the given target {@code double}, false otherwise.
	 */
	public static boolean contains(double[] items, double target, double precision) {
		for (double item : items)
			if (DoubleComparator.compare(item, target, precision))
				return true;
		return false;
	}
}
