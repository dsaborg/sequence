# Sequence
## A lightweight companion to the Java 8 Stream library
###### Daniel Skogquist Åborg ([d2ab.org](http://www.d2ab.org/))

**NOTE: PRE-RELEASE VERSION 0.x, INTERFACE _WILL_ CHANGE UNTIL 1.0**

The Sequence library is a leaner alternative to sequential Java 8 Streams, used in similar ways but with a lighter step,
and with better integration with the rest of Java.

It aims to be roughly feature complete with sequential `Streams`, with some additional convenience methods.
In particular it allows easier collecting into common `Collections` without having to use `Collectors`,
better handling of `Maps` which allows transformation and filtering of `Map` `Entries` as first-class citizens,
and tighter integration with pre-Java 8 by being implemented in terms of `Iterable` and `Iterators`.

`Sequences` use Java 8 lambdas in much the same way as `Streams` do, but is based on `Iterables` and `Iterators` instead
of a pipeline, and is built for convenience and compatibility with the rest of Java. It's for programmers wanting
to perform common data processing tasks on moderately small collections. If you need parallel iteration use `Streams`.
`Sequences` go to great lengths to be as lazy and late-evaluating as possible, with minimal overhead.

```
List<String> evens = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                             .filter(x -> x % 2 == 0)
                             .map(Objects::toString)
                             .toList();

assertThat(evens, contains("2", "4", "6", "8"));
```

### Maps

`Maps` are handled as `Sequences` of `Entry` or `Pair`, with special transformation methods that convert 
to/from `Maps`. `Pair` implements `Entry` and provides extra transformation methods.

```
Sequence<Integer> keys = Sequence.of(1, 2, 3);
Sequence<String> values = Sequence.of("1", "2", "3");

Sequence<Pair<Integer, String>> keyValueSequence = keys.interleave(values);
Map<Integer, String> map = keyValueSequence.toMap();

assertThat(map, is(equalTo(Maps.builder(1, "1").put(2, "2").put(3, "3").build())));
```

You can also map `Entry` `Sequences` to `Pairs` which allows more expressive transformation and filtering.

```
Map<String, Integer> map = Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build();

Sequence<Pair<String, Integer>> sequence = Sequence.from(map)
                                                   .map(Pair::from)
                                                   .filter(p -> p.test((s, i) -> i != 2))
                                                   .map(p -> p.map((s, i) -> Pair.of(s + " x 2", i * 2)));

assertThat(sequence.toMap(), is(equalTo(Maps.builder("1 x 2", 2).put("3 x 2", 6).put("4 x 2", 8).build())));
```

You can also work directly on `Entry` keys and values using `EntrySequence`.

```
Map<String, Integer> original = Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build();

EntrySequence<Integer, String> oddsInverted = EntrySequence.from(original)
                                                           .filter((k, v) -> v % 2 != 0)
                                                           .map((k, v) -> Pair.of(v, k));

assertThat(oddsInverted.toMap(), is(equalTo(Maps.builder(1, "1").put(3, "3").build())));
```

### Iterable

Because `Sequences` are `Iterables` you can re-use them safely after you have already traversed them, as long as they're
backed by an `Iterable`/`Collection`, not an `Iterator` or `Stream`, of course.

```
Sequence<Integer> singulars = Sequence.ints().limit(10); // Digits 1..10

// using sequence of ints 1..10 first time to get odd numbers between 1 and 10
Sequence<Integer> odds = singulars.step(2);
assertThat(odds, contains(1, 3, 5, 7, 9));

// re-using the same sequence again to get squares of numbers between 4 and 9
Sequence<Integer> squares = singulars.map(i -> i * i).skip(3).limit(5);
assertThat(squares, contains(16, 25, 36, 49, 64));
```

Also because `Sequences` are `Iterables` they work beautifully in foreach loops:

```
Sequence<Integer> sequence = Sequence.ints().limit(3);

int x = 1;
for (int i : sequence)
    assertThat(i, is(x++));
```

Because `Sequence` is a `@FunctionalInterface` requiring only the `iterator()` method of `Iterable` to be implemented,
it's very easy to create your own full-fledged `Sequence` instances that can be operated on like any other `Sequence`
through the default methods on the interface that carry the bulk of the burden.

```
List list = Arrays.asList(1, 2, 3, 4, 5);

// Sequence as @FunctionalInterface of list's Iterator
Sequence<Integer> sequence = list::iterator;

// Operate on sequence as any other sequence using default methods
Sequence<String> transformed = sequence.map(Object::toString).limit(3);

assertThat(transformed, contains("1", "2", "3"));
```

### Streams

`Sequences` interoperate beautifully with `Streams`, through the expected `from(Stream)` and `.stream()` methods.

```
Stream<String> abcd = Arrays.asList("a", "b", "c", "d").stream();
Stream<String> abbccd = Sequence.from(abcd).pair().<String>flatten().stream();

assertThat(abbccd.collect(Collectors.toList()), contains("a", "b", "b", "c", "c", "d"));
```

### Recursion

There is full support for infinite recursive `Sequences`, including termination at a known value.

```
Sequence<Integer> fibonacci = Sequence.recurse(Pair.of(0, 1),
                                               pair -> pair.shiftedLeft(pair.apply(Integer::sum)))
                                      .map(Pair::getLeft)
                                      .until(55);

assertThat(fibonacci, contains(0, 1, 1, 2, 3, 5, 8, 13, 21, 34));
```

```
Exception e = new IllegalStateException(new IllegalArgumentException(new NullPointerException()));

Sequence<Throwable> sequence = Sequence.recurse(e, Throwable::getCause).until(null);

assertThat(sequence,
           contains(instanceOf(IllegalStateException.class), instanceOf(IllegalArgumentException.class),
                    instanceOf(NullPointerException.class)));
```

### Reduction

Also the standard reduction operations are available as per `Stream`:

```
Sequence<Long> thirteen = Sequence.recurse(1L, i -> i + 1).limit(13);
Long factorial = thirteen.reduce(1L, (r, i) -> r * i);

assertThat(factorial, is(6227020800L));
```

### Primitive

There are also primitive versions of `Sequence` for `char`, `int`, `long` and `double` processing, `Chars`, `Ints`, `Longs` and `Doubles`:

```
Chars snakeCase = Chars.from("Hello Lexicon").map(c -> (c == ' ') ? '_' : c).map(Character::toLowerCase);

assertThat(snakeCase.asString(), is("hello_lexicon"));
```

```
Ints squares = Ints.positive().map(i -> i * i);

assertThat(squares.skip(3).limit(5), contains(16, 25, 36, 49, 64));
```

```
Longs negativeOdds = Longs.negative().step(2);

assertThat(negativeOdds.skip(3).limit(5), contains(-7L, -9L, -11L, -13L, -15L));
```

```
Doubles squareRoots = Doubles.positive().limit(3).map(Math::sqrt);

assertThat(squareRoots, contains(sqrt(1), sqrt(2), sqrt(3)));
```

The primitive `Sequences` also have mapping methods that peek on the previous and next elements:

```
Chars titleCase = Chars.from("hello_lexicon")
                       .mapBack((p, c) -> (p == -1 || p == '_') ? toUpperCase(c) : c)
                       .map(c -> (c == '_') ? ' ' : c);

assertThat(titleCase.asString(), is("Hello Lexicon"));
```

### Conclusion

Go ahead and give it a try and experience a leaner way to `Stream` your `Sequences`!

Developed with [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/)!
