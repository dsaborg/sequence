package org.da2ab.sequence;

import junitrepeat.Repeat;
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
	private final Sequence<Integer> oneToThree = Sequence.of(1, 2, 3);
	private final Sequence<Integer> oneToFour = Sequence.of(1, 2, 3, 4);
	private final Sequence<Integer> oneToFive = Sequence.of(1, 2, 3, 4, 5);
	private final Sequence<Integer> oneToNine = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
	private final Sequence<Integer> oneRandom = Sequence.of(17);
	private final Sequence<Integer> twoRandom = Sequence.of(17, 32);
	private final Sequence<Integer> threeRandom = Sequence.of(2, 3, 1);
	private final Sequence<Integer> nineRandom = Sequence.of(67, 5, 43, 3, 5, 7, 24, 5, 67);

	@Test
	public void ofOne() throws Exception {
		checkingReuse(() -> assertThat(oneOnly, contains(1)));
	}

	public static void checkingReuse(Runnable action) {
		action.run();
		action.run();
	}

	@Test
	public void ofMany() throws Exception {
		checkingReuse(() -> assertThat(oneToThree, contains(1, 2, 3)));
	}

	@Test
	public void forLoop() throws Exception {
		checkingReuse(() -> {
			for (int ignored : empty)
				fail("Should not get called");
		});

		checkingReuse(() -> {
			int expected = 1;
			for (int i : oneToThree)
				assertThat(i, is(expected++));
		});
	}

	@Test
	public void forEach() throws Exception {
		checkingReuse(() -> {
			empty.forEach(i -> fail("Should not get called"));
			oneOnly.forEach(i -> assertThat(i, is(in(asList(1, 2, 3)))));
			oneToTwo.forEach(i -> assertThat(i, is(in(asList(1, 2, 3)))));
			oneToThree.forEach(i -> assertThat(i, is(in(asList(1, 2, 3)))));
		});
	}

	@Test
	@Repeat(times = 2)
	public void iterator() throws Exception {
		checkingReuse(() -> {
			Iterator iterator = oneToThree.iterator();

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(1));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(2));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(3));

			assertThat(iterator.hasNext(), is(false));
			assertThat(iterator.hasNext(), is(false));
		});
	}

	@Test
	public void ofNone() throws Exception {
		Sequence<Integer> sequence = Sequence.<Integer>of();

		checkingReuse(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() throws Exception {
		checkingReuse(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void ofNulls() throws Exception {
		Sequence<Integer> sequence = Sequence.of(1, null, 2, 3, null);

		checkingReuse(() -> assertThat(sequence, contains(1, null, 2, 3, null)));
	}

	@Test
	public void fromSequence() throws Exception {
		Sequence<Integer> fromSequence = Sequence.from(oneToThree);

		checkingReuse(() -> assertThat(fromSequence, contains(1, 2, 3)));
	}

	@Test
	public void fromIterable() throws Exception {
		Iterable<Integer> iterable = () -> asList(1, 2, 3).iterator();

		Sequence<Integer> sequenceFromIterable = Sequence.from(iterable);

		checkingReuse(() -> assertThat(sequenceFromIterable, contains(1, 2, 3)));
	}

	@Test
	public void fromIteratorSupplier() throws Exception {
		Supplier<Iterator<Integer>> iterators = () -> asList(1, 2, 3).iterator();

		Sequence<Integer> sequenceFromIterators = Sequence.from(iterators);

		checkingReuse(() -> assertThat(sequenceFromIterators, contains(1, 2, 3)));
	}

	@Test
	public void fromIterables() throws Exception {
		Iterable<Integer> first = asList(1, 2, 3);
		Iterable<Integer> second = asList(4, 5, 6);
		Iterable<Integer> third = asList(7, 8, 9);

		Sequence<Integer> sequenceFromIterables = Sequence.from(first, second, third);

		checkingReuse(() -> assertThat(sequenceFromIterables, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void fromNoIterables() throws Exception {
		Sequence<Integer> sequenceFromNoIterables = Sequence.from(new Iterable[]{});

		checkingReuse(() -> assertThat(sequenceFromNoIterables, is(emptyIterable())));
	}

	@Test
	public void skip() {
		Sequence<Integer> skipNone = oneToThree.skip(0);
		checkingReuse(() -> assertThat(skipNone, contains(1, 2, 3)));

		Sequence<Integer> skipOne = oneToThree.skip(1);
		checkingReuse(() -> assertThat(skipOne, contains(2, 3)));

		Sequence<Integer> skipTwo = oneToThree.skip(2);
		checkingReuse(() -> assertThat(skipTwo, contains(3)));

		Sequence<Integer> skipThree = oneToThree.skip(3);
		checkingReuse(() -> assertThat(skipThree, is(emptyIterable())));

		Sequence<Integer> skipFour = oneToThree.skip(4);
		checkingReuse(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		Sequence<Integer> limitNone = oneToThree.limit(0);
		checkingReuse(() -> assertThat(limitNone, is(emptyIterable())));

		Sequence<Integer> limitOne = oneToThree.limit(1);
		checkingReuse(() -> assertThat(limitOne, contains(1)));

		Sequence<Integer> limitTwo = oneToThree.limit(2);
		checkingReuse(() -> assertThat(limitTwo, contains(1, 2)));

		Sequence<Integer> limitThree = oneToThree.limit(3);
		checkingReuse(() -> assertThat(limitThree, contains(1, 2, 3)));

		Sequence<Integer> limitFour = oneToThree.limit(4);
		checkingReuse(() -> assertThat(limitFour, contains(1, 2, 3)));
	}

	@Test
	public void then() {
		Sequence<Integer> then = oneToThree.then(Sequence.of(4, 5, 6)).then(Sequence.of(7, 8));

		checkingReuse(() -> assertThat(then, contains(1, 2, 3, 4, 5, 6, 7, 8)));
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

		checkingReuse(() -> assertThat(filtered, contains(2, 4, 6)));
	}

	@Test
	public void filterAndMap() {
		List<String> evens = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
		                             .filter(x -> x % 2 == 0)
		                             .map(Objects::toString)
		                             .toList();

		checkingReuse(() -> assertThat(evens, contains("2", "4", "6", "8")));
	}

	@Test
	public void reuseOfSequence() {
		Sequence<Integer> singulars = Sequence.recurse(1, i -> i + 1).limit(10);

		// using sequence of ints 1..10 first time
		int x = 0, odds[] = {1, 3, 5, 7, 9};
		for (int odd : singulars.step(2))
			assertThat(odd, is(odds[x++]));

		// re-using the same sequence again
		int y = 0, squares[] = {16, 25, 36, 49, 64};
		for (int square : singulars.map(i -> i * i).skip(3).limit(5))
			assertThat(square, is(squares[y++]));
	}

	@Test
	public void flatMapIterables() {
		Sequence<List<Integer>> sequence = Sequence.of(asList(1, 2), asList(3, 4), asList(5, 6));

		Sequence<Integer> flatMap = sequence.flatMap(Function.identity());

		checkingReuse(() -> assertThat(flatMap, contains(1, 2, 3, 4, 5, 6)));
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

		checkingReuse(() -> assertThat(flatMap, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void flattenIterables() {
		Sequence<Integer> flattened = Sequence.of(asList(1, 2), asList(3, 4), asList(5, 6)).flatten();

		checkingReuse(() -> assertThat(flattened, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void flattenLazy() {
		Sequence<Integer> flattened = Sequence.of(asList(1, 2), null).flatten();

		checkingReuse(() -> {
			// NPE if not lazy - see below
			Iterator<Integer> iterator = flattened.iterator();
			assertThat(iterator.next(), is(1));
			assertThat(iterator.next(), is(2));
			try {
				iterator.next();
				fail("Expected NPE");
			} catch (NullPointerException ignored) {
				// expected
			}
		});
	}

	@Test
	public void flattenIterators() {
		Sequence<Iterator<Integer>> sequence = Sequence.of(asList(1, 2).iterator(), asList(3, 4).iterator(), asList(5, 6)
				                                                                                                     .iterator());
		Sequence<Integer> flattened = sequence.flatten();
		assertThat(flattened, contains(1, 2, 3, 4, 5, 6));
		assertThat(flattened, is(emptyIterable()));
	}

	@Test
	public void flattenArrays() {
		Sequence<Integer[]> sequence = Sequence.of(new Integer[]{1, 2}, new Integer[]{3, 4}, new Integer[]{5, 6});

		Sequence<Integer> flattened = sequence.flatten();
		checkingReuse(() -> assertThat(flattened, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void map() {
		Sequence<String> mapped = oneToThree.map(Object::toString);
		checkingReuse(() -> assertThat(mapped, contains("1", "2", "3")));
	}

	@Test
	public void mapIsLazy() {
		Sequence<Integer> sequence = Sequence.of(1, null);

		checkingReuse(() -> {
			// NPE here if not lazy
			Iterator<String> iterator = sequence.map(Object::toString).iterator();

			assertThat(iterator.next(), is("1"));

			try {
				iterator.next();
				fail("Expected NPE");
			} catch (NullPointerException ignored) {
				// expected
			}
		});
	}

	@Test
	public void recurse() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i + 1);
		checkingReuse(() -> assertThat(sequence.limit(10), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void recurseTwins() {
		Sequence<String> sequence = Sequence.recurse(1, Object::toString, s -> Integer.parseInt(s) + 1);
		checkingReuse(() -> assertThat(sequence.limit(10), contains("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")));
	}

	@Test
	public void recurseUntilNull() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i < 10 ? i + 1 : null).untilNull();
		checkingReuse(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void recurseUntil() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i + 1).until(7);
		checkingReuse(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void recurseThrowableCause() {
		Exception e = new IllegalStateException(new IllegalArgumentException(new NullPointerException()));

		Sequence<Throwable> sequence = Sequence.recurse(e, Throwable::getCause).until(null);

		checkingReuse(() -> {
			Iterator<Throwable> iterator = sequence.iterator();
			assertThat(iterator.next(), is(instanceOf(IllegalStateException.class)));
			assertThat(iterator.next(), is(instanceOf(IllegalArgumentException.class)));
			assertThat(iterator.next(), is(instanceOf(NullPointerException.class)));
		});
	}

	@Test
	public void toList() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5, 6, 7);

		checkingReuse(() -> {
			List<Integer> list = sequence.toList();
			assertThat(list, instanceOf(ArrayList.class));
			assertThat(list, contains(1, 2, 3, 4, 5, 6, 7));
		});
	}

	@Test
	public void toLinkedList() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5, 6, 7);

		checkingReuse(() -> {
			List<Integer> list = sequence.toList(LinkedList::new);
			assertThat(list, instanceOf(LinkedList.class));
			assertThat(list, contains(1, 2, 3, 4, 5, 6, 7));
		});
	}

	@Test
	public void toSet() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5, 6, 7);

		checkingReuse(() -> {
			Set<Integer> set = sequence.toSet();
			assertThat(set, instanceOf(HashSet.class));
			assertThat(set, containsInAnyOrder(1, 2, 3, 4, 5, 6, 7));
		});
	}

	@Test
	public void toSortedSet() {
		Sequence<Integer> sequence = Sequence.of(1, 5, 2, 6, 3, 4, 7);

		checkingReuse(() -> {
			SortedSet<Integer> sortedSet = sequence.toSortedSet();
			assertThat(sortedSet, instanceOf(TreeSet.class));
			assertThat(sortedSet, contains(1, 2, 3, 4, 5, 6, 7));
		});
	}

	@Test
	public void toSetWithType() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5, 6, 7);

		checkingReuse(() -> {
			Set<Integer> set = sequence.toSet(LinkedHashSet::new);
			assertThat(set, instanceOf(LinkedHashSet.class));
			assertThat(set, contains(1, 2, 3, 4, 5, 6, 7));
		});
	}

	@Test
	public void toCollection() {
		Sequence<Integer> sequence = oneToThree;

		checkingReuse(() -> {
			Deque<Integer> deque = sequence.toCollection(ArrayDeque::new);
			assertThat(deque, instanceOf(ArrayDeque.class));
			assertThat(deque, contains(1, 2, 3));
		});
	}

	@Test
	public void toMapFromPairs() {
		Map<String, Integer> original = MapBuilder.of("1", 1).and("2", 2).and("3", 3).and("4", 4).build();

		Sequence<Pair<String, Integer>> sequence = Sequence.from(original)
		                                                   .filter(p -> p.test((s, i) -> i != 2))
		                                                   .map(p -> p.map((s, i) -> Pair.of(s + " x 2", i * 2)));

		checkingReuse(() -> {
			Map<String, Integer> map = sequence.pairsToMap(Function.identity());
			assertThat(map, instanceOf(HashMap.class));
			assertThat(map, is(equalTo(MapBuilder.of("1 x 2", 2).and("3 x 2", 6).and("4 x 2", 8).build())));
		});
	}

	@Test
	public void toMapWithTypeFromPairs() {
		Map<String, Integer> original = MapBuilder.of("1", 1).and("2", 2).and("3", 3).and("4", 4).build();

		Sequence<Pair<String, Integer>> sequence = Sequence.from(original)
		                                                   .filter(p -> p.test((s, i) -> i != 2))
		                                                   .map(p -> p.map((s, i) -> Pair.of(s + " x 2", i * 2)));

		checkingReuse(() -> {
			Map<String, Integer> map = sequence.pairsToMap(LinkedHashMap::new, Function.identity());

			assertThat(map, instanceOf(HashMap.class));
			assertThat(map, is(equalTo(MapBuilder.of("1 x 2", 2).and("3 x 2", 6).and("4 x 2", 8).build())));
		});
	}

	@Test
	public void toMap() {
		checkingReuse(() -> {
			Map<String, Integer> map = oneToThree.toMap(Object::toString, Function.identity());

			assertThat(map, instanceOf(HashMap.class));
			assertThat(map, is(equalTo(MapBuilder.of("1", 1).and("2", 2).and("3", 3).build())));
		});
	}

	@Test
	public void toMapWithType() {
		checkingReuse(() -> {
			Map<String, Integer> map = oneToThree.toMap(LinkedHashMap::new, Object::toString, Function.identity());

			assertThat(map, instanceOf(LinkedHashMap.class));
			assertThat(map, is(equalTo(MapBuilder.of("1", 1).and("2", 2).and("3", 3).build())));
		});
	}

	@Test
	public void toSortedMap() {
		checkingReuse(() -> {
			SortedMap<String, Integer> sortedMap = threeRandom.toSortedMap(Object::toString, Function.identity());

			assertThat(sortedMap, instanceOf(TreeMap.class));
			assertThat(sortedMap, is(equalTo(MapBuilder.of("1", 1).and("2", 2).and("3", 3).build())));
		});
	}

	@Test
	public void collect() {
		checkingReuse(() -> {
			Deque<Integer> deque = oneToThree.collect(ArrayDeque::new, ArrayDeque::add);

			assertThat(deque, instanceOf(ArrayDeque.class));
			assertThat(deque, contains(1, 2, 3));
		});
	}

	@Test
	public void toArray() {
		checkingReuse(() -> assertThat(oneToThree.toArray(), is(arrayContaining(1, 2, 3))));
	}

	@Test
	public void toArrayWithType() {
		checkingReuse(() -> assertThat(oneToThree.toArray(Integer[]::new), arrayContaining(1, 2, 3)));
	}

	@Test
	public void collector() {
		checkingReuse(() -> assertThat(oneToThree.collect(Collectors.toList()), contains(1, 2, 3)));
	}

	@Test
	public void join() {
		checkingReuse(() -> assertThat(oneToThree.join(", "), is("1, 2, 3")));
	}

	@Test
	public void joinWithPrefixAndSuffix() {
		checkingReuse(() -> assertThat(oneToThree.join("<", ", ", ">"), is("<1, 2, 3>")));
	}

	@Test
	public void reduce() {
		checkingReuse(() -> {
			assertThat(empty.reduce(Integer::sum), is(Optional.empty()));
			assertThat(oneOnly.reduce(Integer::sum), is(Optional.of(1)));
			assertThat(oneToTwo.reduce(Integer::sum), is(Optional.of(3)));
			assertThat(oneToThree.reduce(Integer::sum), is(Optional.of(6)));
		});
	}

	@Test
	public void reduceWithIdentity() {
		checkingReuse(() -> {
			assertThat(empty.reduce(17, Integer::sum), is(17));
			assertThat(oneOnly.reduce(17, Integer::sum), is(18));
			assertThat(oneToTwo.reduce(17, Integer::sum), is(20));
			assertThat(oneToThree.reduce(17, Integer::sum), is(23));
		});
	}

	@Test
	public void first() {
		checkingReuse(() -> {
			assertThat(empty.first(), is(Optional.empty()));
			assertThat(oneOnly.first(), is(Optional.of(1)));
			assertThat(oneToTwo.first(), is(Optional.of(1)));
			assertThat(oneToThree.first(), is(Optional.of(1)));
		});
	}

	@Test
	public void second() {
		checkingReuse(() -> {
			assertThat(empty.second(), is(Optional.empty()));
			assertThat(oneOnly.second(), is(Optional.empty()));
			assertThat(oneToTwo.second(), is(Optional.of(2)));
			assertThat(oneToThree.second(), is(Optional.of(2)));
			assertThat(oneToFour.second(), is(Optional.of(2)));
		});
	}

	@Test
	public void third() {
		checkingReuse(() -> {
			assertThat(empty.third(), is(Optional.empty()));
			assertThat(oneOnly.third(), is(Optional.empty()));
			assertThat(oneToTwo.third(), is(Optional.empty()));
			assertThat(oneToThree.third(), is(Optional.of(3)));
			assertThat(oneToFour.third(), is(Optional.of(3)));
			assertThat(oneToFive.third(), is(Optional.of(3)));
		});
	}

	@Test
	public void last() {
		checkingReuse(() -> {
			assertThat(empty.last(), is(Optional.empty()));
			assertThat(oneOnly.last(), is(Optional.of(1)));
			assertThat(oneToTwo.last(), is(Optional.of(2)));
			assertThat(oneToThree.last(), is(Optional.of(3)));
		});
	}

	@Test
	public void fibonacci() {
		Sequence<Integer> fibonacci = Sequence.recurse(Pair.of(0, 1), p -> Pair.of(p.second(), p.apply(Integer::sum)))
		                                      .map(Pair::first);
		checkingReuse(() -> assertThat(fibonacci.limit(10), contains(0, 1, 1, 2, 3, 5, 8, 13, 21, 34)));
	}

	@Test
	public void pairs() {
		checkingReuse(() -> {
			assertThat(empty.pair(), is(emptyIterable()));
			assertThat(oneOnly.pair(), is(emptyIterable()));
			assertThat(oneToTwo.pair(), contains(Pair.of(1, 2)));
			assertThat(oneToThree.pair(), contains(Pair.of(1, 2), Pair.of(2, 3)));
			assertThat(oneToFive.pair(), contains(Pair.of(1, 2), Pair.of(2, 3), Pair.of(3, 4), Pair.of(4, 5)));
		});
	}

	@Test
	public void partition() {
		checkingReuse(() -> assertThat(oneToFive.partition(3), contains(asList(1, 2, 3), asList(2, 3, 4), asList(3, 4, 5))));
	}

	@Test
	public void step() {
		checkingReuse(() -> assertThat(oneToNine.step(3), contains(1, 4, 7)));
	}

	@Test
	public void partitionAndStep() {
		Sequence<List<Integer>> partitionAndStep = oneToFive.partition(3).step(2);
		checkingReuse(() -> assertThat(partitionAndStep, contains(asList(1, 2, 3), asList(3, 4, 5))));

		Sequence<List<Integer>> partitioned = oneToFive.partition(3);
		checkingReuse(() -> assertThat(partitioned.step(2), contains(asList(1, 2, 3), asList(3, 4, 5))));
	}

	@Test
	public void distinct() {
		Sequence<Integer> emptyDistinct = empty.distinct();
		checkingReuse(() -> assertThat(emptyDistinct, emptyIterable()));

		Sequence<Integer> oneDistinct = oneRandom.distinct();
		checkingReuse(() -> assertThat(oneDistinct, contains(17)));

		Sequence<Integer> twoDuplicatesDistinct = Sequence.of(17, 17).distinct();
		checkingReuse(() -> assertThat(twoDuplicatesDistinct, contains(17)));

		Sequence<Integer> nineDistinct = nineRandom.distinct();
		checkingReuse(() -> assertThat(nineDistinct, contains(67, 5, 43, 3, 7, 24)));
	}

	@Test
	public void sorted() {
		Sequence<Integer> emptySorted = empty.sorted();
		checkingReuse(() -> assertThat(emptySorted, emptyIterable()));

		Sequence<Integer> oneSorted = oneRandom.sorted();
		checkingReuse(() -> assertThat(oneSorted, contains(17)));

		Sequence<Integer> twoSorted = twoRandom.sorted();
		checkingReuse(() -> assertThat(twoSorted, contains(17, 32)));

		Sequence<Integer> nineSorted = nineRandom.sorted();
		checkingReuse(() -> assertThat(nineSorted, contains(3, 5, 5, 5, 7, 24, 43, 67, 67)));
	}

	@Test
	public void sortedComparator() {
		Sequence<Integer> emptySorted = empty.sorted(Comparator.reverseOrder());
		checkingReuse(() -> assertThat(emptySorted, emptyIterable()));

		Sequence<Integer> oneSorted = oneRandom.sorted(Comparator.reverseOrder());
		checkingReuse(() -> assertThat(oneSorted, contains(17)));

		Sequence<Integer> twoSorted = twoRandom.sorted(Comparator.reverseOrder());
		checkingReuse(() -> assertThat(twoSorted, contains(32, 17)));

		Sequence<Integer> nineSorted = nineRandom.sorted(Comparator.reverseOrder());
		checkingReuse(() -> assertThat(nineSorted, contains(67, 67, 43, 24, 7, 5, 5, 5, 3)));
	}

	@Test
	public void min() {
		Optional<Integer> emptyMin = empty.min(Comparator.naturalOrder());
		checkingReuse(() -> assertThat(emptyMin, is(Optional.empty())));

		Optional<Integer> oneMin = oneRandom.min(Comparator.naturalOrder());
		checkingReuse(() -> assertThat(oneMin, is(Optional.of(17))));

		Optional<Integer> twoMin = twoRandom.min(Comparator.naturalOrder());
		checkingReuse(() -> assertThat(twoMin, is(Optional.of(17))));

		Optional<Integer> nineMin = nineRandom.min(Comparator.naturalOrder());
		checkingReuse(() -> assertThat(nineMin, is(Optional.of(3))));
	}

	@Test
	public void max() {
		Optional<Integer> emptyMax = empty.max(Comparator.naturalOrder());
		checkingReuse(() -> assertThat(emptyMax, is(Optional.empty())));

		Optional<Integer> oneMax = oneRandom.max(Comparator.naturalOrder());
		checkingReuse(() -> assertThat(oneMax, is(Optional.of(17))));

		Optional<Integer> twoMax = twoRandom.max(Comparator.naturalOrder());
		checkingReuse(() -> assertThat(twoMax, is(Optional.of(32))));

		Optional<Integer> nineMax = nineRandom.max(Comparator.naturalOrder());
		checkingReuse(() -> assertThat(nineMax, is(Optional.of(67))));
	}

	@Test
	public void count() {
		checkingReuse(() -> assertThat(empty.count(), is(0)));
		checkingReuse(() -> assertThat(oneOnly.count(), is(1)));
		checkingReuse(() -> assertThat(oneToTwo.count(), is(2)));
		checkingReuse(() -> assertThat(oneToNine.count(), is(9)));
	}

	@Test
	public void any() {
		checkingReuse(() -> assertThat(oneToThree.any(x -> x > 0), is(true)));
		checkingReuse(() -> assertThat(oneToThree.any(x -> x > 2), is(true)));
		checkingReuse(() -> assertThat(oneToThree.any(x -> x > 4), is(false)));
	}

	@Test
	public void all() {
		checkingReuse(() -> assertThat(oneToThree.all(x -> x > 0), is(true)));
		checkingReuse(() -> assertThat(oneToThree.all(x -> x > 2), is(false)));
		checkingReuse(() -> assertThat(oneToThree.all(x -> x > 4), is(false)));
	}

	@Test
	public void none() {
		checkingReuse(() -> assertThat(oneToThree.none(x -> x > 0), is(false)));
		checkingReuse(() -> assertThat(oneToThree.none(x -> x > 2), is(false)));
		checkingReuse(() -> assertThat(oneToThree.none(x -> x > 4), is(true)));
	}

	@Test
	public void peek() {
		Sequence<Integer> peek = oneToThree.peek(x -> assertThat(x, is(both(greaterThan(0)).and(lessThan(4)))));
		checkingReuse(() -> assertThat(peek, contains(1, 2, 3)));
	}

	public static class MapBuilder<K, V> {
		private Map<K, V> map = new HashMap<>();

		private MapBuilder() {
		}

		public static <K, V> MapBuilder<K, V> of(K key, V value) {
			return new MapBuilder<K, V>().put(key, value);
		}

		protected MapBuilder<K, V> put(K key, V value) {
			map.put(key, value);
			return this;
		}

		public MapBuilder<K, V> and(K key, V value) {
			return put(key, value);
		}

		public Map<K, V> build() {
			Map<K, V> result = map;
			map = new HashMap<>(result);
			return result;
		}
	}
}
