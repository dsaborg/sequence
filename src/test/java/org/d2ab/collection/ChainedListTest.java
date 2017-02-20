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

import org.d2ab.sequence.Sequence;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Comparator.reverseOrder;
import static org.d2ab.collection.SizedIterable.SizeType.AVAILABLE;
import static org.d2ab.collection.SizedIterable.SizeType.FIXED;
import static org.d2ab.test.IsIterableBeginningWith.beginsWith;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ChainedListTest {
	private List<Integer> chainedTotallyEmpty = ChainedList.concat(new ArrayList<List<Integer>>());

	private List<Integer> firstEmpty = new ArrayList<>();
	private List<Integer> secondEmpty = new LinkedList<>();
	private List<Integer> thirdEmpty = new ArrayList<>();
	@SuppressWarnings("unchecked")
	private List<Integer> chainedEmpty = ChainedList.concat(firstEmpty, secondEmpty, thirdEmpty);

	private List<Integer> first = new ArrayList<>(Lists.of(1, 2, 3));
	private List<Integer> second = new LinkedList<>(Lists.of(4, 5, 6));
	private List<Integer> third = new ArrayList<>(Lists.of(7, 8, 9, 10));
	@SuppressWarnings("unchecked")
	private List<Integer> chained = ChainedList.concat(first, second, third);

	@SuppressWarnings("unchecked")
	private List<Integer> fixed = ChainedList.concat(Lists.of(1, 2, 3), Lists.of(4, 5, 6), Lists.of(7, 8, 9, 10));

	private List<Integer> infinite = ChainedList.concat(
			Sequence.generate(() -> Lists.of(17)).asList());

	@Test
	public void size() {
		assertThat(chainedTotallyEmpty.size(), is(0));
		assertThat(chainedEmpty.size(), is(0));
		assertThat(chained.size(), is(10));
		assertThat(fixed.size(), is(10));
		expecting(UnsupportedOperationException.class, infinite::size);
	}

	@Test
	public void sizeType() {
		assertThat(Iterables.sizeType(chainedTotallyEmpty), is(AVAILABLE));
		assertThat(Iterables.sizeType(chainedEmpty), is(AVAILABLE));
		assertThat(Iterables.sizeType(chained), is(AVAILABLE));
		assertThat(Iterables.sizeType(fixed), is(FIXED));
		expecting(UnsupportedOperationException.class, () -> Iterables.sizeType(infinite));
	}

	@Test
	public void isEmpty() {
		assertThat(chainedTotallyEmpty.isEmpty(), is(true));
		assertThat(chainedEmpty.isEmpty(), is(true));
		assertThat(chained.isEmpty(), is(false));
		assertThat(fixed.isEmpty(), is(false));
		expecting(UnsupportedOperationException.class, infinite::isEmpty);
	}

	@Test
	public void containsElement() {
		assertThat(chainedTotallyEmpty.contains(17), is(false));

		assertThat(chainedEmpty.contains(17), is(false));

		for (int i = 1; i <= 10; i++)
			assertThat(chained.contains(i), is(true));
		assertThat(chained.contains(17), is(false));

		for (int i = 1; i <= 10; i++)
			assertThat(fixed.contains(i), is(true));
		assertThat(fixed.contains(17), is(false));

		assertThat(infinite.contains(17), is(true));
	}

	@Test
	public void iterator() {
		assertThat(chainedTotallyEmpty, is(emptyIterable()));
		expecting(NoSuchElementException.class, () -> chainedTotallyEmpty.iterator().next());

		assertThat(chainedEmpty, is(emptyIterable()));
		expecting(NoSuchElementException.class, () -> chainedEmpty.iterator().next());

		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(fixed, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(infinite, beginsWith(17, 17, 17, 17));
	}

	@Test
	public void iteratorRemove() {
		Iterator<Integer> iterator = chained.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.next();
		iterator.remove();

		assertThat(chained, contains(1, 3, 5, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 3));
		assertThat(second, contains(5, 6));
		assertThat(third, contains(7, 8, 9, 10));
	}

	@Test
	public void toArray() {
		assertThat(chainedTotallyEmpty.toArray(), is(emptyArray()));

		assertThat(chainedEmpty.toArray(), is(emptyArray()));

		assertThat(chained.toArray(), is(arrayContaining(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));

		assertThat(fixed.toArray(), is(arrayContaining(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void toArrayOfType() {
		assertThat(chainedTotallyEmpty.toArray(new Integer[0]), is(emptyArray()));

		assertThat(chainedEmpty.toArray(new Integer[0]), is(emptyArray()));

		assertThat(chained.toArray(new Integer[10]), is(arrayContaining(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));

		assertThat(fixed.toArray(new Integer[10]), is(arrayContaining(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void add() {
		chainedTotallyEmpty.add(17);
		assertThat(chainedTotallyEmpty, contains(17));

		chainedEmpty.add(17);
		assertThat(chainedEmpty, contains(17));
		assertThat(firstEmpty, contains(17));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		chained.add(17);
		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 17));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10, 17));

		expecting(UnsupportedOperationException.class, () -> fixed.add(17));
	}

	@Test
	public void removeIndexed() {
		expecting(IndexOutOfBoundsException.class, () -> chainedEmpty.remove(17));
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		expecting(IndexOutOfBoundsException.class, () -> chained.remove(17));
		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		assertThat(chained.remove(4), is(5));
		assertThat(chained, contains(1, 2, 3, 4, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 6));
		assertThat(third, contains(7, 8, 9, 10));

		expecting(UnsupportedOperationException.class, () -> fixed.remove(4));
	}

	@Test
	public void remove() {
		assertThat(chainedTotallyEmpty.remove((Integer) 17), is(false));

		assertThat(chainedEmpty.remove((Integer) 17), is(false));
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		assertThat(chained.remove((Integer) 17), is(false));
		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		assertThat(chained.remove((Integer) 5), is(true));
		assertThat(chained, contains(1, 2, 3, 4, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 6));
		assertThat(third, contains(7, 8, 9, 10));

		expecting(UnsupportedOperationException.class, () -> fixed.remove((Integer) 5));
	}

	@Test
	public void containsAll() {
		assertThat(chainedTotallyEmpty.containsAll(Lists.of(17, 18)), is(false));

		assertThat(chainedEmpty.containsAll(Lists.of(17, 18)), is(false));

		assertThat(chained.containsAll(Lists.of(2, 3, 4)), is(true));
		assertThat(chained.containsAll(Lists.of(2, 3, 17)), is(false));

		assertThat(fixed.containsAll(Lists.of(2, 3, 4)), is(true));
		assertThat(fixed.containsAll(Lists.of(2, 3, 17)), is(false));

		assertThat(infinite.containsAll(Lists.of(17)), is(true));
	}

	@Test
	public void addAll() {
		chainedTotallyEmpty.addAll(Lists.of(1, 2));
		assertThat(chainedTotallyEmpty, contains(1, 2));

		chainedEmpty.addAll(Lists.of(1, 2));
		assertThat(chainedEmpty, contains(1, 2));
		assertThat(firstEmpty, contains(1, 2));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		chained.addAll(Lists.of(17, 18));
		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 17, 18));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10, 17, 18));

		expecting(UnsupportedOperationException.class, () -> fixed.addAll(Lists.of(17, 18)));
	}

	@Test
	public void addAllAtIndex() {
		chainedTotallyEmpty.addAll(0, Lists.of(1, 2));
		assertThat(chainedTotallyEmpty, contains(1, 2));

		chainedEmpty.addAll(0, Lists.of(1, 2));
		assertThat(chainedEmpty, contains(1, 2));
		assertThat(firstEmpty, contains(1, 2));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		chained.addAll(3, Lists.of(17, 18));
		assertThat(chained, contains(1, 2, 3, 17, 18, 4, 5, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 3, 17, 18));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		chained.addAll(6, Lists.of(19, 20));
		assertThat(chained, contains(1, 2, 3, 17, 18, 4, 19, 20, 5, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 3, 17, 18));
		assertThat(second, contains(4, 19, 20, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		expecting(IndexOutOfBoundsException.class, () -> chained.addAll(15, Lists.of(21, 22)));
		assertThat(chained, contains(1, 2, 3, 17, 18, 4, 19, 20, 5, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 3, 17, 18));
		assertThat(second, contains(4, 19, 20, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		expecting(UnsupportedOperationException.class, () -> fixed.addAll(3, Lists.of(17, 18)));
	}

	@Test
	public void removeAll() {
		assertThat(chainedTotallyEmpty.removeAll(Lists.of(1, 2)), is(false));
		assertThat(chainedTotallyEmpty, is(emptyIterable()));

		assertThat(chainedEmpty.removeAll(Lists.of(1, 2)), is(false));
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		assertThat(chained.removeAll(Lists.of()), is(false));
		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		assertThat(chained.removeAll(Lists.of(3, 4, 5)), is(true));
		assertThat(chained, contains(1, 2, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2));
		assertThat(second, contains(6));
		assertThat(third, contains(7, 8, 9, 10));

		expecting(UnsupportedOperationException.class, () -> fixed.removeAll(Lists.of(3, 4, 5)));
	}

	@Test
	public void retainAll() {
		assertThat(chainedTotallyEmpty.retainAll(Lists.of(1, 2)), is(false));
		assertThat(chainedTotallyEmpty, is(emptyIterable()));

		assertThat(chainedEmpty.retainAll(Lists.of(1, 2)), is(false));
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		assertThat(chained.retainAll(Lists.of(2, 3, 4)), is(true));
		assertThat(chained, contains(2, 3, 4));
		assertThat(first, contains(2, 3));
		assertThat(second, contains(4));
		assertThat(third, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> fixed.retainAll(Lists.of(2, 3, 4)));
	}

	@Test
	public void replaceAll() {
		chainedTotallyEmpty.replaceAll(x -> x + 1);
		assertThat(chainedTotallyEmpty, is(emptyIterable()));

		chainedEmpty.replaceAll(x -> x + 1);
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		chained.replaceAll(x -> x + 1);
		assertThat(chained, contains(2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
		assertThat(first, contains(2, 3, 4));
		assertThat(second, contains(5, 6, 7));
		assertThat(third, contains(8, 9, 10, 11));

		expecting(UnsupportedOperationException.class, () -> fixed.replaceAll(x -> x + 1));
	}

	@Test
	public void sort() {
		chainedTotallyEmpty.sort(reverseOrder());
		assertThat(chainedTotallyEmpty, is(emptyIterable()));

		chainedEmpty.sort(reverseOrder());
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		chained.sort(reverseOrder());
		assertThat(chained, contains(10, 9, 8, 7, 6, 5, 4, 3, 2, 1));
		assertThat(first, contains(10, 9, 8));
		assertThat(second, contains(7, 6, 5));
		assertThat(third, contains(4, 3, 2, 1));

		expecting(UnsupportedOperationException.class, () -> fixed.sort(reverseOrder()));
	}

	@Test
	public void clear() {
		chainedTotallyEmpty.clear();
		assertThat(chainedTotallyEmpty, is(emptyIterable()));

		chainedEmpty.clear();
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		chained.clear();
		assertThat(chained, is(emptyIterable()));
		assertThat(first, is(emptyIterable()));
		assertThat(second, is(emptyIterable()));
		assertThat(third, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, fixed::clear);
		expecting(UnsupportedOperationException.class, infinite::clear);
	}

	@Test
	public void testEquals() {
		assertThat(chainedTotallyEmpty.equals(Lists.of()), is(true));
		assertThat(chainedTotallyEmpty.equals(Lists.of(1, 2)), is(false));

		assertThat(chainedEmpty.equals(Lists.of()), is(true));
		assertThat(chainedEmpty.equals(Lists.of(1, 2)), is(false));

		assertThat(chained.equals(Lists.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)), is(true));
		assertThat(chained.equals(Lists.of(1, 17, 3, 4, 5, 6, 7, 8, 9, 10)), is(false));

		assertThat(fixed.equals(Lists.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)), is(true));
		assertThat(fixed.equals(Lists.of(1, 17, 3, 4, 5, 6, 7, 8, 9, 10)), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(chainedTotallyEmpty.hashCode(), is(1));

		assertThat(chainedEmpty.hashCode(), is(1));

		assertThat(chained.hashCode(), is(-975991962));

		assertThat(fixed.hashCode(), is(-975991962));
	}

	@Test
	public void get() {
		assertThat(chained.get(0), is(1));
		assertThat(chained.get(2), is(3));
		assertThat(chained.get(4), is(5));
		assertThat(chained.get(7), is(8));
		assertThat(chained.get(9), is(10));
		expecting(IndexOutOfBoundsException.class, () -> chained.get(10));

		assertThat(fixed.get(0), is(1));
		assertThat(fixed.get(2), is(3));
		assertThat(fixed.get(4), is(5));
		assertThat(fixed.get(7), is(8));
		assertThat(fixed.get(9), is(10));
		expecting(IndexOutOfBoundsException.class, () -> fixed.get(10));
	}

	@Test
	public void set() {
		assertThat(chained.set(2, 17), is(3));
		assertThat(chained, contains(1, 2, 17, 4, 5, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 17));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		assertThat(chained.set(4, 18), is(5));
		assertThat(chained, contains(1, 2, 17, 4, 18, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 17));
		assertThat(second, contains(4, 18, 6));
		assertThat(third, contains(7, 8, 9, 10));

		expecting(IndexOutOfBoundsException.class, () -> chained.set(10, 19));
		assertThat(chained, contains(1, 2, 17, 4, 18, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 17));
		assertThat(second, contains(4, 18, 6));
		assertThat(third, contains(7, 8, 9, 10));

		expecting(UnsupportedOperationException.class, () -> fixed.set(2, 17));
	}

	@Test
	public void addAtIndex() {
		chainedTotallyEmpty.add(0, 17);
		assertThat(chainedTotallyEmpty, contains(17));

		chainedEmpty.add(0, 17);
		assertThat(chainedEmpty, contains(17));
		assertThat(firstEmpty, contains(17));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		chained.add(2, 17);
		assertThat(first, contains(1, 2, 17, 3));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		chained.add(4, 18);
		assertThat(first, contains(1, 2, 17, 3, 18));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		chained.add(6, 19);
		assertThat(first, contains(1, 2, 17, 3, 18));
		assertThat(second, contains(4, 19, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		expecting(IndexOutOfBoundsException.class, () -> chained.add(14, 21));
		assertThat(first, contains(1, 2, 17, 3, 18));
		assertThat(second, contains(4, 19, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		expecting(UnsupportedOperationException.class, () -> fixed.add(2, 17));
	}

	@Test
	public void indexOf() {
		assertThat(chainedTotallyEmpty.indexOf(17), is(-1));

		assertThat(chainedEmpty.indexOf(17), is(-1));

		assertThat(chained.indexOf(3), is(2));
		assertThat(chained.indexOf(5), is(4));
		assertThat(chained.indexOf(8), is(7));
		assertThat(chained.indexOf(17), is(-1));

		assertThat(fixed.indexOf(3), is(2));
		assertThat(fixed.indexOf(5), is(4));
		assertThat(fixed.indexOf(8), is(7));
		assertThat(fixed.indexOf(17), is(-1));

		assertThat(infinite.indexOf(17), is(0));
	}

	@Test
	public void lastIndexOf() {
		assertThat(chainedTotallyEmpty.lastIndexOf(17), is(-1));

		assertThat(chainedEmpty.lastIndexOf(17), is(-1));

		assertThat(chained.lastIndexOf(3), is(2));
		assertThat(chained.lastIndexOf(5), is(4));
		assertThat(chained.lastIndexOf(8), is(7));
		assertThat(chained.lastIndexOf(17), is(-1));

		assertThat(fixed.lastIndexOf(3), is(2));
		assertThat(fixed.lastIndexOf(5), is(4));
		assertThat(fixed.lastIndexOf(8), is(7));
		assertThat(fixed.lastIndexOf(17), is(-1));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Integer> totallyEmptyIterator = chainedTotallyEmpty.listIterator();
		assertThat(totallyEmptyIterator.hasNext(), is(false));
		assertThat(totallyEmptyIterator.hasPrevious(), is(false));
		assertThat(totallyEmptyIterator.nextIndex(), is(0));
		assertThat(totallyEmptyIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, totallyEmptyIterator::next);

		ListIterator<Integer> emptyIterator = chainedEmpty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyIterator::next);

		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		ListIterator<Integer> listIterator = chained.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.add(33);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is(1));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.next(), is(2));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.next(), is(3));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.previous(), is(3));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.previous(), is(2));
		listIterator.set(17);
		listIterator.remove();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.next(), is(3));
		listIterator.add(18);
		listIterator.add(19);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(5));
		assertThat(listIterator.previousIndex(), is(4));

		assertThat(listIterator.next(), is(4));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(6));
		assertThat(listIterator.previousIndex(), is(5));

		assertThat(chained, contains(33, 1, 3, 18, 19, 4, 5, 6, 7, 8, 9, 10));
		assertThat(first, contains(33, 1, 3, 18, 19));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));
	}

	@Test
	public void exhaustiveListIterator() {
		ListIterator<Integer> listIterator = chained.listIterator();

		AtomicInteger i = new AtomicInteger();
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is(i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
			expecting(NoSuchElementException.class, listIterator::next);

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previous(), is(i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
			expecting(NoSuchElementException.class, listIterator::previous);
		});
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Integer> iterator = chained.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is(i + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));

		assertThat(chained, is(emptyIterable()));
		assertThat(first, is(emptyIterable()));
		assertThat(second, is(emptyIterable()));
		assertThat(third, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		ListIterator<Integer> listIterator = chained.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.next(), is(i + 1));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(10));

		assertThat(chained, is(emptyIterable()));
		assertThat(first, is(emptyIterable()));
		assertThat(second, is(emptyIterable()));
		assertThat(third, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 10;
		ListIterator<Integer> listIterator = chained.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previous(), is(i + 1));
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
		}
		assertThat(i, is(0));

		assertThat(chained, is(emptyIterable()));
		assertThat(first, is(emptyIterable()));
		assertThat(second, is(emptyIterable()));
		assertThat(third, is(emptyIterable()));
	}

	@Test
	public void subList() {
		List<Integer> totallyEmptySubList = chainedTotallyEmpty.subList(0, 0);
		assertThat(totallyEmptySubList, is(emptyIterable()));
		totallyEmptySubList.add(17);
		assertThat(totallyEmptySubList, contains(17));
		assertThat(chainedTotallyEmpty, contains(17));

		List<Integer> emptySubList = chainedEmpty.subList(0, 0);
		assertThat(emptySubList, is(emptyIterable()));
		emptySubList.add(17);
		assertThat(emptySubList, contains(17));
		assertThat(chainedEmpty, contains(17));
		assertThat(firstEmpty, contains(17));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		assertThat(chained.subList(2, 8), contains(3, 4, 5, 6, 7, 8));

		assertThat(fixed.subList(2, 8), contains(3, 4, 5, 6, 7, 8));
	}

	@Test
	public void stream() {
		assertThat(chainedTotallyEmpty.stream().collect(Collectors.toList()), is(emptyIterable()));

		assertThat(chainedEmpty.stream().collect(Collectors.toList()), is(emptyIterable()));

		assertThat(chained.stream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(fixed.stream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(infinite.stream().limit(4).collect(Collectors.toList()), contains(17, 17, 17, 17));
	}

	@Test
	public void parallelStream() {
		assertThat(chainedTotallyEmpty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));

		assertThat(chainedEmpty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));

		assertThat(chained.parallelStream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(fixed.parallelStream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(infinite.parallelStream().limit(4).collect(Collectors.toList()), contains(17, 17, 17, 17));
	}

	@Test
	public void removeIf() {
		chainedTotallyEmpty.removeIf(x -> x.equals(2) || x.equals(5));
		assertThat(chainedTotallyEmpty, is(emptyIterable()));

		chainedEmpty.removeIf(x -> x.equals(2) || x.equals(5));
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		chained.removeIf(x -> x.equals(2) || x.equals(5));
		assertThat(chained, contains(1, 3, 4, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 3));
		assertThat(second, contains(4, 6));
		assertThat(third, contains(7, 8, 9, 10));

		expecting(UnsupportedOperationException.class, () -> fixed.removeIf(x -> x.equals(2) || x.equals(5)));
	}

	@Test
	public void forEach() {
		chainedTotallyEmpty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		chainedEmpty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		chained.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(11));

		value.set(1);
		fixed.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(11));
	}
}