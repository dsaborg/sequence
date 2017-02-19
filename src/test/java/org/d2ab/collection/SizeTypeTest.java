package org.d2ab.collection;

import org.junit.Test;

import static org.d2ab.collection.SizedIterable.SizeType.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SizeTypeTest {
	@Test
	public void unavailableConcat() {
		assertThat(UNAVAILABLE.concat(UNAVAILABLE), is(UNAVAILABLE));
		assertThat(UNAVAILABLE.concat(AVAILABLE), is(UNAVAILABLE));
		assertThat(UNAVAILABLE.concat(FIXED), is(UNAVAILABLE));
		assertThat(UNAVAILABLE.concat(INFINITE), is(INFINITE));
	}

	@Test
	public void unavailableIntersect() {
		assertThat(UNAVAILABLE.intersect(UNAVAILABLE), is(UNAVAILABLE));
		assertThat(UNAVAILABLE.intersect(AVAILABLE), is(UNAVAILABLE));
		assertThat(UNAVAILABLE.intersect(FIXED), is(UNAVAILABLE));
		assertThat(UNAVAILABLE.intersect(INFINITE), is(UNAVAILABLE));
	}

	@Test
	public void unavailableLimited() {
		assertThat(UNAVAILABLE.limited(), is(UNAVAILABLE));
	}

	@Test
	public void unavailableLimitedSize() {
		assertThat(UNAVAILABLE.limitedSize(null, (SizedIterable<Integer>) Iterables.of(1, 2, 3, 4, 5)::iterator, 10),
		           is(5));
	}

	@Test
	public void availableConcat() {
		assertThat(AVAILABLE.concat(UNAVAILABLE), is(UNAVAILABLE));
		assertThat(AVAILABLE.concat(AVAILABLE), is(AVAILABLE));
		assertThat(AVAILABLE.concat(FIXED), is(AVAILABLE));
		assertThat(AVAILABLE.concat(INFINITE), is(INFINITE));
	}

	@Test
	public void availableIntersect() {
		assertThat(AVAILABLE.intersect(UNAVAILABLE), is(UNAVAILABLE));
		assertThat(AVAILABLE.intersect(AVAILABLE), is(AVAILABLE));
		assertThat(AVAILABLE.intersect(FIXED), is(AVAILABLE));
		assertThat(AVAILABLE.intersect(INFINITE), is(AVAILABLE));
	}

	@Test
	public void availableLimited() {
		assertThat(AVAILABLE.limited(), is(AVAILABLE));
	}

	@Test
	public void availableLimitedSize() {
		assertThat(AVAILABLE.limitedSize(Iterables.of(1, 2, 3, 4, 5), null, 10),
		           is(5));

		assertThat(AVAILABLE.limitedSize(Iterables.of(1, 2, 3, 4, 5), null, 1),
		           is(1));
	}

	@Test
	public void fixedConcat() {
		assertThat(FIXED.concat(UNAVAILABLE), is(UNAVAILABLE));
		assertThat(FIXED.concat(AVAILABLE), is(AVAILABLE));
		assertThat(FIXED.concat(FIXED), is(FIXED));
		assertThat(FIXED.concat(INFINITE), is(INFINITE));
	}

	@Test
	public void fixedIntersect() {
		assertThat(FIXED.intersect(UNAVAILABLE), is(UNAVAILABLE));
		assertThat(FIXED.intersect(AVAILABLE), is(AVAILABLE));
		assertThat(FIXED.intersect(FIXED), is(FIXED));
		assertThat(FIXED.intersect(INFINITE), is(FIXED));
	}

	@Test
	public void fixedLimited() {
		assertThat(FIXED.limited(), is(FIXED));
	}

	@Test
	public void fixedLimitedSize() {
		assertThat(FIXED.limitedSize(Iterables.of(1, 2, 3, 4, 5), null, 10),
		           is(5));

		assertThat(FIXED.limitedSize(Iterables.of(1, 2, 3, 4, 5), null, 1),
		           is(1));
	}

	@Test
	public void infiniteConcat() {
		assertThat(INFINITE.concat(UNAVAILABLE), is(INFINITE));
		assertThat(INFINITE.concat(AVAILABLE), is(INFINITE));
		assertThat(INFINITE.concat(INFINITE), is(INFINITE));
	}

	@Test
	public void infiniteIntersect() {
		assertThat(INFINITE.intersect(UNAVAILABLE), is(UNAVAILABLE));
		assertThat(INFINITE.intersect(AVAILABLE), is(AVAILABLE));
		assertThat(INFINITE.intersect(INFINITE), is(INFINITE));
	}

	@Test
	public void infiniteLimited() {
		assertThat(INFINITE.limited(), is(AVAILABLE));
	}

	@Test
	public void infiniteLimitedSize() {
		assertThat(INFINITE.limitedSize(null, null, 10), is(10));
	}
}