package org.d2ab.sequence;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
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
		Sequence<Integer> listSequence = ListSequence.from(new ArrayList<>(asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));

		Sequence<Integer> subList = listSequence.subList(2, 8);
		twice(() -> assertThat(subList, contains(3, 4, 5, 6, 7, 8)));

		assertThat(subList.remove(1), is(4));
		twice(() -> assertThat(subList, contains(3, 5, 6, 7, 8)));
		twice(() -> assertThat(listSequence, contains(1, 2, 3, 5, 6, 7, 8, 9, 10)));

		assertThat(subList.remove((Integer) 5), is(true));
		twice(() -> assertThat(subList, contains(3, 6, 7, 8)));
		twice(() -> assertThat(listSequence, contains(1, 2, 3, 6, 7, 8, 9, 10)));

		Iterator<Integer> subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.next(), is(3));
		subListIterator.remove();
		twice(() -> assertThat(subList, contains(6, 7, 8)));
		twice(() -> assertThat(listSequence, contains(1, 2, 6, 7, 8, 9, 10)));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, contains(7)));
		twice(() -> assertThat(listSequence, contains(1, 2, 7, 9, 10)));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(listSequence, contains(1, 2, 9, 10)));
	}
}
