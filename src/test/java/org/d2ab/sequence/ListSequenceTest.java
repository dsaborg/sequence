package org.d2ab.sequence;

import org.junit.Test;

import java.util.List;

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

	@Test
	public void subList() {
		ListSequence<Integer> listSequence = new ListSequence<>();
		listSequence.add(1);
		listSequence.add(2);
		listSequence.add(3);
		listSequence.add(4);
		listSequence.add(5);

		List<Integer> subList = listSequence.subList(1, 4);
		assertThat(subList, contains(2, 3, 4));

		subList.clear();
		assertThat(subList, is(emptyIterable()));

		assertThat(listSequence, contains(1, 5));
	}
}
