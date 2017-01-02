package org.d2ab.collection.chars;

import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class CharCollectionBoxingTest extends BaseBoxingTest {
	CharCollection empty = CharCollection.Base.create();
	CharCollection collection = CharCollection.Base.create('a', 'b', 'c', 'd', 'e');

	@Test
	public void toArray() {
		assertArrayEquals(new Character[0], empty.toArray());
		assertArrayEquals(new Character[]{'a', 'b', 'c', 'd', 'e'}, collection.toArray());
	}

	@Test
	public void toArrayWithType() {
		assertArrayEquals(new Character[0], empty.toArray(new Character[0]));
		assertArrayEquals(new Character[]{'a', 'b', 'c', 'd', 'e'}, collection.toArray(new Character[0]));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger('a');
		collection.forEach(x -> assertThat(x, is((char) value.getAndIncrement())));
		assertThat((char) value.get(), is('f'));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(collection.stream().collect(Collectors.toList()), contains('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(collection.parallelStream().collect(Collectors.toList()), contains('a', 'b', 'c', 'd', 'e'));
	}
}
