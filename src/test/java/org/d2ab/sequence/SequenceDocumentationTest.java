/*
 * Copyright 2015 Daniel Skogquist Åborg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.d2ab.sequence;

import org.d2ab.collection.Maps;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class SequenceDocumentationTest {
	@Test
	public void filterAndMap() {
		List<String> evens = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
		                             .filter(x -> x % 2 == 0)
		                             .map(Objects::toString)
		                             .toList();

		assertThat(evens, contains("2", "4", "6", "8"));
	}

	@Test
	public void toMapFromSeparateSequences() {
		Sequence<Integer> keys = Sequence.of(1, 2, 3);
		Sequence<String> values = Sequence.of("1", "2", "3");

		Map<Integer, String> map = keys.interleave(values).toMap();

		assertThat(map, is(equalTo(Maps.builder(1, "1").put(2, "2").put(3, "3").build())));
	}

	@Test
	public void reuseOfSequence() {
		Sequence<Integer> singulars = Sequence.recurse(1, i -> i + 1).limit(10); // Digits 1..10

		// using sequence of ints 1..10 first time to get odd numbers between 1 and 10
		Sequence<Integer> odds = singulars.step(2);
		int x = 0, expectedOdds[] = {1, 3, 5, 7, 9};
		for (int odd : odds)
			assertThat(odd, is(expectedOdds[x++]));

		// re-using the same sequence again to get squares of numbers between 4 and 9
		Sequence<Integer> squares = singulars.map(i -> i * i).skip(3).limit(5);
		int y = 0, expectedSquares[] = {16, 25, 36, 49, 64};
		for (int square : squares)
			assertThat(square, is(expectedSquares[y++]));
	}

	@SuppressWarnings("SpellCheckingInspection")
	@Test
	public void streamToSequenceAndBack() {
		Stream<String> abcd = Arrays.asList("a", "b", "c", "d").stream();
		Stream<String> abbccd = Sequence.from(abcd).pair().<String>flatten().stream();
		assertThat(abbccd.collect(Collectors.toList()), contains("a", "b", "b", "c", "c", "d"));
	}

	@Test
	public void fibonacci() {
		Sequence<Integer> fibonacci = Sequence.recurse(Pair.of(0, 1),
		                                               pair -> pair.shiftedLeft(pair.apply(Integer::sum)))
		                                      .map(Pair::getLeft)
		                                      .until(55);
		assertThat(fibonacci, contains(0, 1, 1, 2, 3, 5, 8, 13, 21, 34));
	}

	@Test
	public void factorial() {
		Sequence<Long> sequence = Sequence.recurse(1L, i -> i + 1).limit(13);
		assertThat(sequence.reduce(1L, (r, i) -> r * i), is(6227020800L));
	}
}
