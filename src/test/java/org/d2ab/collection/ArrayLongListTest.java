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

import org.d2ab.collection.iterator.LongIterator;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ArrayLongListTest {
	private final ArrayLongList empty = new ArrayLongList();

	private final long[] contents;
	private final long[] prefix;
	private final long[] suffix;
	private final ArrayLongList full;

	public ArrayLongListTest(long[] contents, int offset, int length, int capacity, long[] prefix, long[] suffix) {
		this.contents = Arrays.copyOf(contents, contents.length);
		this.prefix = prefix;
		this.suffix = suffix;
		full = new ArrayLongList(this.contents, offset, length, capacity);
	}

	@Parameters
	public static Object[][] parameters() {
		return new Object[][]{
				{new long[]{0xD, 0xE, 0xA, 0xD, 1, 2, 3, 4, 5, 0xB, 0xE, 0xE, 0xF}, 4, 5, 5,
				 new long[]{0xD, 0xE, 0xA, 0xD}, new long[]{0xB, 0xE, 0xE, 0xF}},
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
	public void toLongArray() throws Exception {
		assertArrayEquals(new long[0], empty.toLongArray());
		assertArrayEquals(new long[]{1, 2, 3, 4, 5}, full.toLongArray());
	}

	@Test
	public void iterator() throws Exception {
		assertThat(empty, is(emptyIterable()));
		assertThat(full, containsLongs(1, 2, 3, 4, 5));
	}

	@Test
	public void listIteratorEmpty() {
		LongListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		LongListIterator listIterator = full.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.nextLong(), is(1L));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.nextLong(), is(2L));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.nextLong(), is(3L));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.previousLong(), is(3L));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.previousLong(), is(2L));

		listIterator.set(17);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.nextLong(), is(17L));

		listIterator.add(18);
		listIterator.add(19);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));
		assertThat(listIterator.nextLong(), is(3L));

		assertThat(full, containsLongs(1, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		LongListIterator listIterator = full.listIterator();

		AtomicInteger i = new AtomicInteger();
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextLong(), is((long) (i.get() + 1)));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previousLong(), is((long) (i.get() + 1)));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
		});
	}

	@Test
	public void iteratorRemoveAll() {
		LongIterator iterator = full.iterator();

		long l = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.nextLong(), is(l + 1));
			iterator.remove();
			l++;
		}
		assertThat(l, is(5L));

		assertThat(full, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		LongListIterator listIterator = full.listIterator();

		long l = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.nextLong(), is(l + 1));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			l++;
		}
		assertThat(l, is(5L));

		assertThat(full, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 5;
		LongListIterator listIterator = full.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previousLong(), is((long) (i + 1)));
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
	public void replaceAllLongs() throws Exception {

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
	public void addLong() throws Exception {

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
	public void removeLong() throws Exception {

	}

	@Test
	public void containsLong() throws Exception {

	}

	@Test
	public void removeAll() throws Exception {

	}

	@Test
	public void retainAll() throws Exception {

	}

	@Test
	public void removeLongsIf() throws Exception {

	}

	@Test
	public void forEachLong() throws Exception {

	}
}