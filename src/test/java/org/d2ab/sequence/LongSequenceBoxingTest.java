package org.d2ab.sequence;

import org.d2ab.collection.longs.LongList;
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class LongSequenceBoxingTest extends BaseBoxingTest {
	private final LongSequence empty = LongSequence.empty();
	private final LongSequence _12345 = LongSequence.from(LongList.create(1L, 2L, 3L, 4L, 5L));

	@Test
	public void forLoop() {
		twice(() -> {
			for (long ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			long expected = 1;
			for (long i : _12345)
				assertThat(i, is(expected++));

			assertThat(expected, is(6L));
		});
	}
}
