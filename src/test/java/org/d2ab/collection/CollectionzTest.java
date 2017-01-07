package org.d2ab.collection;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CollectionzTest {
	@Test
	public void testAsList() {
		Collection<Integer> collection = new ArrayDeque<>(asList(1, 2, 3));
		List<Integer> asList = Collectionz.asList(collection);
		assertThat(asList, is(equalTo(new ArrayList<>(collection))));
	}

	@Test
	public void testAsListWithList() {
		List<Integer> list = new ArrayList<>(asList(1, 2, 3));
		assertThat(Collectionz.asList(list), is(sameInstance(list)));
	}
}