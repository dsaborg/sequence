package org.d2ab.function;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FunctionalInterfacesTest {
	@Test
	public void charPredicate() {
		CharPredicate geB = c -> c >= 'b';

		assertThat(geB.negate().test('a'), is(true));
		assertThat(geB.negate().test('b'), is(false));
		assertThat(geB.negate().test('c'), is(false));

		CharPredicate eqB = c -> c == 'b';
		assertThat(geB.and(eqB).test('a'), is(false));
		assertThat(geB.and(eqB).test('b'), is(true));
		assertThat(geB.and(eqB).test('c'), is(false));

		CharPredicate ltB = c -> c < 'b';
		assertThat(ltB.or(eqB).test('a'), is(true));
		assertThat(ltB.or(eqB).test('b'), is(true));
		assertThat(ltB.or(eqB).test('c'), is(false));
	}

	@Test
	public void charBiPredicate() {
		CharBiPredicate le = (x1, x2) -> x1 <= x2;

		assertThat(le.negate().test('a', 'a'), is(false));
		assertThat(le.negate().test('b', 'a'), is(true));
		assertThat(le.negate().test('a', 'b'), is(false));

		CharBiPredicate eq = (x1, x2) -> x1 == x2;
		assertThat(le.and(eq).test('a', 'a'), is(true));
		assertThat(le.and(eq).test('a', 'b'), is(false));
		assertThat(le.and(eq).test('b', 'a'), is(false));

		CharBiPredicate lt = (x1, x2) -> x1 < x2;
		assertThat(lt.or(eq).test('a', 'a'), is(true));
		assertThat(lt.or(eq).test('a', 'b'), is(true));
		assertThat(lt.or(eq).test('b', 'a'), is(false));
	}

	@Test
	public void charConsumer() {
		StringBuilder builder = new StringBuilder();
		CharConsumer first = c -> builder.append("First:" + c);
		first.andThen(c -> builder.append("AndThen:" + c)).accept('q');
		assertThat(builder.toString(), is("First:qAndThen:q"));
	}

	@Test
	public void charUnaryOperator() {
		CharUnaryOperator identity = CharUnaryOperator.identity();
		assertThat(identity.applyAsChar('a'), is('a'));
		assertThat(identity.applyAsChar('b'), is('b'));

		CharUnaryOperator first = c -> (char) (c + 1);
		CharUnaryOperator then = first.andThen(c -> {
			assertThat(c, is('b'));
			return (char) (c + 2);
		});
		assertThat(then.applyAsChar('a'), is('d'));

		CharUnaryOperator compose = c -> {
			assertThat(c, is('b'));
			return (char) (c + 2);
		};
		CharUnaryOperator with = compose.compose(c -> (char) (c + 1));
		assertThat(with.applyAsChar('a'), is('d'));
	}

	@Test
	public void doubleBiPredicate() {
		DoubleBiPredicate le = (x1, x2) -> x1 <= x2;

		assertThat(le.negate().test(1, 1), is(false));
		assertThat(le.negate().test(2, 1), is(true));
		assertThat(le.negate().test(1, 2), is(false));

		DoubleBiPredicate eq = (x1, x2) -> x1 == x2;
		assertThat(le.and(eq).test(1, 1), is(true));
		assertThat(le.and(eq).test(1, 2), is(false));
		assertThat(le.and(eq).test(2, 1), is(false));

		DoubleBiPredicate lt = (x1, x2) -> x1 < x2;
		assertThat(lt.or(eq).test(1, 1), is(true));
		assertThat(lt.or(eq).test(1, 2), is(true));
		assertThat(lt.or(eq).test(2, 1), is(false));
	}

	@Test
	public void intBiPredicate() {
		IntBiPredicate le = (x1, x2) -> x1 <= x2;

		assertThat(le.negate().test(1, 1), is(false));
		assertThat(le.negate().test(2, 1), is(true));
		assertThat(le.negate().test(1, 2), is(false));

		IntBiPredicate eq = (x1, x2) -> x1 == x2;
		assertThat(le.and(eq).test(1, 1), is(true));
		assertThat(le.and(eq).test(1, 2), is(false));
		assertThat(le.and(eq).test(2, 1), is(false));

		IntBiPredicate lt = (x1, x2) -> x1 < x2;
		assertThat(lt.or(eq).test(1, 1), is(true));
		assertThat(lt.or(eq).test(1, 2), is(true));
		assertThat(lt.or(eq).test(2, 1), is(false));
	}

	@Test
	public void longBiPredicate() {
		LongBiPredicate le = (x1, x2) -> x1 <= x2;

		assertThat(le.negate().test(1, 1), is(false));
		assertThat(le.negate().test(2, 1), is(true));
		assertThat(le.negate().test(1, 2), is(false));

		LongBiPredicate eq = (x1, x2) -> x1 == x2;
		assertThat(le.and(eq).test(1, 1), is(true));
		assertThat(le.and(eq).test(1, 2), is(false));
		assertThat(le.and(eq).test(2, 1), is(false));

		LongBiPredicate lt = (x1, x2) -> x1 < x2;
		assertThat(lt.or(eq).test(1, 1), is(true));
		assertThat(lt.or(eq).test(1, 2), is(true));
		assertThat(lt.or(eq).test(2, 1), is(false));
	}
}