package org.d2ab.sequence;

import org.d2ab.collection.Iterables;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;

public class CollectionSequenceTest {
	private final Collection<Integer> emptyCollection = new ArrayDeque<>();
	private final Sequence<Integer> empty = CollectionSequence.from(emptyCollection);

	private final Collection<Integer> collection = new ArrayDeque<>(asList(1, 2, 3, 4, 5));
	private final Sequence<Integer> sequence = CollectionSequence.from(collection);

	private final Sequence<Integer> odds = sequence.filter(x -> x % 2 == 1);
	private final Sequence<String> strings = sequence.biMap(Object::toString, Integer::parseInt);
	private final Sequence<String> oddStrings = odds.biMap(Object::toString, Integer::parseInt);

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void concatCollectionOfCollections() {
		Collection<Integer> collection1 = new ArrayDeque<>(asList(1, 2, 3));
		Collection<Integer> collection2 = new ArrayDeque<>(asList(4, 5, 6));
		Collection<Integer> collection3 = new ArrayDeque<>(asList(7, 8, 9));
		Collection<Collection<Integer>> collectionCollection = new ArrayDeque<>(
				asList(collection1, collection2, collection3));

		Sequence<Integer> sequence = CollectionSequence.concat(collectionCollection);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 17)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		collectionCollection.add(new ArrayDeque<>(asList(10, 11, 12)));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void add() {
		assertThat(empty.add(17), is(true));
		twice(() -> assertThat(empty, contains(17)));

		assertThat(sequence.add(17), is(true));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 17)));
	}

	@Test
	public void addAllVarargs() {
		assertThat(empty.addAll(17, 18), is(true));
		twice(() -> assertThat(empty, contains(17, 18)));

		assertThat(sequence.addAll(17, 18), is(true));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 17, 18)));
	}

	@Test
	public void addAllIterable() {
		assertThat(empty.addAll(Iterables.of(17, 18)), is(true));
		twice(() -> assertThat(empty, contains(17, 18)));

		assertThat(sequence.addAll(Iterables.of(17, 18)), is(true));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 17, 18)));
	}

	@Test
	public void addAllCollection() {
		assertThat(empty.addAll(asList(17, 18)), is(true));
		twice(() -> assertThat(empty, contains(17, 18)));

		assertThat(sequence.addAll(asList(17, 18)), is(true));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 17, 18)));
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
		assertThat(odds.add(17), is(true));
		expecting(IllegalArgumentException.class, () -> odds.add(18));

		assertThat(odds, contains(1, 3, 5, 17));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 17));
		assertThat(collection, contains(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void filterAddAll() {
		assertThat(odds.addAll(asList(17, 19)), is(true));
		assertThat(odds, contains(1, 3, 5, 17, 19));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 17, 19));
		assertThat(collection, contains(1, 2, 3, 4, 5, 17, 19));

		expecting(IllegalArgumentException.class, () -> odds.addAll(asList(21, 22)));
		assertThat(odds, contains(1, 3, 5, 17, 19, 21));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 17, 19, 21));
		assertThat(collection, contains(1, 2, 3, 4, 5, 17, 19, 21));
	}

	@Test
	public void filterRemove() {
		assertThat(odds.remove(3), is(true));
		assertThat(odds.remove(4), is(false));

		assertThat(odds, contains(1, 5));
		assertThat(sequence, contains(1, 2, 4, 5));
		assertThat(collection, contains(1, 2, 4, 5));
	}

	@Test
	public void filterContains() {
		assertThat(odds.contains(3), is(true));
		assertThat(odds.contains(4), is(false));
		assertThat(odds.contains(17), is(false));
	}

	@Test
	public void biMapAdd() {
		assertThat(strings.add("6"), is(true));
		assertThat(strings, contains("1", "2", "3", "4", "5", "6"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6));
		assertThat(collection, contains(1, 2, 3, 4, 5, 6));

		expecting(NumberFormatException.class, () -> strings.add("foo"));
		assertThat(strings, contains("1", "2", "3", "4", "5", "6"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6));
		assertThat(collection, contains(1, 2, 3, 4, 5, 6));
	}

	@Test
	public void biMapAddAll() {
		assertThat(strings.addAll(asList("6", "7")), is(true));
		assertThat(strings, contains("1", "2", "3", "4", "5", "6", "7"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7));
		assertThat(collection, contains(1, 2, 3, 4, 5, 6, 7));

		expecting(NumberFormatException.class, () -> strings.addAll(asList("8", "foo")));
		assertThat(strings, contains("1", "2", "3", "4", "5", "6", "7", "8"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(collection, contains(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void biMapRemove() {
		assertThat(strings.remove("3"), is(true));
		assertThat(strings.remove("17"), is(false));

		assertThat(strings, contains("1", "2", "4", "5"));
		assertThat(sequence, contains(1, 2, 4, 5));
		assertThat(collection, contains(1, 2, 4, 5));
	}

	@Test
	public void biMapContains() {
		assertThat(strings.contains("3"), is(true));
		assertThat(strings.contains("17"), is(false));
	}

	@Test
	public void filterBiMapAdd() {
		assertThat(oddStrings.add("7"), is(true));
		assertThat(oddStrings, contains("1", "3", "5", "7"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 7));
		assertThat(collection, contains(1, 2, 3, 4, 5, 7));

		expecting(NumberFormatException.class, () -> oddStrings.add("foo"));
		assertThat(oddStrings, contains("1", "3", "5", "7"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 7));
		assertThat(collection, contains(1, 2, 3, 4, 5, 7));
	}

	@Test
	public void filterBiMapAddAll() {
		assertThat(oddStrings.addAll(asList("7", "9")), is(true));
		assertThat(oddStrings, contains("1", "3", "5", "7", "9"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 7, 9));
		assertThat(collection, contains(1, 2, 3, 4, 5, 7, 9));

		expecting(NumberFormatException.class, () -> oddStrings.addAll(asList("11", "foo")));
		assertThat(oddStrings, contains("1", "3", "5", "7", "9", "11"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 7, 9, 11));
		assertThat(collection, contains(1, 2, 3, 4, 5, 7, 9, 11));
	}

	@Test
	public void filterBiMapRemove() {
		assertThat(oddStrings.remove("3"), is(true));
		assertThat(oddStrings.remove("17"), is(false));

		assertThat(oddStrings, contains("1", "5"));
		assertThat(sequence, contains(1, 2, 4, 5));
		assertThat(collection, contains(1, 2, 4, 5));
	}

	@Test
	public void filterBiMapContains() {
		assertThat(oddStrings.contains("3"), is(true));
		assertThat(oddStrings.contains("4"), is(false));
		assertThat(oddStrings.contains("17"), is(false));
	}
}
