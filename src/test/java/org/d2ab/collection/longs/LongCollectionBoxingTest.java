package org.d2ab.collection.longs;

import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class LongCollectionBoxingTest extends BaseBoxingTest {
	private final LongCollection empty = LongCollection.Base.create();
	private final LongCollection collection = LongCollection.Base.create(1, 2, 3, 4, 5);

	@Test
	public void toArray() {
		assertArrayEquals(new Long[0], empty.toArray());
		assertArrayEquals(new Long[]{1L, 2L, 3L, 4L, 5L}, collection.toArray());
	}

	@Test
	public void toArrayWithType() {
		assertArrayEquals(new Long[0], empty.toArray(new Long[0]));
		assertArrayEquals(new Long[]{1L, 2L, 3L, 4L, 5L}, collection.toArray(new Long[0]));
	}
}
