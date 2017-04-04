package org.d2ab.sequence;

import org.junit.Test;

import java.util.LinkedList;
import java.util.Optional;

import static org.d2ab.test.HasSizeCharacteristics.containsSized;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ReverseSequenceTest {
	private final Sequence<Number> backing = Sequence.createOf(1.0, 2.0, 3, 4, 5);
	private final Sequence<Number> _12345 = new ReverseSequence<>(backing);

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
		twice(() -> assertThat(_12345.first(), is(Optional.of(5))));
	}

	@Test
	public void last() {
		twice(() -> assertThat(_12345.last(), is(Optional.of(1.0))));
	}

	@Test
	public void at() {
		twice(() -> assertThat(_12345.at(0), is(Optional.of(5))));
		twice(() -> assertThat(_12345.at(2), is(Optional.of(3))));
		twice(() -> assertThat(_12345.at(4), is(Optional.of(1.0))));
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

	@Test
	public void removeArbitrary() {
		assertThat(_12345.removeArbitrary(), is(Optional.of(1.0)));
		twice(() -> assertThat(backing, containsSized(2.0, 3, 4, 5)));
	}

	@Test
	public void removeFirst() {
		assertThat(_12345.removeFirst(), is(Optional.of(5)));
		twice(() -> assertThat(backing, containsSized(1.0, 2.0, 3, 4)));
	}

	@Test
	public void removeLast() {
		assertThat(_12345.removeLast(), is(Optional.of(1.0)));
		twice(() -> assertThat(backing, containsSized(2.0, 3, 4, 5)));
	}

	@Test
	public void removeAt() {
		assertThat(_12345.removeAt(0), is(Optional.of(5)));
		assertThat(_12345.removeAt(1), is(Optional.of(3)));
		assertThat(_12345.removeAt(2), is(Optional.of(1.0)));
		twice(() -> assertThat(backing, containsSized(2.0, 4)));
	}

	@Test
	public void removeArbitraryByPredicate() {
		assertThat(_12345.removeArbitrary(x -> x.intValue() > 2), is(Optional.of(3)));
		twice(() -> assertThat(backing, containsSized(1.0, 2.0, 4, 5)));
	}

	@Test
	public void removeFirstByPredicate() {
		assertThat(_12345.removeFirst(x -> x.intValue() > 2), is(Optional.of(5)));
		twice(() -> assertThat(backing, containsSized(1.0, 2.0, 3, 4)));
	}

	@Test
	public void removeLastByPredicate() {
		assertThat(_12345.removeLast(x -> x.intValue() > 2), is(Optional.of(3)));
		twice(() -> assertThat(backing, containsSized(1.0, 2.0, 4, 5)));
	}

	@Test
	public void removeArbitraryByClass() {
		assertThat(_12345.removeArbitrary(Integer.class), is(Optional.of(3)));
		twice(() -> assertThat(backing, containsSized(1.0, 2.0, 4, 5)));
	}

	@Test
	public void removeFirstByClass() {
		assertThat(_12345.removeFirst(Integer.class), is(Optional.of(5)));
		twice(() -> assertThat(backing, containsSized(1.0, 2.0, 3, 4)));
	}

	@Test
	public void removeLastByClass() {
		assertThat(_12345.removeLast(Integer.class), is(Optional.of(3)));
		twice(() -> assertThat(backing, containsSized(1.0, 2.0, 4, 5)));
	}
}