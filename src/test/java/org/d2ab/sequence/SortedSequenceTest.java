package org.d2ab.sequence;

import org.d2ab.util.Pair;
import org.junit.Test;

import java.util.Optional;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static org.d2ab.test.HasSizeCharacteristics.containsSized;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SortedSequenceTest {
	private final Sequence<Pair<Integer, String>> backingDuplicates = Sequence.createOf(Pair.of(1, "a"), Pair.of(2, "a"), Pair.of(3, "a"),
	                                                                                    Pair.of(1, "b"), Pair.of(3, "b"), Pair.of(1, "c"));
	private final SortedSequence<Pair<Integer, String>> duplicatesLeftNatural = new SortedSequence<>(backingDuplicates, comparing(Pair::getLeft));
	private final SortedSequence<Pair<Integer, String>> duplicatesLeftReverse = new SortedSequence<>(backingDuplicates, reverseOrder(comparing(Pair::getLeft)));

	private final SortedSequence<Pair<Integer, String>> duplicatesNatural = new SortedSequence<>(backingDuplicates, naturalOrder());
	private final SortedSequence<Pair<Integer, String>> duplicatesReverse = new SortedSequence<>(backingDuplicates, reverseOrder());

	private final SortedSequence<Number> mixed = new SortedSequence<>(Sequence.of(3, 2, 1, 3.0, 2.0, 1.0), comparing(Number::intValue));

	@Test
	public void filter() {
		assertThat(duplicatesLeftNatural.filter(p -> p.getLeft() > 1), contains(Pair.of(2, "a"), Pair.of(3, "a"), Pair.of(3, "b")));
		assertThat(duplicatesLeftReverse.filter(p -> p.getLeft() > 1), contains(Pair.of(3, "a"), Pair.of(3, "b"), Pair.of(2, "a")));
		assertThat(duplicatesNatural.filter(p -> p.getLeft() > 1), contains(Pair.of(2, "a"), Pair.of(3, "a"), Pair.of(3, "b")));
		assertThat(duplicatesReverse.filter(p -> p.getLeft() > 1), contains(Pair.of(3, "b"), Pair.of(3, "a"), Pair.of(2, "a")));
	}

	@Test
	public void filterByClass() {
		assertThat(mixed.filter(Integer.class), contains(1, 2, 3));
		assertThat(mixed.filter(Double.class), contains(1.0, 2.0, 3.0));
	}

	@Test
	public void minNormal() {
		assertThat(duplicatesLeftNatural.min(), is(Optional.of(Pair.of(1, "a"))));
		assertThat(duplicatesLeftReverse.min(), is(Optional.of(Pair.of(1, "a"))));
		assertThat(duplicatesNatural.min(), is(Optional.of(Pair.of(1, "a"))));
		assertThat(duplicatesReverse.min(), is(Optional.of(Pair.of(1, "a"))));
	}

	@Test
	public void maxNormal() {
		assertThat(duplicatesLeftNatural.max(), is(Optional.of(Pair.of(3, "b"))));
		assertThat(duplicatesLeftReverse.max(), is(Optional.of(Pair.of(3, "b"))));
		assertThat(duplicatesNatural.max(), is(Optional.of(Pair.of(3, "b"))));
		assertThat(duplicatesReverse.max(), is(Optional.of(Pair.of(3, "b"))));
	}

	@Test
	public void minLeft() {
		assertThat(duplicatesLeftNatural.min(comparing(Pair::getLeft)), is(Optional.of(Pair.of(1, "a"))));
		assertThat(duplicatesLeftReverse.min(comparing(Pair::getLeft)), is(Optional.of(Pair.of(1, "a"))));
		assertThat(duplicatesNatural.min(comparing(Pair::getLeft)), is(Optional.of(Pair.of(1, "a"))));
		assertThat(duplicatesReverse.min(comparing(Pair::getLeft)), is(Optional.of(Pair.of(1, "c"))));
	}

	@Test
	public void maxLeft() {
		assertThat(duplicatesLeftNatural.max(comparing(Pair::getLeft)), is(Optional.of(Pair.of(3, "a"))));
		assertThat(duplicatesLeftReverse.max(comparing(Pair::getLeft)), is(Optional.of(Pair.of(3, "a"))));
		assertThat(duplicatesNatural.max(comparing(Pair::getLeft)), is(Optional.of(Pair.of(3, "b"))));
		assertThat(duplicatesReverse.max(comparing(Pair::getLeft)), is(Optional.of(Pair.of(3, "a"))));
	}

	@Test
	public void firstStableSort() {
		assertThat(duplicatesLeftNatural.first(), is(Optional.of(Pair.of(1, "a"))));
	}

	@Test
	public void lastStableSort() {
		assertThat(duplicatesLeftNatural.last(), is(Optional.of(Pair.of(3, "b"))));
	}

	@Test
	public void reversed() {
		assertThat(duplicatesLeftNatural.reverse(), containsSized(Pair.of(3, "b"), Pair.of(3, "a"),
		                                                          Pair.of(2, "a"), Pair.of(1, "c"),
		                                                          Pair.of(1, "b"), Pair.of(1, "a")));
	}
}