package org.d2ab.collection;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class ArrayzTest {
	@Test
	public void constructor() {
		new Arrayz() {
			// code coverage
		};
	}

	@Test
	public void forEach() throws Exception {
		AtomicInteger i = new AtomicInteger();
		Arrayz.forEach(new Integer[]{1, 2, 3, 4, 5}, x -> assertThat(x, is(i.getAndIncrement() + 1)));
		assertThat(i.get(), is(5));
	}

	@Test
	public void reverse() throws Exception {
		Integer[] xs = {1, 2, 3, 4, 5};
		assertThat(Arrayz.reverse((Object[]) xs), is(sameInstance(xs)));
		assertThat(xs, is(arrayContaining(5, 4, 3, 2, 1)));
	}

	@Test
	public void reverseLongs() throws Exception {
		long[] xs = {1, 2, 3, 4, 5};
		assertThat(Arrayz.reverse(xs), is(sameInstance(xs)));
		assertArrayEquals(new long[]{5, 4, 3, 2, 1}, xs);
	}

	@Test
	public void reverseInts() throws Exception {
		int[] xs = {1, 2, 3, 4, 5};
		assertThat(Arrayz.reverse(xs), is(sameInstance(xs)));
		assertArrayEquals(new int[]{5, 4, 3, 2, 1}, xs);
	}

	@Test
	public void reverseDoubles() throws Exception {
		double[] xs = {1, 2, 3, 4, 5};
		assertThat(Arrayz.reverse(xs), is(sameInstance(xs)));
		assertArrayEquals(new double[]{5, 4, 3, 2, 1}, xs, 0);
	}

	@Test
	public void reverseChars() throws Exception {
		char[] xs = {'a', 'b', 'c', 'd', 'e'};
		assertThat(Arrayz.reverse(xs), is(sameInstance(xs)));
		assertArrayEquals(new char[]{'e', 'd', 'c', 'b', 'a'}, xs);
	}

	@Test
	public void contains() throws Exception {
		Integer[] xs = {1, 2, 3, 4, 5};
		for (Integer x : xs)
			assertThat(Arrayz.contains(xs, x), is(true));
		assertThat(Arrayz.contains(xs, 0), is(false));
		assertThat(Arrayz.contains(xs, 6), is(false));
	}

	@Test
	public void containsLong() throws Exception {
		long[] xs = {1, 2, 3, 4, 5};
		for (long x : xs)
			assertThat(Arrayz.contains(xs, x), is(true));
		assertThat(Arrayz.contains(xs, 0), is(false));
		assertThat(Arrayz.contains(xs, 6), is(false));
	}

	@Test
	public void containsInt() throws Exception {
		int[] xs = {1, 2, 3, 4, 5};
		for (int x : xs)
			assertThat(Arrayz.contains(xs, x), is(true));
		assertThat(Arrayz.contains(xs, 0), is(false));
		assertThat(Arrayz.contains(xs, 6), is(false));
	}

	@Test
	public void containsDoubleExactly() throws Exception {
		double[] xs = {1, 2, 3, 4, 5};
		for (double x : xs)
			assertThat(Arrayz.containsExactly(xs, x), is(true));
		assertThat(Arrayz.containsExactly(xs, 0), is(false));
		assertThat(Arrayz.containsExactly(xs, 6), is(false));
	}

	@Test
	public void containsDouble() throws Exception {
		double[] xs = {1, 2, 3, 4, 5};
		for (double x : xs) {
			assertThat(Arrayz.contains(xs, x, 0), is(true));
			assertThat(Arrayz.contains(xs, x + 0.1, 0.2), is(true));
			assertThat(Arrayz.contains(xs, x - 0.1, 0.2), is(true));
		}
		assertThat(Arrayz.contains(xs, 0, 0), is(false));
		assertThat(Arrayz.contains(xs, 6, 0), is(false));
	}

	@Test
	public void containsChar() throws Exception {
		char[] xs = {'a', 'b', 'c', 'd', 'e'};
		for (char x : xs)
			assertThat(Arrayz.contains(xs, x), is(true));
		assertThat(Arrayz.contains(xs, ' '), is(false));
		assertThat(Arrayz.contains(xs, 'q'), is(false));
	}
}