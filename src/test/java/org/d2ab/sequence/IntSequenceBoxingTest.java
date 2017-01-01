package org.d2ab.sequence;

import org.d2ab.collection.ints.IntList;
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class IntSequenceBoxingTest extends BaseBoxingTest {
	private final IntSequence empty = IntSequence.empty();
	private final IntSequence _12345 = IntSequence.from(IntList.create(1, 2, 3, 4, 5));

	@Test
	public void forLoop() {
		twice(() -> {
			for (int ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			int expected = 1;
			for (int i : _12345)
				assertThat(i, is(expected++));

			assertThat(expected, is(6));
		});
	}
}
