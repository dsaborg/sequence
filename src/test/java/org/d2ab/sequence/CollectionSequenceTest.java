package org.d2ab.sequence;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class CollectionSequenceTest {
	private final Collection<Integer> collection = new ArrayDeque<>(asList(1, 2, 3, 4, 5));
	private final Sequence<Integer> sequence = CollectionSequence.from(collection);
	private final Sequence<Integer> odds = sequence.filter(x -> x % 2 == 1);

	@Test
	public void add() {
		sequence.add(17);

		assertThat(sequence, contains(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void remove() {
		assertThat(sequence.remove(3), is(true));
		assertThat(sequence, contains(1, 2, 4, 5));

		assertThat(sequence.remove(17), is(false));
		assertThat(sequence, contains(1, 2, 4, 5));
	}

	@Test
	public void containsInteger() {
		assertThat(sequence.contains(3), is(true));
		assertThat(sequence.contains(17), is(false));
	}

	@Test
	public void filterAdd() {
		odds.add(17);
		expecting(IllegalArgumentException.class, () -> odds.add(18));

		assertThat(odds, contains(1, 3, 5, 17));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void filterRemove() {
		assertThat(odds.remove(3), is(true));
		assertThat(odds.remove(4), is(false));

		assertThat(odds, contains(1, 5));
		assertThat(sequence, contains(1, 2, 4, 5));
	}

	@Test
	public void filterContains() {
		assertThat(odds.contains(3), is(true));
		assertThat(odds.contains(4), is(false));
	}
}
