# Sequence
## A lightweight alternative to Java 8 sequential Stream

##### By Daniel Skogquist Åborg ([d2ab.org](http://d2ab.org/))

Follow me on Twitter ([@daniel2aborg](http://twitter.com/daniel2aborg)) to receive updates about Sequence.

Your feedback is welcome! For comments, feature requests or bug reports,
use the [GitHub Issues Page](https://github.com/d2ab/sequence/issues),
email me at [daniel@d2ab.org](mailto:daniel@d2ab.org),
or send me a message on [twitter](http://twitter.com/daniel2aborg).

Javadoc for the entire project is available at the [Sequence javadoc.io Page](http://www.javadoc.io/doc/org.d2ab/sequence).

* [News](#news)
* [Overview](#overview)
* [Install](#install)
* [Javadoc](#javadoc)
* [Usage](#usage)
* [Conclusion](#conclusion)

### News

**2016-04-25:** Sequence v1.1.1 is released, with
[updated javadoc](http://www.javadoc.io/doc/org.d2ab/sequence/1.1.1),
[dedicated concatenation methods](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#concat-java.lang.Iterable...-),
and [enhanced conversion to primitives](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/BiSequence.html#toChars-org.d2ab.function.chars.ToCharBiFunction-).

**2016-04-23:** Sequence v1.1 is released, with
[caching](http://static.javadoc.io/org.d2ab/sequence/1.1/org/d2ab/sequence/Sequence.html#cache-java.util.Iterator-),
[splitting](http://static.javadoc.io/org.d2ab/sequence/1.1/org/d2ab/sequence/Sequence.html#split-T-),
[skipping at tail](http://static.javadoc.io/org.d2ab/sequence/1.1/org/d2ab/sequence/Sequence.html#skipTail-long-),
[starting in middle](http://static.javadoc.io/org.d2ab/sequence/1.1/org/d2ab/sequence/Sequence.html#startingFrom-T-)
and [emptiness check](http://static.javadoc.io/org.d2ab/sequence/1.1/org/d2ab/sequence/Sequence.html#isEmpty--).
The Sequence test suite is now at over 1000 tests!

### Overview

The Sequence library is a leaner alternative to sequential Java 8 Streams, used in similar ways but with a lighter step,
and with better integration with the rest of Java.

It aims to be roughly feature complete with sequential `Streams`, with additional convenience methods for advanced
traversal and transformation. In particular it allows easier collecting into common `Collections` without `Collectors`,
better handling of `Maps` with `Pairs` and `Map.Entry` as first-class citizens, tighter integration with the rest of
Java by being implemented in terms of `Iterable`, and advanced partitioning, mapping and filtering methods, for example
allowing you to peek at previous or next elements to make decisions during traversal. `Sequences` go to great lengths
to be as lazy and late-evaluating as possible, with minimal overhead.

`Sequences` use Java 8 lambdas in much the same way as `Streams` do, but is based on readily available `Iterables`
instead of a black box pipeline, and is built for convenience and compatibility with the rest of Java. It's
for programmers wanting to perform common data processing tasks on moderately sized collections. If you need parallel
iteration or are processing over 1 million or so entries, you might benefit from using a parallel `Stream` instead.

The `Sequence` library is protected by over 1000 tests for your safety.

```Java
List<String> evens = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                             .filter(x -> x % 2 == 0)
                             .map(Object::toString)
                             .toList();

assertThat(evens, contains("2", "4", "6", "8"));
```

See also:
[Sequence#of(T...)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#of-T...-),
[Sequence#from(Iterable)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#from-java.lang.Iterable-),
[Sequence#filter(Predicate)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#filter-java.util.function.Predicate-),
[Sequence#map(Function)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#map-java.util.function.Function-),
[Sequence#toList()](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#toList--)

### Install

The Sequence library is available for manual install or as a maven central dependency for maven and gradle.

#### Manual

For manually installable releases, check out the [GitHub Releases Page](https://github.com/d2ab/sequence/releases).

#### Maven

To install in maven, use the maven central dependency:

```Maven
<dependency>
  <groupId>org.d2ab</groupId>
  <artifactId>sequence</artifactId>
  <version>1.1.1</version>
</dependency>
```

#### Gradle

To install in gradle, use the maven central dependency:

```Gradle
repositories {
    jcenter()
}

dependencies {
    compile 'org.d2ab:sequence:1.1.1'
}
```

### Javadoc

Javadoc for the entire project is available at the [Sequence javadoc.io Page](http://www.javadoc.io/doc/org.d2ab/sequence).

The main Sequence package is
[org.d2ab.sequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/package-summary.html)
where all the sequences reside.
There are seven kinds of Sequences, each dealing with a different type of entry. The first is the regular
[Sequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/Sequence.html)
which is the general purpose stream of items.
[EntrySequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/EntrySequence.html)
and
[BiSequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/BiSequence.html)
work directly on the constituent components of
[Map.Entry](https://docs.oracle.com/javase/8/docs/api/java/util/Map.Entry.html) and
[Pair](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/util/Pair.html)
objects. The last four are primitive sequences dealing with `char`, `int`, `long` and `double` primitives;
[CharSeq](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/CharSeq.html),
[IntSequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/IntSequence.html),
[LongSequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/LongSequence.html), and
[DoubleSequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/DoubleSequence.html).
These work much the same as the regular
[Sequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/Sequence.html)
except they're adapted to work directly on primitives.

* [Sequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/Sequence.html)
* [BiSequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/BiSequence.html)
* [EntrySequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/EntrySequence.html)
* [CharSeq](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/CharSeq.html)
* [IntSequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/IntSequence.html)
* [LongSequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/LongSequence.html)
* [DoubleSequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/index.html?org/d2ab/sequence/DoubleSequence.html)

### Usage

#### Iterable

Because each `Sequence` is an `Iterable` you can re-use them safely after you have already traversed them, as long as
they're not backed by an `Iterator` or `Stream` which can only be traversed once.

```Java
Sequence<Integer> singulars = Sequence.range(1, 9); // Digits 1..9

// using sequence of ints 1..9 first time to get odd numbers between 1 and 9
Sequence<Integer> odds = singulars.step(2);
assertThat(odds, contains(1, 3, 5, 7, 9));

// re-using the same sequence again to get squares of numbers between 4 and 8
Sequence<Integer> squares = singulars.startingFrom(4).endingAt(8).map(i -> i * i);
assertThat(squares, contains(16, 25, 36, 49, 64));
```

See also:
[Sequence#range(int, int)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#range-int-int-),
[Sequence#ints()](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#ints--),
[Sequence#intsFromZero()](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#intsFromZero--),
[Sequence#filter(Predicate)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#filter-java.util.function.Predicate-),
[Sequence#map(Function)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#map-java.util.function.Function-),
[Sequence#step(long)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#step-long-),
[Sequence#limit(long)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#limit-long-),
[Sequence#skip(long)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#skip-long-),
[Sequence#startingFrom(T)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#startingFrom-T-),
[Sequence#endingAt(T)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#endingAt-T-)

#### Foreach

Also because each `Sequence` is an `Iterable` they work beautifully in foreach loops:

```Java
Sequence<Integer> sequence = Sequence.ints().limit(5);

int expected = 1;
for (int each : sequence)
    assertThat(each, is(expected++));

assertThat(expected, is(6));
```

#### FunctionalInterface

Because `Sequence` is a `@FunctionalInterface` requiring only the `iterator()` method of `Iterable` to be implemented,
it's very easy to create your own full-fledged `Sequence` instances that can be operated on like any other `Sequence`
through the default methods on the interface that carry the bulk of the burden. In fact, this is how `Sequence's` own
factory methods work. You could consider all of `Sequence` to be a smarter version of `Iterable`.

```Java
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

// Sequence as @FunctionalInterface of list's Iterator
Sequence<Integer> sequence = list::iterator;

// Operate on sequence as any other sequence using default methods
Sequence<String> transformed = sequence.map(Object::toString);

assertThat(transformed.limit(3), contains("1", "2", "3"));
```

#### Caching

Sequences can be created from `Iterators` or `Streams` but can then only be passed over once.

```Java
Iterator<Integer> iterator = Arrays.asList(1, 2, 3, 4, 5).iterator();

Sequence<Integer> sequence = Sequence.once(iterator);

assertThat(sequence, contains(1, 2, 3, 4, 5));
assertThat(sequence, is(emptyIterable()));
```

See also:
[Sequence#once(Iterator)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#once-java.util.Iterator-),
[Sequence#once(Stream)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#once-java.util.stream.Stream-)

If you have an `Iterator` or `Stream` and wish to convert it to a full-fledged multi-iterable `Sequence`, use the
caching methods on `Sequence`.

```Java
Iterator<Integer> iterator = Arrays.asList(1, 2, 3, 4, 5).iterator();

Sequence<Integer> cached = Sequence.cache(iterator);

assertThat(cached, contains(1, 2, 3, 4, 5));
assertThat(cached, contains(1, 2, 3, 4, 5));
```

See also:
[Sequence#cache(Iterable)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#cache-java.lang.Iterable-),
[Sequence#cache(Iterator)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#cache-java.util.Iterator-),
[Sequence#cache(Stream)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#cache-java.util.stream.Stream-)

#### Updating

`Sequences` have full support for updating the underlying collection where possible, both through `Iterator#remove()`
and by modifying the underlying collection directly in between iterations.

```Java
List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

Sequence.from(list).filter(x -> x % 2 != 0).clear();

assertThat(list, contains(2, 4));
```

```Java
List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

Sequence<Integer> sequence = Sequence.from(list).filter(x -> x % 2 == 0);
assertThat(sequence, contains(2, 4));

list.add(6);
assertThat(sequence, contains(2, 4, 6));
```

See also:
[Sequence#removeAll()](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#removeAll--)

#### Streams

`Sequences` interoperate beautifully with `Stream`, through the `once(Stream)` and `.stream()` methods.

```Java
Sequence<String> paired = Sequence.once(Stream.of("a", "b", "c", "d")).pairs().flatten();

assertThat(paired.stream().collect(Collectors.toList()), contains("a", "b", "b", "c", "c", "d"));
```

See also:
[Sequence#once(Stream)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#once-java.util.stream.Stream-),
[Sequence#cache(Stream)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#cache-java.util.stream.Stream-),
[Sequence#stream()](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#stream--)

#### Recursion

There is full support for infinite recursive `Sequences`, including termination at a known value.

```Java
Sequence<Integer> fibonacci = BiSequence.recurse(0, 1, (i, j) -> Pair.of(j, i + j))
                                        .toSequence((i, j) -> i)
                                        .endingAt(34);

assertThat(fibonacci, contains(0, 1, 1, 2, 3, 5, 8, 13, 21, 34));
```

```Java
Exception exception = new IllegalStateException(new IllegalArgumentException(new NullPointerException()));

Sequence<Throwable> exceptionAndCauses = Sequence.recurse(exception, Throwable::getCause).untilNull();

assertThat(exceptionAndCauses, contains(instanceOf(IllegalStateException.class),
                                        instanceOf(IllegalArgumentException.class),
                                        instanceOf(NullPointerException.class)));

exceptionAndCauses.last(IllegalArgumentException.class).ifPresent(Throwable::printStackTrace);
```

```Java
Iterator<String> delimiter = Sequence.of("").append(Sequence.of(", ").repeat()).iterator();

StringBuilder joined = new StringBuilder();
for (String number : Arrays.asList("One", "Two", "Three"))
    joined.append(delimiter.next()).append(number);

assertThat(joined.toString(), is("One, Two, Three"));
```

See also:
[BiSequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/BiSequence.html),
[BiSequence#recurse(L, R, BiFunction)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/BiSequence.html#recurse-L-R-java.util.function.BiFunction-),
[BiSequence#toSequence(BiFunction)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/BiSequence.html#toSequence-java.util.function.BiFunction-),
[Pair](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/util/Pair.html),
[Pair#of(T, U)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/util/Pair.html#of-T-U-),
[Sequence#recurse(T, UnaryOperator)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#recurse-T-java.util.function.UnaryOperator-),
[Sequence#generate(Supplier)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#generate-java.util.function.Supplier-),
[Sequence#repeat()](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#repeat--),
[Sequence#repeat(long)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#repeat-long-),
[Sequence#until(T)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#until-T-),
[Sequence#endingAt(T)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#endingAt-T-),
[Sequence#untilNull(T)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#untilNull--),
[Sequence#until(Predicate)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#until-java.util.function.Predicate-),
[Sequence#endingAt(Predicate)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#endingAt-java.util.function.Predicate-)

#### Reduction

The standard reduction operations are available as per `Stream`:

```Java
Sequence<Long> thirteen = Sequence.longs().limit(13);

long factorial = thirteen.reduce(1L, (r, i) -> r * i);

assertThat(factorial, is(6227020800L));
```

See also:
[Sequence#reduce(BinaryOperator)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#reduce-java.util.function.BinaryOperator-),
[Sequence#reduce(T, BinaryOperator)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#reduce-T-java.util.function.BinaryOperator-)

#### Maps

`Maps` are handled as `Sequences` of `Entry`, with special transformation methods that convert to/from `Maps`.

```Java
Sequence<Integer> keys = Sequence.of(1, 2, 3);
Sequence<String> values = Sequence.of("1", "2", "3");

Map<Integer, String> map = keys.interleave(values).toMap();

assertThat(map, is(equalTo(Maps.builder(1, "1").put(2, "2").put(3, "3").build())));
```

See also:
[Sequence#interleave(Iterable)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#interleave-java.lang.Iterable-),
[Sequence#pairs()](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#pairs--),
[Sequence#adjacentPairs()](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#adjacentPairs--),
[Pair](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/util/Pair.html),
[Sequence#toMap()](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#toMap--),
[Sequence#toMap(Function, Function)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#toMap-java.util.function.Function-java.util.function.Function-),
[Sequence#toSortedMap()](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#toSortedMap--),
[Sequence#toSortedMap(Function, Function)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#toSortedMap-java.util.function.Function-java.util.function.Function-)

You can also map `Entry` `Sequences` to `Pairs` which allows more expressive transformation and filtering.

```Java
Map<String, Integer> map = Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build();

Sequence<Pair<String, Integer>> sequence = Sequence.from(map)
                                                   .map(Pair::from)
                                                   .filter(p -> p.test((s, i) -> i != 2))
                                                   .map(p -> p.map((s, i) -> Pair.of(s + " x 2", i * 2)));

assertThat(sequence.toMap(), is(equalTo(Maps.builder("1 x 2", 2).put("3 x 2", 6).put("4 x 2", 8).build())));
```

See also:
[Pair](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/util/Pair.html),
[Pair#of(T, U)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/util/Pair.html#of-T-U-),
[Pair#from(Entry)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/util/Pair.html#from-java.util.Map.Entry-),
[Pair#test(BiPredicate)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/util/Pair.html#test-java.util.function.BiPredicate-),
[Pair#map(BiFunction)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/util/Pair.html#map-java.util.function.BiFunction-)

You can also work directly on `Entry` keys and values using `EntrySequence`.

```Java
Map<String, Integer> original = Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build();

EntrySequence<Integer, String> oddsInverted = EntrySequence.from(original)
                                                           .filter((k, v) -> v % 2 != 0)
                                                           .map((k, v) -> Maps.entry(v, k));

assertThat(oddsInverted.toMap(), is(equalTo(Maps.builder(1, "1").put(3, "3").build())));
```

See also:
[EntrySequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/EntrySequence.html),
[EntrySequence#from(Map)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/EntrySequence.html#from-java.util.Map-),
[EntrySequence#filter(BiPredicate)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/EntrySequence.html#filter-java.util.function.BiPredicate-),
[EntrySequence#map(BiFunction)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/EntrySequence.html#map-java.util.function.BiFunction-),
[EntrySequence#toSequence(BiFunction)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/EntrySequence.html#toSequence-java.util.function.BiFunction-),
[Maps#entry(K, V)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/collection/Maps.html#entry-K-V-),
[EntrySequence#toMap()](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/EntrySequence.html#toMap--)

#### Pairs

When iterating over sequences of `Pairs` of item, `BiSequence` provides native operators and transformations:

```Java
BiSequence<String, Integer> presidents = BiSequence.ofPairs("Abraham Lincoln", 1861, "Richard Nixon", 1969,
                                                            "George Bush", 2001, "Barack Obama", 2005);

Sequence<String> joinedOffice = presidents.toSequence((n, y) -> n + " (" + y + ")");

assertThat(joinedOffice, contains("Abraham Lincoln (1861)", "Richard Nixon (1969)", "George Bush (2001)",
                                  "Barack Obama (2005)"));
```

See also:
[BiSequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/BiSequence.html),
[BiSequence#from(Map)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/BiSequence.html#from-java.util.Map-),
[BiSequence#filter(BiPredicate)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/BiSequence.html#filter-java.util.function.BiPredicate-),
[BiSequence#map(BiFunction)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/BiSequence.html#map-java.util.function.BiFunction-),
[BiSequence#toSequence(BiFunction)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/BiSequence.html#toSequence-java.util.function.BiFunction-),
[BiSequence#toMap()](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/BiSequence.html#toMap--)

#### Primitive

There are also primitive versions of `Sequence` for `char`, `int`, `long` and `double` processing: `CharSeq`,
`IntSequence`, `LongSequence` and `DoubleSequence`.

```Java
CharSeq snakeCase = CharSeq.from("Hello Lexicon").map(c -> (c == ' ') ? '_' : c).map(Character::toLowerCase);

assertThat(snakeCase.asString(), is("hello_lexicon"));
```

```Java
IntSequence squares = IntSequence.positive().map(i -> i * i);

assertThat(squares.limit(5), contains(1, 4, 9, 16, 25));
```

```Java
LongSequence negativeOdds = LongSequence.negative().step(2);

assertThat(negativeOdds.limit(5), contains(-1L, -3L, -5L, -7L, -9L));
```

```Java
DoubleSequence squareRoots = IntSequence.positive().toDoubles().map(Math::sqrt);

assertThat(squareRoots.limit(3), contains(sqrt(1), sqrt(2), sqrt(3)));
```

See also:
[CharSeq](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/CharSeq.html),
[IntSequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/IntSequence.html),
[LongSequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/LongSequence.html),
[DoubleSequence](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/DoubleSequence.html)
[Sequence#toChars(ToCharFunction)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#toChars-org.d2ab.function.chars.ToCharFunction-)
[Sequence#toInts(ToIntFunction)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#toInts-java.util.function.ToIntFunction-)
[Sequence#toLongs(ToLongFunction)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#toLongs-java.util.function.ToLongFunction-)
[Sequence#toDoubles(ToDoubleFunction)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#toDoubles-java.util.function.ToDoubleFunction-)

#### Peeking

`Sequences` also have mapping and filtering methods that peek on the previous and next elements:

```Java
CharSeq titleCase = CharSeq.from("hello_lexicon")
                           .mapBack('_', (p, c) -> p == '_' ? toUpperCase(c) : c)
                           .map(c -> (c == '_') ? ' ' : c);

assertThat(titleCase.asString(), is("Hello Lexicon"));
```

See also:
[Sequence#peekBack(BiConsumer)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#peekBack-java.util.function.BiConsumer-),
[Sequence#peekForward(BiConsumer)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#peekForward-java.util.function.BiConsumer-),
[Sequence#filterBack(BiPredicate)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#filterBack-java.util.function.BiPredicate-),
[Sequence#filterForward(BiPredicate)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#filterForward-java.util.function.BiPredicate-),
[Sequence#mapBack(BiFunction)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#mapBack-java.util.function.BiFunction-),
[Sequence#mapForward(BiFunction)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#mapForward-java.util.function.BiFunction-)

#### Partitioning

Both regular and primitive `Sequences` have advanced windowing and partitioning methods, allowing you to divide up
`Sequences` in various ways, including a partitioning method that uses a `BiPredicate` to determine which two
elements to create a batch between.

```Java
Sequence<Sequence<Integer>> batched = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9).batch(3);

assertThat(batched, contains(contains(1, 2, 3), contains(4, 5, 6), contains(7, 8, 9)));
```

```Java
String vowels = "aeoiuy";

Sequence<String> consonantsVowels = CharSeq.from("terrain")
                                           .batch((a, b) -> (vowels.indexOf(a) == -1) !=
                                                            (vowels.indexOf(b) == -1))
                                           .map(CharSeq::asString);

assertThat(consonantsVowels, contains("t", "e", "rr", "ai", "n"));
```

See also:
[Sequence#window(int)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#window-int-),
[Sequence#window(int, int)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#window-int-int-),
[Sequence#batch(int)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#batch-int-),
[Sequence#batch(BiPredicate)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#batch-java.util.function.BiPredicate-),
[Sequence#split(T)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#split-T-),
[Sequence#split(Predicate)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/Sequence.html#split-java.util.function.Predicate-)

#### Reading

Primitive sequences can be read from `Readers` or `InputStreams` into a `CharSeq` or `IntSequence` respective. These
can also be converted back to `Readers` and `InputStreams` respectively, allowing for filtering or transformation of
these streams.

```Java
Reader reader = new StringReader("hello world\ngoodbye world\n");

Sequence<String> titleCase = CharSeq.read(reader)
                                    .mapBack('\n',
                                             (p, n) -> p == '\n' || p == ' ' ?
                                                       Character.toUpperCase(n) : n)
                                    .split('\n')
                                    .map(phrase -> phrase.append('!'))
                                    .map(CharSeq::asString);

assertThat(titleCase, contains("Hello World!", "Goodbye World!"));

reader.close();
```

```Java
Reader original = new StringReader("hello world\ngoodbye world\n");

BufferedReader transformed = new BufferedReader(CharSeq.read(original).map(Character::toUpperCase).asReader());

assertThat(transformed.readLine(), is("HELLO WORLD"));
assertThat(transformed.readLine(), is("GOODBYE WORLD"));

transformed.close();
original.close();
```

```Java
InputStream inputStream = new ByteArrayInputStream(new byte[]{0xD, 0xE, 0xA, 0xD, 0xB, 0xE, 0xE, 0xF});

String hexString = IntSequence.read(inputStream)
                              .toSequence(Integer::toHexString)
                              .map(String::toUpperCase)
                              .join();

assertThat(hexString, is("DEADBEEF"));

inputStream.close();
```

See also:
[CharSeq#read(Reader)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/CharSeq.html#read-java.io.Reader-),
[IntSequence#read(InputStream)](http://static.javadoc.io/org.d2ab/sequence/1.1.1/org/d2ab/sequence/IntSequence.html#read-java.io.InputStream-)

### Conclusion

Go ahead and give it a try and experience a leaner way to `Stream` your `Sequences`! :bowtie:

Developed with [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/). :heart:
