package org.d2ab.sequence;

import org.d2ab.iterator.Iterators;
import org.junit.Test;

import java.util.*;

import static java.util.Comparator.comparing;
import static org.d2ab.test.Tests.times;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ShuffledSequenceTest {
	private final Sequence<Number> empty = new ShuffledSequence<>(Sequence.createOf(), new Random(17));

	private final Sequence<Number> singleton = new ShuffledSequence<>(Sequence.createOf(1.0), new Random(17));

	private final Sequence<Number> two = new ShuffledSequence<>(Sequence.createOf(1.0, 2.0), new Random(17));

	private final Number[] smallElements = {1.0, 2f, 3, 4L};
	private final Sequence<Number> small = new ShuffledSequence<>(Sequence.createOf(smallElements), new Random(17));

	private final Number[] duplicateElements = {1.0, 2.0, 3f, 4f, 5f, 1, 2, 3, 4, 5, 1L, 5L};
	private final Sequence<Number> duplicates = new ShuffledSequence<>(Sequence.createOf(duplicateElements), new Random(17));

	@Test
	public void filter() {
		assertThat(duplicates.filter(x -> x.intValue() > 1), containsInAnyOrder(2.0, 3f, 4f, 5f, 2, 3, 4, 5, 5L));
		assertThat(small.filter(x -> x.intValue() > 1), containsInAnyOrder(2f, 3, 4L));
	}

	@Test
	public void filterByClass() {
		assertThat(small.filter(Integer.class), contains(3));
		assertThat(small.filter(Double.class), contains(1.0));
	}

	@Test
	public void iteration() {
		for (int i = 0; i < 10; i++) {
			assertThat(small, containsInAnyOrder(smallElements));
			assertThat(duplicates, containsInAnyOrder(duplicateElements));
		}
	}

	@Test
	public void iterationRandomCoverage() {
		Set<Integer> permutations = new HashSet<>();

		for (int i = 0; i < 100; i++)
			permutations.add(Iterators.toList(small.iterator()).hashCode());

		assertThat(permutations.size(), is(24));
	}

	@Test
	public void size() {
		for (int i = 0; i < 10; i++) {
			assertThat(small.size(), is(4));
			assertThat(duplicates.size(), is(12));
		}
	}

	@Test
	public void toList() {
		for (int i = 0; i < 100; i++)
			assertThat(small.toList(), containsInAnyOrder(smallElements));
	}

	@Test
	public void toListRandomCoverage() {
		Set<Integer> permutations = new HashSet<>();

		for (int i = 0; i < 100; i++)
			permutations.add(small.toList().hashCode());

		assertThat(permutations.size(), is(24));
	}

	@Test
	public void toListWithConstructor() {
		for (int i = 0; i < 100; i++)
			assertThat(small.toList(LinkedList::new), containsInAnyOrder(smallElements));
	}

	@Test
	public void toListWithConstructorRandomCoverage() {
		Set<Integer> permutations = new HashSet<>();

		for (int i = 0; i < 100; i++)
			permutations.add(small.toList(LinkedList::new).hashCode());

		assertThat(permutations.size(), is(24));
	}

	@Test
	public void toArray() {
		times(10, () -> assertThat(small.toArray(), is(arrayContainingInAnyOrder(smallElements))));
		times(10, () -> assertThat(duplicates.toArray(), is(arrayContainingInAnyOrder(duplicateElements))));
	}

	@Test
	public void toArrayRandomCoverage() {
		Set<Integer> permutations = new HashSet<>();

		for (int i = 0; i < 100; i++)
			permutations.add(Arrays.hashCode(small.toArray()));

		assertThat(permutations.size(), is(24));
	}

	@Test
	public void toArrayWithConstructor() {
		times(10, () -> assertThat(small.toArray(Number[]::new), is(arrayContainingInAnyOrder(smallElements))));
		times(10, () -> assertThat(duplicates.toArray(Number[]::new), is(arrayContainingInAnyOrder(duplicateElements))));
	}

	@Test
	public void toArrayWithConstructorRandomCoverage() {
		Set<Integer> permutations = new HashSet<>();

		for (int i = 0; i < 100; i++)
			permutations.add(Arrays.hashCode(small.toArray(Number[]::new)));

		assertThat(permutations.size(), is(24));
	}

	@Test
	public void min() {
		Set<Number> mins = new HashSet<>();

		for (int i = 0; i < 10; i++)
			mins.add(duplicates.min(comparing(Number::intValue)).get());

		assertThat(mins, containsInAnyOrder(1.0, 1, 1L));

		assertThat(empty.min(comparing(Number::intValue)), is(Optional.empty()));
		assertThat(singleton.min(comparing(Number::intValue)), is(Optional.of(1.0)));
		assertThat(two.min(comparing(Number::intValue)), is(Optional.of(1.0)));
	}

	@Test
	public void max() {
		Set<Number> maxes = new HashSet<>();

		for (int i = 0; i < 10; i++)
			maxes.add(duplicates.max(comparing(Number::intValue)).get());

		assertThat(maxes, containsInAnyOrder(5f, 5, 5L));

		assertThat(empty.max(comparing(Number::intValue)), is(Optional.empty()));
		assertThat(singleton.max(comparing(Number::intValue)), is(Optional.of(1.0)));
		assertThat(two.max(comparing(Number::intValue)), is(Optional.of(2.0)));
	}

	@Test
	public void first() {
		Set<Number> firsts = new LinkedHashSet<>();

		for (int i = 0; i < 10; i++)
			firsts.add(small.first().get());

		assertThat(firsts, containsInAnyOrder(smallElements));
		assertThat(firsts, not(contains(smallElements))); // check random ordering
	}

	@Test
	public void last() {
		Set<Number> lasts = new LinkedHashSet<>();

		for (int i = 0; i < 10; i++)
			lasts.add(small.last().get());

		assertThat(lasts, containsInAnyOrder(smallElements));
		assertThat(lasts, not(contains(smallElements))); // check random ordering
	}

	@Test
	public void at() {
		Set<Number> ats = new LinkedHashSet<>();

		for (int i = 0; i < 10; i++)
			ats.add(small.at(2).get());

		assertThat(ats, containsInAnyOrder(smallElements));
		assertThat(ats, not(contains(smallElements))); // check random ordering
	}

	@Test
	public void removeFirst() {
		Set<Number> firsts = new LinkedHashSet<>();

		for (int i = 0; i < 4; i++)
			firsts.add(small.removeFirst().get());

		assertThat(small, is(emptyIterable()));
		assertThat(firsts, containsInAnyOrder(smallElements));
		assertThat(firsts, not(contains(smallElements))); // check random ordering
	}

	@Test
	public void removeLast() {
		Set<Number> lasts = new LinkedHashSet<>();

		for (int i = 0; i < 4; i++)
			lasts.add(small.removeLast().get());

		assertThat(small, is(emptyIterable()));
		assertThat(lasts, containsInAnyOrder(smallElements));
		assertThat(lasts, not(contains(smallElements))); // check random ordering
	}

	@Test
	public void removeAt() {
		Set<Number> ats = new LinkedHashSet<>();

		twice(() -> ats.add(small.removeAt(2).get()));
		twice(() -> ats.add(small.removeAt(0).get()));

		assertThat(ats, containsInAnyOrder(smallElements));
		assertThat(ats, not(contains(smallElements))); // check random ordering
	}

	@Test
	public void atOutOfBounds() {
		times(10, () -> assertThat(small.at(4), is(Optional.empty())));
	}
}