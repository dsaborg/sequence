# Sequence
## A lightweight alternative to Java 8 sequential Stream

##### By Daniel Skogquist Åborg ([d2ab.org](http://d2ab.org/))

* [Overview](#overview)
* [Install](#install)
* [Usage](#usage)
* [Conclusion](#conclusion)

### Overview

The Sequence library is a leaner alternative to sequential Java 8 Streams, used in similar ways but with a lighter step,
and with better integration with the rest of Java.

It aims to be roughly feature complete with sequential `Streams`, with some additional convenience methods.
In particular it allows easier collecting into common `Collections` without having to use `Collectors`,
better handling of `Maps` which allows transformation and filtering of `Map.Entry` as first-class citizens,
and tighter integration with pre-Java 8 by being implemented in terms of `Iterable` and `Iterators`. `Sequences` go to
great lengths to be as lazy and late-evaluating as possible, with minimal overhead.

`Sequences` use Java 8 lambdas in much the same way as `Streams` do, but is based on readily available `Iterables` and
`Iterators` instead of a black box pipeline, and is built for convenience and compatibility with the rest of Java. It's
for programmers wanting to perform common data processing tasks on moderately small collections. If you need parallel
iteration or are processing over 1 million or so entries, you might benefit from using a parallel `Stream` instead.

```Java
List<String> evens = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                             .filter(x -> x % 2 == 0)
                             .map(Object::toString)
                             .toList();

assertThat(evens, contains("2", "4", "6", "8"));
```

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
  <version>0.15.0</version>
</dependency>
```

#### Gradle

To install in gradle, use the maven central dependency:

```Gradle
repositories {
    jcenter()
}

dependencies {
    compile 'org.d2ab:sequence:0.15.0'
}
```

### Usage

#### Iterable

Because each `Sequence` is an `Iterable` you can re-use them safely after you have already traversed them, as long as
they're backed by an `Iterable`, `Collection` or array of course, not a once only `Iterator` or `Stream`.

```Java
Sequence<Integer> singulars = Sequence.ints().limit(10); // Digits 1..10

// using sequence of ints 1..10 first time to get odd numbers between 1 and 10
Sequence<Integer> odds = singulars.step(2);
assertThat(odds, contains(1, 3, 5, 7, 9));

// re-using the same sequence again to get squares of numbers between 4 and 9
Sequence<Integer> squares = singulars.map(i -> i * i).skip(3).limit(5);
assertThat(squares, contains(16, 25, 36, 49, 64));
```

Also because each `Sequence` is an `Iterable` they work beautifully in foreach loops:

```Java
Sequence<Integer> sequence = Sequence.ints().limit(3);

int x = 1;
for (int i : sequence)
    assertThat(i, is(x++));
```

Because `Sequence` is a `@FunctionalInterface` requiring only the `iterator()` method of `Iterable` to be implemented,
it's very easy to create your own full-fledged `Sequence` instances that can be operated on like any other `Sequence`
through the default methods on the interface that carry the bulk of the burden.

```Java
List list = Arrays.asList(1, 2, 3, 4, 5);

// Sequence as @FunctionalInterface of list's Iterator
Sequence<Integer> sequence = list::iterator;

// Operate on sequence as any other sequence using default methods
Sequence<String> transformed = sequence.map(Object::toString).limit(3);

assertThat(transformed, contains("1", "2", "3"));
```

#### Streams

`Sequence` interoperates beautifully with `Stream`, through the expected `from(Stream)` and `.stream()` methods.

```Java
Stream<String> abcd = Arrays.asList("a", "b", "c", "d").stream();
Stream<String> abbccd = Sequence.from(abcd).pairs().<String>flatten().stream();

assertThat(abbccd.collect(Collectors.toList()), contains("a", "b", "b", "c", "c", "d"));
```

#### Recursion

There is full support for infinite recursive `Sequences`, including termination at a known value.

```Java
Sequence<Integer> fibonacci = Sequence.recurse(Pair.of(0, 1), p -> p.shiftLeft(p.apply(Integer::sum)))
                                      .map(Pair::getLeft)
                                      .endingAt(34);

assertThat(fibonacci, contains(0, 1, 1, 2, 3, 5, 8, 13, 21, 34));
```

```Java
Exception e = new IllegalStateException(new IllegalArgumentException(new NullPointerException()));

Sequence<Throwable> sequence = Sequence.recurse(e, Throwable::getCause).untilNull();

assertThat(sequence,
           contains(instanceOf(IllegalStateException.class), instanceOf(IllegalArgumentException.class),
                    instanceOf(NullPointerException.class)));
```

#### Reduction

The standard reduction operations are available as per `Stream`:

```Java
Sequence<Long> thirteen = Sequence.recurse(1L, i -> i + 1).limit(13);
Long factorial = thirteen.reduce(1L, (r, i) -> r * i);

assertThat(factorial, is(6227020800L));
```

#### Pairs

When iterating over sequences of `Pairs` of item, `BiSequence` provides native operators and transformations:

```Java
BiSequence<String, Integer> presidents = BiSequence.ofPairs("Abraham Lincoln", 1861, "Richard Nixon", 1969,
                                                            "George Bush", 2001, "Barack Obama", 2005);

Sequence<String> joinedOffice = presidents.toSequence((n, y) -> n + " (" + y + ")");

assertThat(joinedOffice, contains("Abraham Lincoln (1861)", "Richard Nixon (1969)", "George Bush (2001)",
                                  "Barack Obama (2005)"));
```

#### Maps

`Maps` are handled as `Sequences` of `Entry`, with special transformation methods that convert to/from `Maps`.

```Java
Sequence<Integer> keys = Sequence.of(1, 2, 3);
Sequence<String> values = Sequence.of("1", "2", "3");

Sequence<Pair<Integer, String>> keyValueSequence = keys.interleave(values);
Map<Integer, String> map = keyValueSequence.toMap();

assertThat(map, is(equalTo(Maps.builder(1, "1").put(2, "2").put(3, "3").build())));
```

You can also map `Entry` `Sequences` to `Pairs` which allows more expressive transformation and filtering.

```Java
Map<String, Integer> map = Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build();

Sequence<Pair<String, Integer>> sequence = Sequence.from(map)
                                                   .map(Pair::from)
                                                   .filter(p -> p.test((s, i) -> i != 2))
                                                   .map(p -> p.map((s, i) -> Pair.of(s + " x 2", i * 2)));

assertThat(sequence.toMap(), is(equalTo(Maps.builder("1 x 2", 2).put("3 x 2", 6).put("4 x 2", 8).build())));
```

You can also work directly on `Entry` keys and values using `EntrySequence`.

```Java
Map<String, Integer> original = Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build();

EntrySequence<Integer, String> oddsInverted = EntrySequence.from(original)
                                                           .filter((k, v) -> v % 2 != 0)
                                                           .map((k, v) -> Pair.of(v, k));

assertThat(oddsInverted.toMap(), is(equalTo(Maps.builder(1, "1").put(3, "3").build())));
```

#### Primitive

There are also primitive versions of `Sequence` for `char`, `int`, `long` and `double` processing, `CharSeq`,
`IntSequence`, `LongSequence` and `DoubleSequence`:

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

The primitive `Sequences` also have mapping methods that peek on the previous and next elements:

```Java
CharSeq titleCase = CharSeq.from("hello_lexicon")
                           .mapBack((p, c) -> (p == -1 || p == '_') ? toUpperCase(c) : c)
                           .map(c -> (c == '_') ? ' ' : c);

assertThat(titleCase.asString(), is("Hello Lexicon"));
```

#### Partitioning

Both regular and primitive `Sequences` have advanced windowing and partitioning methods, allowing you to divide up
`Sequences` in various ways, including a partitioning method that uses a `BiPredicate` to determine which two
elements to create a batch between.

```Java
CharSeq word = CharSeq.from("terrain");
String vowels = "aeoiuy";

Sequence<String> consonantsVowels = word.batch((a, b) -> (vowels.indexOf(a) == -1) != (vowels.indexOf(b) == -1))
                                        .map(CharSeq::asString);

assertThat(consonantsVowels, contains("t", "e", "rr", "ai", "n"));
```

#### Updating

`Sequences` have full support for updating the underlying collection, where possible, both through `Iterator#remove()`
and by modifying the underlying collection directly in between iterations.

```Java
List<Integer> list = new ArrayList<>(asList(1, 2, 3, 4, 5));

Sequence.from(list).filter(x -> x % 2 != 0).removeAll();

assertThat(list, contains(2, 4));
```

```Java
List<Integer> list = new ArrayList<>(asList(1, 2, 3, 4, 5));

Sequence<Integer> sequence = Sequence.from(list);

list.add(6);

assertThat(sequence, contains(1, 2, 3, 4, 5, 6));
```

### Conclusion

Go ahead and give it a try and experience a leaner way to `Stream` your `Sequences`! :bowtie:

Developed with [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/)! :heart:
