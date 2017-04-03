package org.d2ab.sequence;

import org.d2ab.util.Pair;
import org.junit.Test;

import java.util.Optional;

import static java.util.Comparator.comparing;
import static org.d2ab.test.HasSizeCharacteristics.containsSized;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SortedSequenceTest {
	private final SortedSequence<Pair<Integer, String>> duplicates = new SortedSequence<>(
			Sequence.createOf(Pair.of(1, "a"), Pair.of(2, "a"), Pair.of(3, "a"),
			                  Pair.of(1, "b"), Pair.of(3, "b"), Pair.of(1, "c")),
			comparing(Pair::getLeft));

	@Test
	public void firstStableSort() {
		assertThat(duplicates.first(), is(Optional.of(Pair.of(1, "a"))));
	}

	@Test
	public void lastStableSort() {
		assertThat(duplicates.last(), is(Optional.of(Pair.of(3, "b"))));
	}

	@Test
	public void reversed() {
		assertThat(duplicates.reverse(), containsSized(Pair.of(3, "b"), Pair.of(3, "a"),
		                                               Pair.of(2, "a"), Pair.of(1, "c"),
		                                               Pair.of(1, "b"), Pair.of(1, "a")));
	}
}