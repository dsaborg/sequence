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

package org.d2ab.util;

import org.d2ab.iterator.ArrayIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Utilities for arrays, similar to {@link Arrays} with a few extras like iterators and {@link #forEach}.
 */
public class Arrayz {
	private Arrayz() {
	}

	@SafeVarargs
	public static <T> void forEach(Consumer<? super T> action, T... array) {
		requireNonNull(action);
		for (T item : requireNonNull(array))
			action.accept(item);
	}

	@SafeVarargs
	public static <T> Iterator<T> iterator(T... items) {
		return new ArrayIterator<>(requireNonNull(items));
	}

	@SafeVarargs
	public static <T> Iterable<T> iterable(T... items) {
		return () -> new ArrayIterator<>(requireNonNull(items));
	}

	public static void swap(long[] array, int i, int j) {
		requireNonNull(array);
		long temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	public static void swap(int[] array, int i, int j) {
		requireNonNull(array);
		int temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	public static void swap(char[] array, int i, int j) {
		requireNonNull(array);
		char temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	public static void swap(double[] array, int i, int j) {
		requireNonNull(array);
		double temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}
}
