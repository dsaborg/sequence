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
package org.d2ab.iterable;

import org.junit.Test;
import org.d2ab.test.Tests;

import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ChainingIterableTest {
	@Test
	public void empty() {
		ChainingIterable<String> chainingIterable = new ChainingIterable<>();
		assertThat(chainingIterable, is(emptyIterable()));
		assertThat(chainingIterable, is(emptyIterable()));
	}

	@Test
	public void one() {
		ChainingIterable<String> chainingIterable = new ChainingIterable<>(asList("a", "b", "c"));
		assertThat(chainingIterable, contains("a", "b", "c"));
		assertThat(chainingIterable, contains("a", "b", "c"));
	}

	@Test
	public void two() {
		ChainingIterable<String> chainingIterable = new ChainingIterable<>(asList("a", "b", "c"),
		                                                                   asList("d", "e", "f"));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f"));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f"));
	}

	@Test
	public void three() {
		ChainingIterable<String> chainingIterable = new ChainingIterable<>(asList("a", "b", "c"),
		                                                                   asList("d", "e", "f"),
		                                                                   asList("g", "h", "i"));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
	}

	@Test
	public void lazy() {
		ChainingIterable<String> chainingIterable = new ChainingIterable<>(asList("a", "b", "c"), null);
		Iterator<String> iterator = chainingIterable.iterator();
		assertThat(iterator.next(), is("a"));
		assertThat(iterator.next(), is("b"));
		assertThat(iterator.next(), is("c"));
		Tests.expecting(NullPointerException.class, iterator::hasNext);
	}

	@Test
	public void appendIterable() {
		ChainingIterable<String> chainingIterable = new ChainingIterable<>(asList("a", "b", "c"));
		chainingIterable.append(asList("d", "e", "f"));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f"));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f"));
	}

	@Test
	public void appendIterator() {
		ChainingIterable<String> chainingIterable = new ChainingIterable<>(asList("a", "b", "c"));
		chainingIterable.append(asList("d", "e", "f").iterator());
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f"));
		assertThat(chainingIterable, contains("a", "b", "c"));
	}

	@Test
	public void appendItems() {
		ChainingIterable<String> chainingIterable = new ChainingIterable<>(asList("a", "b", "c"));
		chainingIterable.append("d", "e", "f");
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f"));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f"));
	}

	@Test
	public void appendAllIterables() {
		ChainingIterable<String> chainingIterable = new ChainingIterable<>(asList("a", "b", "c"));
		chainingIterable.appendAll(asList(asList("d", "e", "f"), asList("g", "h", "i")));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
	}

	@Test
	public void appendAllIterators() {
		ChainingIterable<String> chainingIterable = new ChainingIterable<>(asList("a", "b", "c"));
		chainingIterable.appendAll(asList(asList("d", "e", "f").iterator(), asList("g", "h", "i").iterator()));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
		assertThat(chainingIterable, contains("a", "b", "c"));
	}

	@Test
	public void appendAllArrays() {
		ChainingIterable<String> chainingIterable = new ChainingIterable<>(asList("a", "b", "c"));
		chainingIterable.appendAll(asList(new String[]{"d", "e", "f"}, new String[]{"g", "h", "i"}));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f", "g", "h", "i"));
	}

	@Test
	public void appendAllMixed() {
		ChainingIterable<String> chainingIterable = new ChainingIterable<>(asList("a", "b", "c"));
		chainingIterable.appendAll(asList(asList("d", "e", "f"),
		                                  asList("g", "h", "i").iterator(),
		                                  new String[]{"j", "k", "l"}));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"));
		assertThat(chainingIterable, contains("a", "b", "c", "d", "e", "f", "j", "k", "l"));
	}
}
