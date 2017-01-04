package org.d2ab.sequence;

import org.d2ab.collection.doubles.DoubleIterable;
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DoubleSequenceBoxingTest extends BaseBoxingTest {
	private final DoubleSequence empty = DoubleSequence.empty();
	private final DoubleSequence _12345 = DoubleSequence.from(DoubleIterable.of(1.0, 2.0, 3.0, 4.0, 5.0));

	@Test
	public void forLoop() {
		twice(() -> {
			for (double ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			double expected = 1.0;
			for (double d : _12345)
				assertThat(d, is(expected++));

			assertThat(expected, is(6.0));
		});
	}
}
