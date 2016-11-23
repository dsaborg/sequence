package org.d2ab.sequence;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ListSequenceTest {
	@Test
	public void add() {
		ListSequence<Integer> listSequence = new ListSequence<>();
		listSequence.add(17);

		assertThat(listSequence, contains(17));
	}

	@Test
	public void remove() {
		ListSequence<Integer> listSequence = new ListSequence<>();
		listSequence.add(17);

		assertThat(listSequence, contains(17));

		listSequence.remove((Integer) 17);
		assertThat(listSequence, is(emptyIterable()));
	}

	@Test
	public void containsInteger() {
		ListSequence<Integer> listSequence = new ListSequence<>();
		listSequence.add(17);

		assertThat(listSequence.contains(17), is(true));
	}
}
