package org.d2ab.sequence;

import org.junit.Test;

import java.util.LinkedList;
import java.util.Optional;

import static org.d2ab.test.HasSizeCharacteristics.containsSized;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ReverseSequenceTest {
	private final Sequence<Number> _12345 = new ReverseSequence<>(Sequence.createOf(1.0, 2.0, 3, 4, 5));

	@Test
	public void iteration() {
		twice(() -> assertThat(_12345, containsSized(5, 4, 3, 2.0, 1.0)));
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(_12345.toArray(), is(arrayContaining(5, 4, 3, 2.0, 1.0))));
	}

	@Test
	public void toArrayWithConstructor() {
		twice(() -> assertThat(_12345.toArray(Number[]::new), is(arrayContaining(5, 4, 3, 2.0, 1.0))));
	}

	@Test
	public void toList() {
		twice(() -> assertThat(_12345.toList(), contains(5, 4, 3, 2.0, 1.0)));
	}

	@Test
	public void toListWithConstructor() {
		twice(() -> assertThat(_12345.toList(LinkedList::new), contains(5, 4, 3, 2.0, 1.0)));
	}

	@Test
	public void arbitrary() {
		twice(() -> assertThat(_12345.arbitrary(), is(Optional.of(1.0))));
	}

	@Test
	public void first() {
		assertThat(_12345.first(), is(Optional.of(5)));
	}

	@Test
	public void last() {
		assertThat(_12345.last(), is(Optional.of(1.0)));
	}

	@Test
	public void at() {
		assertThat(_12345.at(0), is(Optional.of(5)));
		assertThat(_12345.at(2), is(Optional.of(3)));
		assertThat(_12345.at(4), is(Optional.of(1.0)));
	}

	@Test
	public void arbitraryByPredicate() {
		twice(() -> assertThat(_12345.arbitrary(x -> x.intValue() > 2), is(Optional.of(3))));
	}

	@Test
	public void firstByPredicate() {
		assertThat(_12345.first(x -> x.intValue() > 2), is(Optional.of(5)));
	}

	@Test
	public void lastByPredicate() {
		assertThat(_12345.last(x -> x.intValue() > 2), is(Optional.of(3)));
	}

	@Test
	public void atByPredicate() {
		assertThat(_12345.at(0, x -> x.intValue() > 2), is(Optional.of(5)));
		assertThat(_12345.at(1, x -> x.intValue() > 2), is(Optional.of(4)));
		assertThat(_12345.at(2, x -> x.intValue() > 2), is(Optional.of(3)));
	}

	@Test
	public void arbitraryByClass() {
		twice(() -> assertThat(_12345.arbitrary(Integer.class), is(Optional.of(3))));
	}

	@Test
	public void firstByClass() {
		assertThat(_12345.first(Integer.class), is(Optional.of(5)));
	}

	@Test
	public void lastByClass() {
		assertThat(_12345.last(Integer.class), is(Optional.of(3)));
	}

	@Test
	public void atByClass() {
		assertThat(_12345.at(0, Integer.class), is(Optional.of(5)));
		assertThat(_12345.at(1, Integer.class), is(Optional.of(4)));
		assertThat(_12345.at(2, Integer.class), is(Optional.of(3)));
	}
}