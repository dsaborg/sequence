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
package org.d2ab.test;

import org.d2ab.collection.chars.CharIterable;
import org.d2ab.collection.doubles.DoubleIterable;
import org.d2ab.collection.ints.IntIterable;
import org.d2ab.collection.longs.LongIterable;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.iterator.longs.LongIterator;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class Tests {
	@FunctionalInterface
	public interface ThrowingRunnable {
		void run() throws Exception;
	}

	private Tests() {
	}

	public static void expecting(Class<? extends Exception> exceptionClass, ThrowingRunnable action) {
		try {
			action.run();
			fail("Expected " + exceptionClass.getName());
		} catch (Exception t) {
			if (!exceptionClass.isInstance(t))
				throw new AssertionError("Expected " + exceptionClass.getName() + " but got: " + t, t);
		}
	}

	public static <T> T removeFirst(Iterable<T> iterable) {
		Iterator<T> iterator = iterable.iterator();
		T first = iterator.next();
		iterator.remove();
		return first;
	}

	public static char removeFirst(CharIterable iterable) {
		CharIterator iterator = iterable.iterator();
		char first = iterator.nextChar();
		iterator.remove();
		return first;
	}

	public static double removeFirst(DoubleIterable iterable) {
		DoubleIterator iterator = iterable.iterator();
		double first = iterator.nextDouble();
		iterator.remove();
		return first;
	}

	public static long removeFirst(LongIterable iterable) {
		LongIterator iterator = iterable.iterator();
		long first = iterator.nextLong();
		iterator.remove();
		return first;
	}

	public static int removeFirst(IntIterable iterable) {
		IntIterator iterator = iterable.iterator();
		int first = iterator.nextInt();
		iterator.remove();
		return first;
	}

	public static void twice(Runnable action) {
		times(2, action);
	}

	public static void times(int times, Runnable action) {
		while (times-- > 0)
			action.run();
	}

	public static void twiceIndexed(AtomicLong index, long expectedIncrements, Runnable r) {
		long previousValue = index.get();
		twice(() -> {
			r.run();
			assertThat(index.get(), is(previousValue + expectedIncrements));
			index.set(previousValue);
		});
	}

	public static void twiceIndexed(AtomicInteger index, int expectedIncrement, Runnable r) {
		int previousValue = index.get();
		twice(() -> {
			r.run();
			assertThat(index.get(), is(previousValue + expectedIncrement));
			index.set(previousValue);
		});
	}
}
