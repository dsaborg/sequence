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

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ChainingSequenceTest {
	private final ChainingSequence<String> empty = new ChainingSequence<>();
	private final ChainingSequence<String> abc = new ChainingSequence<>(Arrays.asList("a", "b", "c"));
	private final ChainingSequence<String> abc_def = new ChainingSequence<>(Arrays.asList("a", "b", "c"),
	                                                                        Arrays.asList("d", "e", "f"));
	private final ChainingSequence<String> abc_def_ghi = new ChainingSequence<>(Arrays.asList("a", "b", "c"),
	                                                                            Arrays.asList("d", "e", "f"),
	                                                                            Arrays.asList("g", "h", "i"));

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void one() {
		twice(() -> assertThat(abc, contains("a", "b", "c")));
	}

	@Test
	public void two() {
		twice(() -> assertThat(abc_def, contains("a", "b", "c", "d", "e", "f")));
	}

	@Test
	public void three() {
		twice(() -> assertThat(abc_def_ghi, contains("a", "b", "c", "d", "e", "f", "g", "h", "i")));
	}

	@Test
	public void lazy() {
		ChainingSequence<String> chainingSequence = new ChainingSequence<>(Arrays.asList("a", "b", "c"), () -> {
			throw new IllegalStateException(); // Not thrown yet, until below when iterator is requested
		});

		Iterator<String> iterator = chainingSequence.iterator();
		assertThat(iterator.next(), is("a"));
		assertThat(iterator.next(), is("b"));
		assertThat(iterator.next(), is("c"));

		expecting(IllegalStateException.class, iterator::hasNext); // Exception not thrown until iterator is encountered
	}

	@Test
	public void appendIterable() {
		abc.append(Arrays.asList("d", "e", "f"));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f"));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f"));
	}

	@Test
	public void appendIterator() {
		abc.append(Arrays.asList("d", "e", "f").iterator());
		abc.append(Arrays.asList("g", "h", "i"));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
		assertThat(abc, contains("a", "b", "c", "g", "h", "i"));
	}

	@Test
	public void appendItems() {
		abc.append("d", "e", "f");
		assertThat(abc, contains("a", "b", "c", "d", "e", "f"));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f"));
	}

	@Test
	public void appendStream() {
		abc.append(Arrays.asList("d", "e", "f").stream());
		abc.append(Arrays.asList("g", "h", "i"));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
	}

	@Test
	public void flatAppendIterables() {
		abc.flatAppend(Arrays.asList(Arrays.asList("d", "e", "f"), Arrays.asList("g", "h", "i")));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
	}

	@Test
	public void flatAppendIterators() {
		abc.flatAppend(Arrays.asList(Arrays.asList("d", "e", "f").iterator(), Arrays.asList("g", "h", "i").iterator()));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
		assertThat(abc, contains("a", "b", "c"));
	}

	@Test
	public void flatAppendArrays() {
		abc.flatAppend(Arrays.asList(new String[]{"d", "e", "f"}, new String[]{"g", "h", "i"}));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
	}

	@Test
	public void flatAppendStreams() {
		abc.flatAppend(Arrays.asList(Arrays.asList("d", "e", "f").stream(), Arrays.asList("g", "h", "i").stream()));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
	}

	@Test
	public void flatAppendPairs() {
		abc.flatAppend(Arrays.asList(Pair.of("d", "e"), Pair.of("f", "g"), Pair.of("h", "i")));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
	}

	@Test
	public void flatAppendMixed() {
		abc.flatAppend(Arrays.asList(Arrays.asList("d", "e", "f"), Arrays.asList("g", "h", "i").iterator(), new String[]{"j", "k", "l"}));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"));
		assertThat(abc, contains("a", "b", "c", "d", "e", "f", "j", "k", "l"));
	}

	@Test
	public void testToString() {
		assertThat(abc_def_ghi.toString(), is("ChainingSequence[[a, b, c], [d, e, f], [g, h, i]]"));
	}

	@Test
	public void testEquals() {
		assertThat(abc.equals(abc), is(true));
		assertThat(abc.equals(new ChainingSequence<>(Arrays.asList("a", "b", "c"))), is(true));
		assertThat(abc.equals(empty), is(false));
		assertThat(abc.equals(abc_def), is(false));
		assertThat(abc.equals(abc_def_ghi), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(abc.hashCode(), is(abc.hashCode()));
		assertThat(abc.hashCode(), is(new ChainingSequence<>(Arrays.asList("a", "b", "c")).hashCode()));
		assertThat(abc.hashCode(), is(not(empty.hashCode())));
		assertThat(abc.hashCode(), is(not(abc_def.hashCode())));
		assertThat(abc.hashCode(), is(not(abc_def_ghi.hashCode())));
	}
}
