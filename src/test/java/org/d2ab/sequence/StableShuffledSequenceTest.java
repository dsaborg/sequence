package org.d2ab.sequence;

import org.junit.Test;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import static java.util.Comparator.comparing;
import static org.d2ab.test.HasSizeCharacteristics.containsSized;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class StableShuffledSequenceTest {
	private static final Supplier<Random> FIXED_RANDOM_SUPPLIER = () -> new Random(17);

	private final Sequence<Number> backing = Sequence.createOf(1.0, 2.0, 1, 2, 3, 4, 5);
	private final Sequence<Number> digits = new StableShuffledSequence<>(backing, FIXED_RANDOM_SUPPLIER);
	private final Number[] expectedItemOrder = {3, 2.0, 5, 4, 2, 1.0, 1};

	@Test
	public void iteration() {
		twice(() -> assertThat(digits, containsSized(expectedItemOrder)));
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(digits.toArray(), is(arrayContaining(expectedItemOrder))));
	}

	@Test
	public void toArrayWithConstructor() {
		twice(() -> assertThat(digits.toArray(Number[]::new), is(arrayContaining(expectedItemOrder))));
	}

	@Test
	public void toList() {
		twice(() -> assertThat(digits.toList(), contains(expectedItemOrder)));
	}

	@Test
	public void toListWithConstructor() {
		twice(() -> assertThat(digits.toList(LinkedList::new), contains(expectedItemOrder)));
	}

	@Test
	public void min() {
		twice(() -> assertThat(digits.min(comparing(Number::intValue)), is(Optional.of(1.0))));
	}

	@Test
	public void max() {
		twice(() -> assertThat(digits.max(comparing(Number::intValue)), is(Optional.of(5))));
	}

	@Test
	public void arbitrary() {
		twice(() -> assertThat(digits.arbitrary(), is(Optional.of(1.0))));
	}

	@Test
	public void first() {
		twice(() -> assertThat(digits.first(), is(Optional.of(expectedItemOrder[0]))));
	}

	@Test
	public void last() {
		twice(() -> assertThat(digits.last(), is(Optional.of(expectedItemOrder[6]))));
	}

	@Test
	public void at() {
		twice(() -> assertThat(digits.at(0), is(Optional.of(expectedItemOrder[0]))));
		twice(() -> assertThat(digits.at(2), is(Optional.of(expectedItemOrder[2]))));
		twice(() -> assertThat(digits.at(4), is(Optional.of(expectedItemOrder[4]))));
		twice(() -> assertThat(digits.at(6), is(Optional.of(expectedItemOrder[6]))));
		twice(() -> assertThat(digits.at(7), is(Optional.empty())));
	}

	@Test
	public void arbitraryByPredicate() {
		twice(() -> assertThat(digits.arbitrary(x -> x.intValue() > 2), is(Optional.of(3))));
	}

	@Test
	public void firstByPredicate() {
		twice(() -> assertThat(digits.first(x -> x.intValue() > 2), is(Optional.of(3))));
	}

	@Test
	public void lastByPredicate() {
		twice(() -> assertThat(digits.last(x -> x.intValue() > 2), is(Optional.of(4))));
	}

	@Test
	public void atByPredicate() {
		twice(() -> assertThat(digits.at(0, x -> x.intValue() > 2), is(Optional.of(3))));
		twice(() -> assertThat(digits.at(1, x -> x.intValue() > 2), is(Optional.of(5))));
		twice(() -> assertThat(digits.at(2, x -> x.intValue() > 2), is(Optional.of(4))));
	}

	@Test
	public void arbitraryByClass() {
		twice(() -> assertThat(digits.arbitrary(Integer.class), is(Optional.of(1))));
		twice(() -> assertThat(digits.arbitrary(Double.class), is(Optional.of(1.0))));
	}

	@Test
	public void firstByClass() {
		twice(() -> assertThat(digits.first(Integer.class), is(Optional.of(3))));
		twice(() -> assertThat(digits.first(Double.class), is(Optional.of(2.0))));
	}

	@Test
	public void lastByClass() {
		twice(() -> assertThat(digits.last(Integer.class), is(Optional.of(1))));
		twice(() -> assertThat(digits.last(Double.class), is(Optional.of(1.0))));
	}

	@Test
	public void atByClass() {
		twice(() -> assertThat(digits.at(0, Integer.class), is(Optional.of(3))));
		twice(() -> assertThat(digits.at(1, Integer.class), is(Optional.of(5))));
		twice(() -> assertThat(digits.at(2, Integer.class), is(Optional.of(4))));

		twice(() -> assertThat(digits.at(0, Double.class), is(Optional.of(2.0))));
		twice(() -> assertThat(digits.at(1, Double.class), is(Optional.of(1.0))));
		twice(() -> assertThat(digits.at(2, Double.class), is(Optional.empty())));
	}
}