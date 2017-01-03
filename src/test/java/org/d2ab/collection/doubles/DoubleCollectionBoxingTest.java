package org.d2ab.collection.doubles;

import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DoubleCollectionBoxingTest extends BaseBoxingTest {
	DoubleCollection empty = DoubleCollection.Base.create();
	DoubleCollection collection = DoubleCollection.Base.create(1, 2, 3, 4, 5);

	@Test
	public void toArray() {
		assertArrayEquals(new Double[0], empty.toArray());
		assertArrayEquals(new Double[]{1.0, 2.0, 3.0, 4.0, 5.0}, collection.toArray());
	}

	@Test
	public void toArrayWithType() {
		assertArrayEquals(new Double[0], empty.toArray(new Double[0]));
		assertArrayEquals(new Double[]{1.0, 2.0, 3.0, 4.0, 5.0}, collection.toArray(new Double[0]));
	}
}
