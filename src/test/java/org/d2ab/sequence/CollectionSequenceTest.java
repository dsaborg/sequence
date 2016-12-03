package org.d2ab.sequence;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;

public class CollectionSequenceTest {
	@Test
	public void add() {
		CollectionSequence<Integer> collectionSequence = new CollectionSequence<>();
		collectionSequence.add(17);

		assertThat(collectionSequence, contains(17));
	}

	@Test
	public void remove() {
		CollectionSequence<Integer> collectionSequence = new CollectionSequence<>();
		collectionSequence.add(17);

		assertThat(collectionSequence, contains(17));

		collectionSequence.remove((Integer) 17);
		assertThat(collectionSequence, is(emptyIterable()));
	}

	@Test
	public void containsInteger() {
		CollectionSequence<Integer> collectionSequence = new CollectionSequence<>();
		collectionSequence.add(17);

		assertThat(collectionSequence.contains(17), is(true));
	}
}
