package org.d2ab.test;

import org.d2ab.util.Strict;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class BaseBoxingTest {
	@BeforeClass
	public static void unsetStrict() {
		Strict.unset();
	}

	@AfterClass
	public static void resetStrict() {
		Strict.reset();
	}
}
