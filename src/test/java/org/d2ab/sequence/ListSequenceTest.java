package org.d2ab.sequence;

import org.d2ab.collection.Iterables;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ListSequenceTest {
	private final List<Integer> emptyList = new ArrayList<>();
	private final Sequence<Integer> empty = ListSequence.from(emptyList);

	private final List<Integer> list = new ArrayList<>(asList(1, 2, 3, 4, 5));
	private final Sequence<Integer> sequence = ListSequence.from(list);

	private final Sequence<Integer> odds = sequence.filter(x -> x % 2 == 1);
	private final Sequence<String> oddStrings = odds.biMap(Object::toString, Integer::parseInt);

	private final Sequence<Integer> evens = sequence.filter(x -> x % 2 == 0);
	private final Sequence<String> evenStrings = evens.biMap(Object::toString, Integer::parseInt);

	private final Sequence<String> strings = sequence.biMap(Object::toString, Integer::parseInt);

	@Test
	public void empty() {
		Sequence<Integer> sequence = ListSequence.empty();
		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void emptyImmutable() {
		List<Integer> list = ListSequence.<Integer>empty().asList();
		expecting(UnsupportedOperationException.class, () -> list.add(1));
		expecting(UnsupportedOperationException.class, () -> list.add(0, 0));
		expecting(UnsupportedOperationException.class, () -> list.addAll(asList(1, 2)));
		expecting(UnsupportedOperationException.class, () -> list.addAll(0, asList(-1, 0)));
		expecting(UnsupportedOperationException.class, () -> list.remove(0));
	}

	@Test
	public void ofNone() {
		Sequence<Integer> sequence = ListSequence.of();
		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void ofNoneImmutable() {
		List<Integer> list = ListSequence.<Integer>of().asList();
		expecting(UnsupportedOperationException.class, () -> list.add(1));
		expecting(UnsupportedOperationException.class, () -> list.add(0, 0));
		expecting(UnsupportedOperationException.class, () -> list.addAll(asList(1, 2)));
		expecting(UnsupportedOperationException.class, () -> list.addAll(0, asList(-1, 0)));
		expecting(UnsupportedOperationException.class, () -> list.remove(0));
	}

	@Test
	public void ofOne() {
		Sequence<Integer> sequence = ListSequence.of(1);
		twice(() -> assertThat(sequence, contains(1)));
	}

	@Test
	public void ofOneImmutable() {
		List<Integer> list = ListSequence.of(1).asList();
		expecting(UnsupportedOperationException.class, () -> list.add(2));
		expecting(UnsupportedOperationException.class, () -> list.add(0, 0));
		expecting(UnsupportedOperationException.class, () -> list.addAll(asList(2, 3)));
		expecting(UnsupportedOperationException.class, () -> list.addAll(0, asList(-1, 0)));
		expecting(UnsupportedOperationException.class, () -> list.remove(0));
		expecting(UnsupportedOperationException.class, () -> list.set(0, 17));
	}

	@Test
	public void ofMany() {
		Sequence<Integer> sequence = ListSequence.of(1, 2, 3, 4, 5);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void ofManyImmutable() {
		List<Integer> list = ListSequence.of(1, 2, 3, 4, 5).asList();
		expecting(UnsupportedOperationException.class, () -> list.add(6));
		expecting(UnsupportedOperationException.class, () -> list.add(0, 0));
		expecting(UnsupportedOperationException.class, () -> list.addAll(asList(6, 7)));
		expecting(UnsupportedOperationException.class, () -> list.addAll(0, asList(-1, 0)));
		expecting(UnsupportedOperationException.class, () -> list.remove(0));
		expecting(UnsupportedOperationException.class, () -> list.set(0, 17));
	}

	@Test
	public void ofNulls() {
		Sequence<Integer> sequence = ListSequence.of(1, null, 2, 3, null);
		twice(() -> assertThat(sequence, contains(1, null, 2, 3, null)));
	}

	@Test
	public void fromEmpty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void create() {
		Sequence<Integer> sequence = ListSequence.create();
		twice(() -> assertThat(sequence, is(emptyIterable())));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(17)));
	}

	@Test
	public void withCapacity() {
		Sequence<Integer> sequence = ListSequence.withCapacity(1);
		sequence.addAll(asList(1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void createOfNone() {
		Sequence<Integer> sequence = ListSequence.createOf();
		twice(() -> assertThat(sequence, is(emptyIterable())));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(17)));
	}

	@Test
	public void createOfOne() {
		Sequence<Integer> sequence = ListSequence.createOf(1);
		twice(() -> assertThat(sequence, contains(1)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, 17)));
	}

	@Test
	public void createOfMany() {
		Sequence<Integer> sequence = ListSequence.createOf(1, 2, 3, 4, 5);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 17)));
	}

	@Test
	public void createOfNulls() {
		Sequence<Integer> sequence = ListSequence.createOf(1, null, 2, 3, null);
		twice(() -> assertThat(sequence, contains(1, null, 2, 3, null)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, null, 2, 3, null, 17)));
	}

	@Test
	public void concatArrayOfLists() {
		List<Integer> list1 = new ArrayList<>(asList(1, 2, 3));
		List<Integer> list2 = new ArrayList<>(asList(4, 5, 6));
		List<Integer> list3 = new ArrayList<>(asList(7, 8, 9));

		Sequence<Integer> sequence = ListSequence.concat(list1, list2, list3);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		list1.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 17, 4, 5, 6, 7, 8, 9)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void concatListOfLists() {
		List<Integer> list1 = new ArrayList<>(asList(1, 2, 3));
		List<Integer> list2 = new ArrayList<>(asList(4, 5, 6));
		List<Integer> list3 = new ArrayList<>(asList(7, 8, 9));
		List<List<Integer>> listList = new ArrayList<>(asList(list1, list2, list3));

		Sequence<Integer> sequence = ListSequence.concat(listList);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 17)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		listList.add(new ArrayList<>(asList(10, 11, 12)));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void reverse() {
		Sequence<Integer> emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyReversed.iterator().next());

		Sequence<Integer> reversed = sequence.reverse();
		twice(() -> assertThat(reversed, contains(5, 4, 3, 2, 1)));

		assertThat(removeFirst(reversed), is(5));
		twice(() -> assertThat(reversed, contains(4, 3, 2, 1)));
		twice(() -> assertThat(list, contains(1, 2, 3, 4)));
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
	public void testAsList() {
		assertThat(sequence.asList(), is(sameInstance(list)));
	}

	@Test
	public void filterOddsAdd() {
		odds.add(17);
		expecting(IllegalArgumentException.class, () -> odds.add(18));

		assertThat(odds, contains(1, 3, 5, 17));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 17));
		assertThat(list, contains(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void filterOddsAddAll() {
		assertThat(odds.addAll(asList(17, 19)), is(true));
		assertThat(odds, contains(1, 3, 5, 17, 19));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 17, 19));
		assertThat(list, contains(1, 2, 3, 4, 5, 17, 19));

		expecting(IllegalArgumentException.class, () -> odds.addAll(asList(21, 22)));
		assertThat(odds, contains(1, 3, 5, 17, 19, 21));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 17, 19, 21));
		assertThat(list, contains(1, 2, 3, 4, 5, 17, 19, 21));
	}

	@Test
	public void filterOddsRemove() {
		assertThat(odds.remove(3), is(true));
		assertThat(odds.remove(4), is(false));

		assertThat(odds, contains(1, 5));
		assertThat(sequence, contains(1, 2, 4, 5));
		assertThat(list, contains(1, 2, 4, 5));
	}

	@Test
	public void filterOddsContains() {
		assertThat(odds.contains(3), is(true));
		assertThat(odds.contains(4), is(false));
		assertThat(odds.contains(17), is(false));
	}

	@Test
	public void filterEvensAdd() {
		evens.add(18);
		expecting(IllegalArgumentException.class, () -> evens.add(17));

		assertThat(evens, contains(2, 4, 18));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 18));
		assertThat(list, contains(1, 2, 3, 4, 5, 18));
	}

	@Test
	public void filterEvensAddAll() {
		assertThat(evens.addAll(asList(18, 20)), is(true));
		assertThat(evens, contains(2, 4, 18, 20));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 18, 20));
		assertThat(list, contains(1, 2, 3, 4, 5, 18, 20));

		expecting(IllegalArgumentException.class, () -> evens.addAll(asList(22, 23)));
		assertThat(evens, contains(2, 4, 18, 20, 22));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 18, 20, 22));
		assertThat(list, contains(1, 2, 3, 4, 5, 18, 20, 22));
	}

	@Test
	public void filterEvensRemove() {
		assertThat(evens.remove(4), is(true));
		assertThat(evens.remove(3), is(false));

		assertThat(evens, contains(2));
		assertThat(sequence, contains(1, 2, 3, 5));
		assertThat(list, contains(1, 2, 3, 5));
	}

	@Test
	public void filterEvensContains() {
		assertThat(evens.contains(4), is(true));
		assertThat(evens.contains(3), is(false));
		assertThat(evens.contains(18), is(false));
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

	@Test
	public void biMapAddAll() {
		assertThat(strings.addAll(asList("6", "7")), is(true));
		assertThat(strings, contains("1", "2", "3", "4", "5", "6", "7"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7));
		assertThat(list, contains(1, 2, 3, 4, 5, 6, 7));

		expecting(NumberFormatException.class, () -> strings.addAll(asList("8", "foo")));
		assertThat(strings, contains("1", "2", "3", "4", "5", "6", "7", "8"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(list, contains(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void biMapRemove() {
		assertThat(strings.remove("3"), is(true));
		assertThat(strings.remove("17"), is(false));

		assertThat(strings, contains("1", "2", "4", "5"));
		assertThat(sequence, contains(1, 2, 4, 5));
		assertThat(list, contains(1, 2, 4, 5));
	}

	@Test
	public void biMapContains() {
		assertThat(strings.contains("3"), is(true));
		assertThat(strings.contains("17"), is(false));
	}

	@Test
	public void filterOddsBiMapAdd() {
		assertThat(oddStrings.add("7"), is(true));
		assertThat(oddStrings, contains("1", "3", "5", "7"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 7));
		assertThat(list, contains(1, 2, 3, 4, 5, 7));

		expecting(NumberFormatException.class, () -> oddStrings.add("foo"));
		assertThat(oddStrings, contains("1", "3", "5", "7"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 7));
		assertThat(list, contains(1, 2, 3, 4, 5, 7));
	}

	@Test
	public void filterOddsBiMapAddAll() {
		assertThat(oddStrings.addAll(asList("7", "9")), is(true));
		assertThat(oddStrings, contains("1", "3", "5", "7", "9"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 7, 9));
		assertThat(list, contains(1, 2, 3, 4, 5, 7, 9));

		expecting(NumberFormatException.class, () -> oddStrings.addAll(asList("11", "foo")));
		assertThat(oddStrings, contains("1", "3", "5", "7", "9", "11"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 7, 9, 11));
		assertThat(list, contains(1, 2, 3, 4, 5, 7, 9, 11));
	}

	@Test
	public void filterOddsBiMapRemove() {
		assertThat(oddStrings.remove("3"), is(true));
		assertThat(oddStrings.remove("17"), is(false));

		assertThat(oddStrings, contains("1", "5"));
		assertThat(sequence, contains(1, 2, 4, 5));
		assertThat(list, contains(1, 2, 4, 5));
	}

	@Test
	public void filterOddsBiMapContains() {
		assertThat(oddStrings.contains("3"), is(true));
		assertThat(oddStrings.contains("4"), is(false));
		assertThat(oddStrings.contains("17"), is(false));
	}

	@Test
	public void filterEvensBiMapAdd() {
		assertThat(evenStrings.add("6"), is(true));
		assertThat(evenStrings, contains("2", "4", "6"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6));
		assertThat(list, contains(1, 2, 3, 4, 5, 6));

		expecting(NumberFormatException.class, () -> evenStrings.add("foo"));
		assertThat(evenStrings, contains("2", "4", "6"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6));
		assertThat(list, contains(1, 2, 3, 4, 5, 6));
	}

	@Test
	public void filterEvensBiMapAddAll() {
		assertThat(evenStrings.addAll(asList("6", "8")), is(true));
		assertThat(evenStrings, contains("2", "4", "6", "8"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 8));
		assertThat(list, contains(1, 2, 3, 4, 5, 6, 8));

		expecting(NumberFormatException.class, () -> evenStrings.addAll(asList("10", "foo")));
		assertThat(evenStrings, contains("2", "4", "6", "8", "10"));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 8, 10));
		assertThat(list, contains(1, 2, 3, 4, 5, 6, 8, 10));
	}

	@Test
	public void filterEvensBiMapRemove() {
		assertThat(evenStrings.remove("4"), is(true));
		assertThat(evenStrings.remove("18"), is(false));

		assertThat(evenStrings, contains("2"));
		assertThat(sequence, contains(1, 2, 3, 5));
		assertThat(list, contains(1, 2, 3, 5));
	}

	@Test
	public void filterEvensBiMapContains() {
		assertThat(evenStrings.contains("4"), is(true));
		assertThat(evenStrings.contains("3"), is(false));
		assertThat(evenStrings.contains("18"), is(false));
	}
}
