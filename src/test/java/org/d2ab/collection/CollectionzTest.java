package org.d2ab.collection;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CollectionzTest {
	@Test
	public void constructor() {
		new Collectionz() {
			// code coverage
		};
	}

	@Test
	public void testAsList() {
		Collection<Integer> collection = new ArrayDeque<>(Lists.of(1, 2, 3));
		List<Integer> asList = Collectionz.asList(collection);
		assertThat(asList, is(equalTo(new ArrayList<>(collection))));
	}

	@Test
	public void testAsListWithList() {
		List<Integer> list = new ArrayList<>(Lists.of(1, 2, 3));
		assertThat(Collectionz.asList(list), is(sameInstance(list)));
	}
}