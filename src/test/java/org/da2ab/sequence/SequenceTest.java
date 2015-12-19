package org.da2ab.sequence;

import junitrepeat.Repeat;
import junitrepeat.RepeatRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SequenceTest {
	private final Sequence<Integer> empty = Sequence.<Integer>empty();
	private final Sequence<Integer> oneOnly = Sequence.of(1);
	private final Sequence<Integer> oneToTwo = Sequence.of(1, 2);
	private final Sequence<Integer> oneToNine = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
	private final Sequence<Integer> oneToThree = Sequence.of(1, 2, 3);
	private final Sequence<Integer> three = oneToThree;
	private final Sequence<Integer> oneRandom = Sequence.of(17);
	private final Sequence<Integer> twoRandom = Sequence.of(17, 32);
	private final Sequence<Integer> nineRandom = Sequence.of(67, 5, 43, 3, 5, 7, 24, 5, 67);
	private final Sequence<Integer> oneToFive = Sequence.of(1, 2, 3, 4, 5);

	@Rule
	public RepeatRule repeatRule = new RepeatRule();

	@Test
	@Repeat(times = 2)
	public void ofOne() throws Exception {
		assertThat(oneOnly, contains(1));
	}

	@Test
	@Repeat(times = 2)
	public void ofMany() throws Exception {
		assertThat(oneToThree, contains(1, 2, 3));
	}

	@Test
	@Repeat(times = 2)
	public void forLoop() throws Exception {
		for (int i : empty)
			fail("Should not get called");

		int expected = 1;
		for (int i : oneToThree)
			assertThat(i, is(expected++));
	}

	@Test
	@Repeat(times = 2)
	public void forEach() throws Exception {
		empty.forEach(i -> fail("Should not get called"));
		oneOnly.forEach(i -> assertThat(i, is(in(asList(1, 2, 3)))));
		oneToTwo.forEach(i -> assertThat(i, is(in(asList(1, 2, 3)))));
		oneToThree.forEach(i -> assertThat(i, is(in(asList(1, 2, 3)))));
	}

	@Test
	@Repeat(times = 2)
	public void iterator() throws Exception {
		Iterator iterator = oneToThree.iterator();

		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(1));

		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(2));

		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(3));

		assertThat(iterator.hasNext(), is(false));
		assertThat(iterator.hasNext(), is(false));
	}

	@Test
	public void ofNone() throws Exception {
		Sequence<Integer> sequence = Sequence.<Integer>of();

		assertThat(sequence, is(emptyIterable()));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	@Repeat(times=2)
	public void empty() throws Exception {
		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void ofNulls() throws Exception {
		Sequence<Integer> sequence = Sequence.of(1, null, 2, 3, null);

		assertThat(sequence, contains(1, null, 2, 3, null));
		assertThat(sequence, contains(1, null, 2, 3, null));
	}

	@Test
	public void fromSequence() throws Exception {
		Sequence<Integer> fromSequence = Sequence.from(oneToThree);

		assertThat(fromSequence, contains(1, 2, 3));
		assertThat(fromSequence, contains(1, 2, 3));
	}

	@Test
	public void fromIterable() throws Exception {
		Iterable<Integer> iterable = () -> asList(1, 2, 3).iterator();

		Sequence<Integer> sequenceFromIterable = Sequence.from(iterable);

		assertThat(sequenceFromIterable, contains(1, 2, 3));
		assertThat(sequenceFromIterable, contains(1, 2, 3));
	}

	@Test
	public void fromIteratorSupplier() throws Exception {
		Supplier<Iterator<Integer>> iterators = () -> asList(1, 2, 3).iterator();

		Sequence<Integer> sequenceFromIterators = Sequence.from(iterators);

		assertThat(sequenceFromIterators, contains(1, 2, 3));
		assertThat(sequenceFromIterators, contains(1, 2, 3));
	}

	@Test
	public void fromIterables() throws Exception {
		Iterable<Integer> first = asList(1, 2, 3);
		Iterable<Integer> second = asList(4, 5, 6);
		Iterable<Integer> third = asList(7, 8, 9);

		Sequence<Integer> sequenceFromIterables = Sequence.from(first, second, third);

		assertThat(sequenceFromIterables, contains(1, 2, 3, 4, 5, 6, 7, 8, 9));
		assertThat(sequenceFromIterables, contains(1, 2, 3, 4, 5, 6, 7, 8, 9));
	}

	@Test
	public void fromNoIterables() throws Exception {
		Sequence<Integer> sequenceFromNoIterables = Sequence.from(new Iterable[]{});

		assertThat(sequenceFromNoIterables, is(emptyIterable()));
		assertThat(sequenceFromNoIterables, is(emptyIterable()));
	}

	@Test
	public void fromIterator() throws Exception {
		Iterator<Integer> iterator = asList(1, 2, 3).iterator();

		Sequence<Integer> sequenceFromIterator = Sequence.from(iterator);

		assertThat(sequenceFromIterator, contains(1, 2, 3));
		assertThat(sequenceFromIterator, is(emptyIterable()));
	}

	@Test
	public void skip() {
		Sequence<Integer> skipNone = oneToThree.skip(0);
		assertThat(skipNone, contains(1, 2, 3));
		assertThat(skipNone, contains(1, 2, 3));

		Sequence<Integer> skipOne = oneToThree.skip(1);
		assertThat(skipOne, contains(2, 3));
		assertThat(skipOne, contains(2, 3));

		Sequence<Integer> skipTwo = oneToThree.skip(2);
		assertThat(skipTwo, contains(3));
		assertThat(skipTwo, contains(3));

		Sequence<Integer> skipThree = oneToThree.skip(3);
		assertThat(skipThree, is(emptyIterable()));
		assertThat(skipThree, is(emptyIterable()));

		Sequence<Integer> skipFour = oneToThree.skip(4);
		assertThat(skipFour, is(emptyIterable()));
		assertThat(skipFour, is(emptyIterable()));
	}

	@Test
	public void limit() {
		Sequence<Integer> noLimit = oneToThree.limit(0);
		assertThat(noLimit, is(emptyIterable()));
		assertThat(noLimit, is(emptyIterable()));

		Sequence<Integer> limitOne = oneToThree.limit(1);
		assertThat(limitOne, contains(1));
		assertThat(limitOne, contains(1));

		Sequence<Integer> limitTwo = oneToThree.limit(2);
		assertThat(limitTwo, contains(1, 2));
		assertThat(limitTwo, contains(1, 2));

		Sequence<Integer> limitThree = oneToThree.limit(3);
		assertThat(limitThree, contains(1, 2, 3));
		assertThat(limitThree, contains(1, 2, 3));

		Sequence<Integer> limitFour = oneToThree.limit(4);
		assertThat(limitFour, contains(1, 2, 3));
		assertThat(limitFour, contains(1, 2, 3));
	}

	@Test
	public void then() {
		Sequence<Integer> then = oneToThree.then(Sequence.of(4, 5, 6)).then(Sequence.of(7, 8));

		assertThat(then, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(then, contains(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void thenIsLazy() {
		Iterator<Integer> first = asList(1, 2, 3).iterator();
		Iterator<Integer> second = asList(4, 5, 6).iterator();
		Iterator<Integer> third = asList(7, 8).iterator();

		Sequence<Integer> then = Sequence.from(first).then(() -> second).then(() -> third);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));
		assertThat(third.hasNext(), is(true));

		assertThat(then, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(then, is(emptyIterable())); // iterators exhausted on second run
	}

	@Test
	public void thenIsLazyWhenSkippingHasNext() {
		Iterator<Integer> first = Collections.singletonList(1).iterator();
		Iterator<Integer> second = Collections.singletonList(2).iterator();

		Sequence<Integer> sequence = Sequence.from(first).then(() -> second);

		// check delayed iteration
		Iterator<Integer> iterator = sequence.iterator();
		assertThat(iterator.next(), is(1));
		assertThat(iterator.next(), is(2));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		Sequence<Integer> filtered = Sequence.of(1, 2, 3, 4, 5, 6, 7).filter(i -> i % 2 == 0);

		assertThat(filtered, contains(2, 4, 6));
		assertThat(filtered, contains(2, 4, 6));
	}

	@Test
	public void flatMapIterables() {
		Sequence<List<Integer>> sequence = Sequence.of(asList(1, 2), asList(3, 4), asList(5, 6));
		Sequence<Integer> flatMap = sequence.flatMap(Function.identity());
		assertThat(flatMap, contains(1, 2, 3, 4, 5, 6));
		assertThat(flatMap, contains(1, 2, 3, 4, 5, 6));
	}

	@Test
	public void flatMapLazy() {
		Sequence<Integer> flatMap = Sequence.of(asList(1, 2), null).flatMap(Function.identity());

		// NPE if not lazy - see below
		Iterator<Integer> iterator = flatMap.iterator();
		assertThat(iterator.next(), is(1));
		assertThat(iterator.next(), is(2));
		try {
			iterator.next();
			fail("Expected NPE");
		} catch (NullPointerException ignored) {
			// expected
		}

		Iterator<Integer> iterator2 = flatMap.iterator();
		assertThat(iterator2.next(), is(1));
		assertThat(iterator2.next(), is(2));
		try {
			iterator2.next();
			fail("Expected NPE");
		} catch (NullPointerException ignored) {
			// expected
		}
	}

	@Test
	public void flatMapIterators() {
		Sequence<Iterator<Integer>> sequence = Sequence.of(asList(1, 2).iterator(), asList(3, 4).iterator(), asList(5, 6)
				                                                                                                     .iterator());
		Sequence<Integer> flatMap = sequence.flatMap(Sequence::from);
		assertThat(flatMap, contains(1, 2, 3, 4, 5, 6));
		assertThat(flatMap, is(emptyIterable()));
	}

	@Test
	public void flatMapArrays() {
		Sequence<Integer[]> sequence = Sequence.of(new Integer[]{1, 2}, new Integer[]{3, 4}, new Integer[]{5, 6});
		Sequence<Integer> flatMap = sequence.flatMap(Sequence::of);
		assertThat(flatMap, contains(1, 2, 3, 4, 5, 6));
		assertThat(flatMap, contains(1, 2, 3, 4, 5, 6));
	}

	@Test
	public void map() {
		Sequence<Integer> sequence = oneToThree;
		assertThat(sequence.map(Object::toString), contains("1", "2", "3"));
	}

	@Test
	public void mapIsLazy() {
		Sequence<Integer> sequence = Sequence.of(1, null);

		// NPE if not lazy
		Iterator<String> iterator = sequence.map(Object::toString).iterator();
		assertThat(iterator.next(), is("1"));
		try {
			iterator.next();
			fail("Expected NPE");
		} catch (NullPointerException ignored) {
			// expected
		}

		// re-apply
		Iterator<String> iterator2 = sequence.map(Object::toString).iterator();
		assertThat(iterator2.next(), is("1"));
		try {
			iterator2.next();
			fail("Expected NPE");
		} catch (NullPointerException ignored) {
			// expected
		}
	}

	@Test
	public void recurse() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i + 1);
		assertThat(sequence.limit(10), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		assertThat(sequence.limit(10), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void recurseDouble() {
		Sequence<String> sequence = Sequence.recurse(1, Object::toString, s -> Integer.parseInt(s) + 1);
		assertThat(sequence.limit(10), contains("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
	}

	@Test
	public void recurseUntilNull() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i < 10 ? i + 1 : null).untilNull();
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void recurseUntil() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i + 1).until(7);
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 6));
	}

	@Test
	public void recurseThrowableCause() {
		Exception e = new IllegalStateException(new IllegalArgumentException(new NullPointerException()));
		Sequence<Throwable> sequence = Sequence.recurse(e, Throwable::getCause).until(null);
		Iterator<Throwable> iterator = sequence.iterator();
		assertThat(iterator.next(), is(instanceOf(IllegalStateException.class)));
		assertThat(iterator.next(), is(instanceOf(IllegalArgumentException.class)));
		assertThat(iterator.next(), is(instanceOf(NullPointerException.class)));
	}

	@Test
	public void toList() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5, 6, 7);

		List<Integer> list = sequence.toList();
		assertThat(list, instanceOf(ArrayList.class));
		assertThat(list, contains(1, 2, 3, 4, 5, 6, 7));

		List<Integer> list2 = sequence.toList();
		assertThat(list, instanceOf(ArrayList.class));
		assertThat(list2, contains(1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void toLinkedList() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5, 6, 7);

		List<Integer> list = sequence.toList(LinkedList::new);
		assertThat(list, instanceOf(LinkedList.class));
		assertThat(list, contains(1, 2, 3, 4, 5, 6, 7));

		List<Integer> list2 = sequence.toList(LinkedList::new);
		assertThat(list, instanceOf(LinkedList.class));
		assertThat(list2, contains(1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void toSet() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5, 6, 7);

		Set<Integer> set = sequence.toSet();
		assertThat(set, instanceOf(HashSet.class));
		assertThat(set, containsInAnyOrder(1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void toSortedSet() {
		Sequence<Integer> sequence = Sequence.of(1, 5, 2, 6, 3, 4, 7);

		SortedSet<Integer> sortedSet = sequence.toSortedSet();
		assertThat(sortedSet, instanceOf(TreeSet.class));
		assertThat(sortedSet, contains(1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void toSetWithType() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5, 6, 7);

		Set<Integer> set = sequence.toSet(LinkedHashSet::new);
		assertThat(set, instanceOf(LinkedHashSet.class));
		assertThat(set, contains(1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void toCollection() {
		Sequence<Integer> sequence = oneToThree;

		Deque<Integer> deque = sequence.toCollection(ArrayDeque::new);

		assertThat(deque, instanceOf(ArrayDeque.class));
		assertThat(deque, contains(1, 2, 3));
	}

	@Test
	public void toMap() {
		Sequence<Integer> sequence = oneToThree;

		Map<Integer, String> map = sequence.toMap(Function.identity(), Object::toString);

		Map<Integer, String> expected = new HashMap<>();
		expected.put(1, "1");
		expected.put(2, "2");
		expected.put(3, "3");

		assertThat(map, instanceOf(HashMap.class));
		assertThat(map, is(equalTo(expected)));
	}

	@Test
	public void toMapFromPairs() {
		Map<Integer, String> original = new HashMap<>();
		original.put(1, "1");
		original.put(2, "2");
		original.put(3, "3");

		Map<Integer, String> map = Sequence.from(original)
		                                   .filter(p -> p.test(x -> x != 2, y -> true))
		                                   .map(p -> p.map(x -> x * 2, y -> y + "x2"))
		                                   .pairsToMap(Function.identity());

		Map<Integer, String> expected = new HashMap<>();
		expected.put(2, "1x2");
		expected.put(6, "3x2");

		assertThat(map, instanceOf(HashMap.class));
		assertThat(map, is(equalTo(expected)));
	}

	@Test
	public void toMapWithTypeFromPairs() {
		Map<Integer, String> original = new HashMap<>();
		original.put(1, "1");
		original.put(2, "2");
		original.put(3, "3");

		Map<Integer, String> map = Sequence.from(original)
		                                   .filter(p -> p.test(x -> x != 2, y -> true))
		                                   .map(p -> p.map(x -> x * 2, y -> y + "x2"))
		                                   .pairsToMap(LinkedHashMap::new, Function.identity());

		Map<Integer, String> expected = new HashMap<>();
		expected.put(2, "1x2");
		expected.put(6, "3x2");

		assertThat(map, instanceOf(HashMap.class));
		assertThat(map, is(equalTo(expected)));
	}

	@Test
	public void toSortedMap() {
		Sequence<Integer> sequence = Sequence.of(1, 3, 2);

		SortedMap<Integer, String> sortedMap = sequence.toSortedMap(Function.identity(), Object::toString);

		SortedMap<Integer, String> expected = new TreeMap<>();
		expected.put(1, "1");
		expected.put(2, "2");
		expected.put(3, "3");

		assertThat(sortedMap, instanceOf(TreeMap.class));
		assertThat(sortedMap, is(equalTo(expected)));
	}

	@Test
	public void toMapWithType() {
		Sequence<Integer> sequence = oneToThree;

		Map<Integer, String> map = sequence.toMap(LinkedHashMap::new, Function.identity(), Object::toString);

		Map<Integer, String> expected = new LinkedHashMap<>();
		expected.put(1, "1");
		expected.put(2, "2");
		expected.put(3, "3");

		assertThat(map, instanceOf(LinkedHashMap.class));
		assertThat(map, is(equalTo(expected)));
	}

	@Test
	public void collect() {
		Sequence<Integer> sequence = oneToThree;

		Deque<Integer> deque = sequence.collect(ArrayDeque::new, ArrayDeque::add);

		assertThat(deque, instanceOf(ArrayDeque.class));
		assertThat(deque, contains(1, 2, 3));
	}

	@Test
	public void toArray() {
		Object[] array = oneToThree.toArray();
		assertThat(array, arrayContaining(1, 2, 3));
	}

	@Test
	public void toArrayWithType() {
		Integer[] array = oneToThree.toArray(Integer[]::new);
		assertThat(array, arrayContaining(1, 2, 3));
	}

	@Test
	public void collector() {
		Sequence<Integer> sequence = oneToThree;
		assertThat(sequence.collect(Collectors.toList()), contains(1, 2, 3));
	}

	@Test
	public void join() {
		Sequence<Integer> sequence = oneToThree;

		assertThat(sequence.join(", "), is("1, 2, 3"));
	}

	@Test
	public void joinWithPrefixAndSuffix() {
		Sequence<Integer> sequence = oneToThree;

		assertThat(sequence.join("<", ", ", ">"), is("<1, 2, 3>"));
	}

	@Test
	public void reduce() {
		assertThat(empty.reduce(Integer::sum), is(Optional.empty()));
		assertThat(oneOnly.reduce(Integer::sum), is(Optional.of(1)));
		assertThat(oneToTwo.reduce(Integer::sum), is(Optional.of(3)));
		assertThat(oneToThree.reduce(Integer::sum), is(Optional.of(6)));
	}

	@Test
	public void reduceWithIdentity() {
		assertThat(empty.reduce(17, Integer::sum), is(17));
		assertThat(oneOnly.reduce(17, Integer::sum), is(18));
		assertThat(oneToTwo.reduce(17, Integer::sum), is(20));
		assertThat(oneToThree.reduce(17, Integer::sum), is(23));
	}

	@Test
	public void first() {
		assertThat(empty.first(), is(Optional.empty()));
		assertThat(oneOnly.first(), is(Optional.of(1)));
		assertThat(oneToTwo.first(), is(Optional.of(1)));
		assertThat(oneToThree.first(), is(Optional.of(1)));
	}

	@Test
	public void second() {
		assertThat(empty.second(), is(Optional.empty()));
		assertThat(oneOnly.second(), is(Optional.empty()));
		assertThat(oneToTwo.second(), is(Optional.of(2)));
		assertThat(oneToThree.second(), is(Optional.of(2)));
		assertThat(Sequence.of(1, 2, 3, 4).second(), is(Optional.of(2)));
	}

	@Test
	public void third() {
		assertThat(empty.third(), is(Optional.empty()));
		assertThat(oneOnly.third(), is(Optional.empty()));
		assertThat(oneToTwo.third(), is(Optional.empty()));
		assertThat(oneToThree.third(), is(Optional.of(3)));
		assertThat(Sequence.of(1, 2, 3, 4).third(), is(Optional.of(3)));
		assertThat(oneToFive.third(), is(Optional.of(3)));
	}

	@Test
	public void last() {
		assertThat(empty.last(), is(Optional.empty()));
		assertThat(empty.last(), is(Optional.empty()));

		assertThat(oneOnly.last(), is(Optional.of(1)));
		assertThat(oneOnly.last(), is(Optional.of(1)));

		assertThat(oneToTwo.last(), is(Optional.of(2)));
		assertThat(oneToTwo.last(), is(Optional.of(2)));

		assertThat(oneToThree.last(), is(Optional.of(3)));
		assertThat(oneToThree.last(), is(Optional.of(3)));
	}

	@Test
	public void fibonacci() {
		Sequence<Integer> fibonacci = Sequence.recurse(Pair.of(0, 1), p -> Pair.of(p.second(), p.apply(Integer::sum)))
		                                      .map(Pair::first);
		assertThat(fibonacci.limit(10), contains(0, 1, 1, 2, 3, 5, 8, 13, 21, 34));
	}

	@Test
	public void pairs() {
		assertThat(empty.pairs(), is(emptyIterable()));
		assertThat(empty.pairs(), is(emptyIterable()));

		assertThat(oneOnly.pairs(), is(emptyIterable()));
		assertThat(oneOnly.pairs(), is(emptyIterable()));

		assertThat(oneToTwo.pairs(), contains(Pair.of(1, 2)));
		assertThat(oneToTwo.pairs(), contains(Pair.of(1, 2)));

		assertThat(oneToThree.pairs(), contains(Pair.of(1, 2), Pair.of(2, 3)));
		assertThat(oneToThree.pairs(), contains(Pair.of(1, 2), Pair.of(2, 3)));

		assertThat(oneToFive.pairs(), contains(Pair.of(1, 2), Pair.of(2, 3), Pair.of(3, 4), Pair.of(4, 5)));
		assertThat(oneToFive.pairs(), contains(Pair.of(1, 2), Pair.of(2, 3), Pair.of(3, 4), Pair.of(4, 5)));
	}

	@Test
	public void partition() {
		assertThat(oneToFive.partition(3), contains(asList(1, 2, 3), asList(2, 3, 4), asList(3, 4, 5)));
		assertThat(oneToFive.partition(3), contains(asList(1, 2, 3), asList(2, 3, 4), asList(3, 4, 5)));
	}

	@Test
	public void step() {
		assertThat(oneToNine.step(3), contains(1, 4, 7));
		assertThat(oneToNine.step(3), contains(1, 4, 7));
	}

	@Test
	public void partitionAndStep() {
		assertThat(oneToFive.partition(3).step(2), contains(asList(1, 2, 3), asList(3, 4, 5)));
		assertThat(oneToFive.partition(3).step(2), contains(asList(1, 2, 3), asList(3, 4, 5)));

		Sequence<List<Integer>> partitioned = oneToFive.partition(3);
		assertThat(partitioned.step(2), contains(asList(1, 2, 3), asList(3, 4, 5)));
		assertThat(partitioned.step(2), contains(asList(1, 2, 3), asList(3, 4, 5)));
	}

	@Test
	public void distinct() {
		assertThat(empty.distinct(), emptyIterable());
		assertThat(empty.distinct(), emptyIterable());

		assertThat(oneRandom.distinct(), contains(17));
		assertThat(oneRandom.distinct(), contains(17));

		Sequence<Integer> duplicated = Sequence.of(17, 17);
		assertThat(duplicated.distinct(), contains(17));
		assertThat(duplicated.distinct(), contains(17));

		assertThat(nineRandom.distinct(), contains(67, 5, 43, 3, 7, 24));
		assertThat(nineRandom.distinct(), contains(67, 5, 43, 3, 7, 24));
	}

	@Test
	public void sorted() {
		assertThat(empty.sorted(), emptyIterable());
		assertThat(empty.sorted(), emptyIterable());

		assertThat(oneRandom.sorted(), contains(17));
		assertThat(oneRandom.sorted(), contains(17));

		assertThat(twoRandom.sorted(), contains(17, 32));
		assertThat(twoRandom.sorted(), contains(17, 32));

		assertThat(nineRandom.sorted(), contains(3, 5, 5, 5, 7, 24, 43, 67, 67));
		assertThat(nineRandom.sorted(), contains(3, 5, 5, 5, 7, 24, 43, 67, 67));
	}

	@Test
	public void sortedComparator() {
		assertThat(empty.sorted(Comparator.reverseOrder()), emptyIterable());
		assertThat(empty.sorted(Comparator.reverseOrder()), emptyIterable());

		assertThat(oneRandom.sorted(Comparator.reverseOrder()), contains(17));
		assertThat(oneRandom.sorted(Comparator.reverseOrder()), contains(17));

		assertThat(twoRandom.sorted(Comparator.reverseOrder()), contains(32, 17));
		assertThat(twoRandom.sorted(Comparator.reverseOrder()), contains(32, 17));

		assertThat(nineRandom.sorted(Comparator.reverseOrder()), contains(67, 67, 43, 24, 7, 5, 5, 5, 3));
		assertThat(nineRandom.sorted(Comparator.reverseOrder()), contains(67, 67, 43, 24, 7, 5, 5, 5, 3));
	}

	@Test
	public void min() {
		assertThat(empty.min(Comparator.naturalOrder()), is(Optional.empty()));
		assertThat(empty.min(Comparator.naturalOrder()), is(Optional.empty()));

		assertThat(oneRandom.min(Comparator.naturalOrder()), is(Optional.of(17)));
		assertThat(oneRandom.min(Comparator.naturalOrder()), is(Optional.of(17)));

		assertThat(twoRandom.min(Comparator.naturalOrder()), is(Optional.of(17)));
		assertThat(twoRandom.min(Comparator.naturalOrder()), is(Optional.of(17)));

		assertThat(nineRandom.min(Comparator.naturalOrder()), is(Optional.of(3)));
		assertThat(nineRandom.min(Comparator.naturalOrder()), is(Optional.of(3)));
	}

	@Test
	public void max() {
		assertThat(empty.max(Comparator.naturalOrder()), is(Optional.empty()));
		assertThat(empty.max(Comparator.naturalOrder()), is(Optional.empty()));

		assertThat(oneRandom.max(Comparator.naturalOrder()), is(Optional.of(17)));
		assertThat(oneRandom.max(Comparator.naturalOrder()), is(Optional.of(17)));

		assertThat(twoRandom.max(Comparator.naturalOrder()), is(Optional.of(32)));
		assertThat(twoRandom.max(Comparator.naturalOrder()), is(Optional.of(32)));

		assertThat(nineRandom.max(Comparator.naturalOrder()), is(Optional.of(67)));
		assertThat(nineRandom.max(Comparator.naturalOrder()), is(Optional.of(67)));
	}

	@Test
	public void count() {
		assertThat(empty.count(), is(0));
		assertThat(empty.count(), is(0));

		assertThat(oneOnly.count(), is(1));
		assertThat(oneOnly.count(), is(1));

		assertThat(oneToTwo.count(), is(2));
		assertThat(oneToTwo.count(), is(2));

		assertThat(oneToNine.count(), is(9));
		assertThat(oneToNine.count(), is(9));
	}

	@Test
	public void any() {
		assertThat(three.any(x -> x > 0), is(true));
		assertThat(three.any(x -> x > 0), is(true));

		assertThat(three.any(x -> x > 2), is(true));
		assertThat(three.any(x -> x > 2), is(true));

		assertThat(three.any(x -> x > 4), is(false));
		assertThat(three.any(x -> x > 4), is(false));
	}

	@Test
	public void all() {
		assertThat(three.all(x -> x > 0), is(true));
		assertThat(three.all(x -> x > 0), is(true));

		assertThat(three.all(x -> x > 2), is(false));
		assertThat(three.all(x -> x > 2), is(false));

		assertThat(three.all(x -> x > 4), is(false));
		assertThat(three.all(x -> x > 4), is(false));
	}

	@Test
	public void none() {
		assertThat(three.none(x -> x > 0), is(false));
		assertThat(three.none(x -> x > 0), is(false));

		assertThat(three.none(x -> x > 2), is(false));
		assertThat(three.none(x -> x > 2), is(false));

		assertThat(three.none(x -> x > 4), is(true));
		assertThat(three.none(x -> x > 4), is(true));
	}
}
