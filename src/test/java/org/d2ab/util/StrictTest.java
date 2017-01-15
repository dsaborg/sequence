package org.d2ab.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StrictTest {
	@Test
	public void constructor() {
		new Strict() {
			// for test coverage
		};
	}

	public static class StrictStateTest {
		private static final String previousStrictPropertyValue = System.getProperty(Strict.PROPERTY);

		@BeforeClass
		public static void init() {
			System.setProperty(Strict.PROPERTY, "true");
			Strict.init();
		}

		@AfterClass
		public static void reset() {
			System.setProperty(Strict.PROPERTY, previousStrictPropertyValue);
		}

		@Test
		public void unsetReset() {
			assertThat(Strict.isLenient(), is(false));

			Strict.unset();
			assertThat(Strict.isLenient(), is(true));

			Strict.unset();
			assertThat(Strict.isLenient(), is(true));

			Strict.reset();
			assertThat(Strict.isLenient(), is(true));

			Strict.reset();
			assertThat(Strict.isLenient(), is(false));
		}

		@Test
		public void check() {
			boolean failed;
			try {
				Strict.check();
				failed = false;
			} catch (AssertionError expected) {
				failed = true;
			}

			assertThat(failed, is(true));

			Strict.unset();
			Strict.check();
			Strict.reset();
		}
	}

	public static class LenientStateTest {
		private static final String previousStrictPropertyValue = System.getProperty(Strict.PROPERTY);

		@BeforeClass
		public static void init() {
			System.setProperty(Strict.PROPERTY, "false");
			Strict.init();
		}

		@AfterClass
		public static void reset() {
			System.setProperty(Strict.PROPERTY, previousStrictPropertyValue);
		}

		@Test
		public void unsetReset() {
			assertThat(Strict.isLenient(), is(true));

			Strict.unset();
			assertThat(Strict.isLenient(), is(true));

			Strict.unset();
			assertThat(Strict.isLenient(), is(true));

			Strict.reset();
			assertThat(Strict.isLenient(), is(true));

			Strict.reset();
			assertThat(Strict.isLenient(), is(true));
		}

		@Test
		public void check() {
			Strict.check();

			Strict.unset();
			Strict.check();
			Strict.reset();
		}
	}
}
