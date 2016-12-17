package org.d2ab.sequence;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class ListSequenceTest {
	private final List<Integer> list = new ArrayList<>(asList(1, 2, 3, 4, 5));
	private final Sequence<Integer> sequence = ListSequence.from(list);
	private final Sequence<Integer> odds = sequence.filter(x -> x % 2 == 1);
	private final Sequence<String> strings = sequence.biMap(Object::toString, Integer::parseInt);

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
	public void testAsList() {
		assertThat(sequence.asList(), is(sameInstance(list)));
	}

	@Test
	public void filterAdd() {
		odds.add(17);
		expecting(IllegalArgumentException.class, () -> odds.add(18));

		assertThat(odds, contains(1, 3, 5, 17));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 17));
		assertThat(list, contains(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void filterRemove() {
		assertThat(odds.remove(3), is(true));
		assertThat(odds.remove(4), is(false));

		assertThat(odds, contains(1, 5));
		assertThat(sequence, contains(1, 2, 4, 5));
		assertThat(list, contains(1, 2, 4, 5));
	}

	@Test
	public void filterContains() {
		assertThat(odds.contains(3), is(true));
		assertThat(odds.contains(4), is(false));
	}

	@Test
	public void biMapAdd() {
		strings.add("6");
		assertThat(strings, contains("1", "2", "3", "4", "5", "6"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6));
		assertThat(list, contains(1, 2, 3, 4, 5, 6));

		expecting(NumberFormatException.class, () -> strings.add("foo"));
		assertThat(strings, contains("1", "2", "3", "4", "5", "6"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6));
		assertThat(list, contains(1, 2, 3, 4, 5, 6));
	}
}
