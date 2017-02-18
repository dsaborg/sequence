package org.d2ab.collection;

import org.junit.Test;

import static org.d2ab.collection.SizedIterable.SizeType.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SizeTypeTest {
	@Test
	public void unknownConcat() {
		assertThat(UNKNOWN.concat(UNKNOWN), is(UNKNOWN));
		assertThat(UNKNOWN.concat(KNOWN), is(UNKNOWN));
		assertThat(UNKNOWN.concat(INFINITE), is(INFINITE));
	}

	@Test
	public void unknownIntersect() {
		assertThat(UNKNOWN.intersect(UNKNOWN), is(UNKNOWN));
		assertThat(UNKNOWN.intersect(KNOWN), is(UNKNOWN));
		assertThat(UNKNOWN.intersect(INFINITE), is(UNKNOWN));
	}

	@Test
	public void unknownLimited() {
		assertThat(UNKNOWN.limited(), is(UNKNOWN));
	}

	@Test
	public void unknownLimitedSize() {
		assertThat(UNKNOWN.limitedSize(null, (SizedIterable<Integer>) Iterables.of(1, 2, 3, 4, 5)::iterator, 10),
		           is(5));
	}

	@Test
	public void knownConcat() {
		assertThat(KNOWN.concat(UNKNOWN), is(UNKNOWN));
		assertThat(KNOWN.concat(KNOWN), is(KNOWN));
		assertThat(KNOWN.concat(INFINITE), is(INFINITE));
	}

	@Test
	public void knownIntersect() {
		assertThat(KNOWN.intersect(UNKNOWN), is(UNKNOWN));
		assertThat(KNOWN.intersect(KNOWN), is(KNOWN));
		assertThat(KNOWN.intersect(INFINITE), is(KNOWN));
	}

	@Test
	public void knownLimited() {
		assertThat(KNOWN.limited(), is(KNOWN));
	}

	@Test
	public void knownLimitedSize() {
		assertThat(KNOWN.limitedSize(Iterables.of(1, 2, 3, 4, 5), null, 10),
		           is(5));

		assertThat(KNOWN.limitedSize(Iterables.of(1, 2, 3, 4, 5), null, 1),
		           is(1));
	}

	@Test
	public void infiniteConcat() {
		assertThat(INFINITE.concat(UNKNOWN), is(INFINITE));
		assertThat(INFINITE.concat(KNOWN), is(INFINITE));
		assertThat(INFINITE.concat(INFINITE), is(INFINITE));
	}

	@Test
	public void infiniteIntersect() {
		assertThat(INFINITE.intersect(UNKNOWN), is(UNKNOWN));
		assertThat(INFINITE.intersect(KNOWN), is(KNOWN));
		assertThat(INFINITE.intersect(INFINITE), is(INFINITE));
	}

	@Test
	public void infiniteLimited() {
		assertThat(INFINITE.limited(), is(KNOWN));
	}

	@Test
	public void infiniteLimitedSize() {
		assertThat(INFINITE.limitedSize(null, null, 10), is(10));
	}
}