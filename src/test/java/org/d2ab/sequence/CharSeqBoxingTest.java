package org.d2ab.sequence;

import org.d2ab.collection.chars.CharList;
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class CharSeqBoxingTest extends BaseBoxingTest {
	private final CharSeq empty = CharSeq.empty();
	private final CharSeq abcde = CharSeq.from(CharList.create('a', 'b', 'c', 'd', 'e'));

	@Test
	public void forLoop() {
		twice(() -> {
			for (int ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			char expected = 'a';
			for (char c : abcde)
				assertThat(c, is(expected++));

			assertThat(expected, is('f'));
		});
	}

	@Test
	public void stream() {
		twice(() -> assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable())));
		twice(() -> assertThat(abcde.stream().collect(Collectors.toList()), contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void streamFromOnce() {
		CharSeq emptyOnce = CharSeq.once(empty.iterator());
		assertThat(emptyOnce.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(emptyOnce.stream().collect(Collectors.toList()), is(emptyIterable()));

		CharSeq abcdeOnce = CharSeq.once(abcde.iterator());
		assertThat(abcdeOnce.stream().collect(Collectors.toList()), contains('a', 'b', 'c', 'd', 'e'));
		assertThat(abcdeOnce.stream().collect(Collectors.toList()), is(emptyIterable()));
	}
}
