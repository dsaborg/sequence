package org.d2ab.collection.doubles;

import org.junit.Test;

import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DoubleIterableTest {
	DoubleIterable empty = DoubleIterable.of();
	DoubleIterable iterable = DoubleIterable.from(DoubleList.create(1, 2, 3, 4, 5));

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
	public void doubleStream() {
		assertThat(empty.doubleStream().collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           is(emptyIterable()));

		assertThat(iterable.doubleStream().collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           containsDoubles(1, 2, 3, 4, 5));
	}

	@Test
	public void parallelDoubleStream() {
		assertThat(empty.parallelDoubleStream()
		                .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           is(emptyIterable()));

		assertThat(iterable.parallelDoubleStream()
		                   .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           containsDoubles(1, 2, 3, 4, 5));
	}
}
