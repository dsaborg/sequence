package org.d2ab.sequence;

import org.junit.Test;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import static org.d2ab.test.HasSizeCharacteristics.containsSized;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class StableShuffledSequenceTest {
	private static final Supplier<Random> RANDOM_SUPPLIER = () -> new Random(17);

	private final Sequence<Number> backing = Sequence.createOf(1.0, 2.0, 3, 4, 5);
	private final Sequence<Number> _12345 = new StableShuffledSequence<>(backing, RANDOM_SUPPLIER);
	private final Number[] expectedItemOrder = {2.0, 5, 4, 1.0, 3};

	@Test
	public void iteration() {
		twice(() -> assertThat(_12345, containsSized(expectedItemOrder)));
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(_12345.toArray(), is(arrayContaining(expectedItemOrder))));
	}

	@Test
	public void toArrayWithConstructor() {
		twice(() -> assertThat(_12345.toArray(Number[]::new), is(arrayContaining(expectedItemOrder))));
	}

	@Test
	public void toList() {
		twice(() -> assertThat(_12345.toList(), contains(expectedItemOrder)));
	}

	@Test
	public void toListWithConstructor() {
		twice(() -> assertThat(_12345.toList(LinkedList::new), contains(expectedItemOrder)));
	}

	@Test
	public void arbitrary() {
		twice(() -> assertThat(_12345.arbitrary(), is(Optional.of(1.0))));
	}

	@Test
	public void first() {
		twice(() -> assertThat(_12345.first(), is(Optional.of(expectedItemOrder[0]))));
	}

	@Test
	public void last() {
		twice(() -> assertThat(_12345.last(), is(Optional.of(expectedItemOrder[4]))));
	}

	@Test
	public void at() {
		twice(() -> assertThat(_12345.at(0), is(Optional.of(expectedItemOrder[0]))));
		twice(() -> assertThat(_12345.at(2), is(Optional.of(expectedItemOrder[2]))));
		twice(() -> assertThat(_12345.at(4), is(Optional.of(expectedItemOrder[4]))));
	}

	@Test
	public void arbitraryByPredicate() {
		twice(() -> assertThat(_12345.arbitrary(x -> x.intValue() > 2), is(Optional.of(3))));
	}

	@Test
	public void firstByPredicate() {
		twice(() -> assertThat(_12345.first(x -> x.intValue() > 2), is(Optional.of(5))));
	}

	@Test
	public void lastByPredicate() {
		twice(() -> assertThat(_12345.last(x -> x.intValue() > 2), is(Optional.of(3))));
	}

	@Test
	public void atByPredicate() {
		twice(() -> assertThat(_12345.at(0, x -> x.intValue() > 2), is(Optional.of(5))));
		twice(() -> assertThat(_12345.at(1, x -> x.intValue() > 2), is(Optional.of(4))));
		twice(() -> assertThat(_12345.at(2, x -> x.intValue() > 2), is(Optional.of(3))));
	}

	@Test
	public void arbitraryByClass() {
		twice(() -> assertThat(_12345.arbitrary(Integer.class), is(Optional.of(3))));
	}

	@Test
	public void firstByClass() {
		twice(() -> assertThat(_12345.first(Integer.class), is(Optional.of(5))));
	}

	@Test
	public void lastByClass() {
		twice(() -> assertThat(_12345.last(Integer.class), is(Optional.of(3))));
	}

	@Test
	public void atByClass() {
		twice(() -> assertThat(_12345.at(0, Integer.class), is(Optional.of(5))));
		twice(() -> assertThat(_12345.at(1, Integer.class), is(Optional.of(4))));
		twice(() -> assertThat(_12345.at(2, Integer.class), is(Optional.of(3))));
	}
}