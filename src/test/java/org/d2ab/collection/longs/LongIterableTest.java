package org.d2ab.collection.longs;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LongIterableTest {
	LongIterable empty = LongIterable.of();
	LongIterable iterable = LongIterable.from(LongList.create(1, 2, 3, 4, 5));

	@Test
	public void isEmpty() {
		assertThat(empty.isEmpty(), is(true));
		assertThat(iterable.isEmpty(), is(false));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty, is(emptyIterable()));

		iterable.clear();
		assertThat(iterable, is(emptyIterable()));
	}

	@Test
	public void longStream() {
		assertThat(empty.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           CoreMatchers.is(emptyIterable()));

		assertThat(iterable.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           containsLongs(1, 2, 3, 4, 5));
	}

	@Test
	public void parallelLongStream() {
		assertThat(empty.parallelLongStream()
		                .collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           CoreMatchers.is(emptyIterable()));

		assertThat(iterable.parallelLongStream()
		                   .collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           containsLongs(1, 2, 3, 4, 5));
	}
}
