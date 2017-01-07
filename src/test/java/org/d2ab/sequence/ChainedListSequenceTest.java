package org.d2ab.sequence;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ChainedListSequenceTest {
	private final List<Integer> list1 = new ArrayList<>(asList(1, 2, 3));
	private final List<Integer> list2 = new ArrayList<>(asList(4, 5));
	private final Sequence<Integer> listSequence = ListSequence.concat(list1, list2);
	private final Sequence<Integer> odds = listSequence.filter(x -> x % 2 == 1);

	@Test
	public void add() {
		listSequence.add(17);

		assertThat(listSequence, contains(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void remove() {
		assertThat(listSequence.remove(3), is(true));
		assertThat(listSequence, contains(1, 2, 4, 5));

		assertThat(listSequence.remove(17), is(false));
		assertThat(listSequence, contains(1, 2, 4, 5));
	}

	@Test
	public void containsInteger() {
		assertThat(listSequence.contains(3), is(true));
		assertThat(listSequence.contains(17), is(false));
	}

	@Test
	public void testAsList() {
		assertThat(listSequence.asList(), contains(1, 2, 3, 4, 5));
	}

	@Test
	public void filterAdd() {
		odds.add(17);
		expecting(IllegalArgumentException.class, () -> odds.add(18));

		assertThat(odds, contains(1, 3, 5, 17));
		assertThat(listSequence, contains(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void filterRemove() {
		assertThat(odds.remove(3), is(true));
		assertThat(odds.remove(4), is(false));

		assertThat(odds, contains(1, 5));
		assertThat(listSequence, contains(1, 2, 4, 5));
	}

	@Test
	public void filterContains() {
		assertThat(odds.contains(3), is(true));
		assertThat(odds.contains(4), is(false));
	}
}
