/*
 * Copyright 2016 Daniel Skogquist Åborg
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

package org.d2ab.collection;

import org.d2ab.iterator.ints.IntIterator;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ArrayIntListTest {
	private final ArrayIntList empty = new ArrayIntList();

	private final int[] contents;
	private final int[] prefix;
	private final int[] suffix;
	private final ArrayIntList full;

	public ArrayIntListTest(int[] contents, int offset, int length, int capacity, int[] prefix, int[] suffix) {
		this.contents = Arrays.copyOf(contents, contents.length);
		this.prefix = prefix;
		this.suffix = suffix;
		full = new ArrayIntList(this.contents, offset, length, capacity);
	}

	@Parameters
	public static Object[][] parameters() {
		return new Object[][]{
				{new int[]{0xD, 0xE, 0xA, 0xD, 1, 2, 3, 4, 5, 0xB, 0xE, 0xE, 0xF}, 4, 5, 5,
				 new int[]{0xD, 0xE, 0xA, 0xD}, new int[]{0xB, 0xE, 0xE, 0xF}},
				};
	}

	@After
	public void checkPadding() {
		assertArrayEquals(prefix, Arrays.copyOf(contents, prefix.length));
		assertArrayEquals(suffix, Arrays.copyOfRange(contents, contents.length - suffix.length, contents.length));
	}

	@Test
	public void size() throws Exception {
		assertThat(empty.size(), is(0));
		assertThat(full.size(), is(5));
	}

	@Test
	public void isEmpty() throws Exception {
		assertThat(empty.isEmpty(), is(true));
		assertThat(full.isEmpty(), is(false));
	}

	@Test
	public void clear() throws Exception {
		empty.clear();
		assertThat(empty, is(emptyIterable()));

		full.clear();
		assertThat(full, is(emptyIterable()));
	}

	@Test
	public void toIntArray() throws Exception {
		assertArrayEquals(new int[0], empty.toIntArray());
		assertArrayEquals(new int[]{1, 2, 3, 4, 5}, full.toIntArray());
	}

	@Test
	public void iterator() throws Exception {
		assertThat(empty, is(emptyIterable()));
		assertThat(full, containsInts(1, 2, 3, 4, 5));
	}

	@Test
	public void listIteratorEmpty() {
		IntListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		IntListIterator listIterator = full.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.nextInt(), is(1));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.nextInt(), is(2));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.nextInt(), is(3));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.previousInt(), is(3));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.previousInt(), is(2));

		listIterator.set(17);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.nextInt(), is(17));

		listIterator.add(18);
		listIterator.add(19);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));
		assertThat(listIterator.nextInt(), is(3));

		assertThat(full, contains(1, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		IntListIterator listIterator = full.listIterator();

		AtomicInteger i = new AtomicInteger();
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextInt(), is(i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previousInt(), is(i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
		});
	}

	@Test
	public void iteratorRemoveAll() {
		IntIterator iterator = full.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.nextInt(), is(i + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(5));

		assertThat(full, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		IntListIterator listIterator = full.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.nextInt(), is(i + 1));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(5));

		assertThat(full, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 5;
		IntListIterator listIterator = full.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previousInt(), is(i + 1));
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
		}
		assertThat(i, is(0));

		assertThat(full, is(emptyIterable()));
	}

	@Test
	public void subList() throws Exception {

	}

	@Test
	public void replaceAllInts() throws Exception {

	}

	@Test
	public void getAt() throws Exception {

	}

	@Test
	public void setAt() throws Exception {

	}

	@Test
	public void addAt() throws Exception {

	}

	@Test
	public void removeAt() throws Exception {

	}

	@Test
	public void lastIndexOf() throws Exception {

	}

	@Test
	public void indexOf() throws Exception {

	}

	@Test
	public void addInt() throws Exception {

	}

	@Test
	public void addAll() throws Exception {

	}

	@Test
	public void addAllAt() throws Exception {

	}

	@Test
	public void containsAll() throws Exception {

	}

	@Test
	public void removeInt() throws Exception {

	}

	@Test
	public void containsInt() throws Exception {

	}

	@Test
	public void removeAll() throws Exception {

	}

	@Test
	public void retainAll() throws Exception {

	}

	@Test
	public void removeIntsIf() throws Exception {

	}

	@Test
	public void forEachInt() throws Exception {

	}
}