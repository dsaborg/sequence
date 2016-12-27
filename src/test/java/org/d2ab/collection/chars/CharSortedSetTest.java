package org.d2ab.collection.chars;

import org.junit.Test;

import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CharSortedSetTest {
	@Test
	public void createEmpty() {
		CharSortedSet set = CharSortedSet.create();
		assertThat(set, is(emptyIterable()));

		set.addChar('b');
		set.addChar('c');
		set.addChar('a');
		assertThat(set, containsChars('a', 'b', 'c'));
	}

	@Test
	public void create() {
		CharSortedSet set = CharSortedSet.create('b', 'c', 'a');
		assertThat(set, containsChars('a', 'b', 'c'));

		set.addChar('e');
		set.addChar('d');
		assertThat(set, containsChars('a', 'b', 'c', 'd', 'e'));
	}
}