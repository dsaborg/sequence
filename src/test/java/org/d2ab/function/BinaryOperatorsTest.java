package org.d2ab.function;

import org.junit.Test;

import java.util.function.BinaryOperator;

import static java.util.Comparator.comparing;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;

public class BinaryOperatorsTest {
	@SuppressWarnings("UnnecessaryBoxing")
	private static final Integer FIRST = new Integer(1);
	@SuppressWarnings("UnnecessaryBoxing")
	private static final Integer LAST = new Integer(1);

	@Test
	public void constructor() {
		new BinaryOperators();
	}

	@Test
	public void firstMinByNullComparator() {
		BinaryOperator<Integer> firstMinBy = BinaryOperators.firstMinBy(null);
		assertThat(firstMinBy.apply(FIRST, LAST), is(sameInstance(FIRST)));
		assertThat(firstMinBy.apply(1, 2), is(1));
		assertThat(firstMinBy.apply(2, 1), is(1));
	}

	@Test
	public void firstMinByIntValueComparator() {
		BinaryOperator<Number> firstMinBy = BinaryOperators.firstMinBy(comparing(Number::intValue));
		assertThat(firstMinBy.apply(1, 1.0), is(1));
		assertThat(firstMinBy.apply(1.0, 1), is(1.0));
		assertThat(firstMinBy.apply(1, 2), is(1));
		assertThat(firstMinBy.apply(2, 1), is(1));
	}

	@Test
	public void firstMaxByNullComparator() {
		BinaryOperator<Integer> firstMaxBy = BinaryOperators.firstMaxBy(null);
		assertThat(firstMaxBy.apply(FIRST, LAST), is(sameInstance(FIRST)));
		assertThat(firstMaxBy.apply(1, 2), is(2));
		assertThat(firstMaxBy.apply(2, 1), is(2));
	}

	@Test
	public void firstMaxByIntValueComparator() {
		BinaryOperator<Number> firstMaxBy = BinaryOperators.firstMaxBy(comparing(Number::intValue));
		assertThat(firstMaxBy.apply(1, 1.0), is(1));
		assertThat(firstMaxBy.apply(1.0, 1), is(1.0));
		assertThat(firstMaxBy.apply(1, 2), is(2));
		assertThat(firstMaxBy.apply(2, 1), is(2));
	}

	@Test
	public void lastMinByNullComparator() {
		BinaryOperator<Integer> lastMinBy = BinaryOperators.lastMinBy(null);
		assertThat(lastMinBy.apply(FIRST, LAST), is(sameInstance(LAST)));
		assertThat(lastMinBy.apply(1, 2), is(1));
		assertThat(lastMinBy.apply(2, 1), is(1));
	}

	@Test
	public void lastMinByIntValueComparator() {
		BinaryOperator<Number> lastMinBy = BinaryOperators.lastMinBy(comparing(Number::intValue));
		assertThat(lastMinBy.apply(1, 1.0), is(1.0));
		assertThat(lastMinBy.apply(1.0, 1), is(1));
		assertThat(lastMinBy.apply(1, 2), is(1));
		assertThat(lastMinBy.apply(2, 1), is(1));
	}

	@Test
	public void lastMaxByNullComparator() {
		BinaryOperator<Integer> lastMaxBy = BinaryOperators.lastMaxBy(null);
		assertThat(lastMaxBy.apply(FIRST, LAST), is(sameInstance(LAST)));
		assertThat(lastMaxBy.apply(1, 2), is(2));
		assertThat(lastMaxBy.apply(2, 1), is(2));
	}

	@Test
	public void lastMaxByIntValueComparator() {
		BinaryOperator<Number> lastMaxBy = BinaryOperators.lastMaxBy(comparing(Number::intValue));
		assertThat(lastMaxBy.apply(1, 1.0), is(1.0));
		assertThat(lastMaxBy.apply(1.0, 1), is(1));
		assertThat(lastMaxBy.apply(1, 2), is(2));
		assertThat(lastMaxBy.apply(2, 1), is(2));
	}
}