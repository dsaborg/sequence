# Sequence
## A lightweight companion to the Java 8 Stream library

**NOTE: PRE-RELEASE VERSION 0.x, INTERFACE _WILL_ CHANGE UNTIL 1.0**

The Sequence library is a leaner alternative to sequential Java 8 Streams, used in similar ways but with a lighter step,
and with better integration with the rest of Java.

It aims to be roughly feature complete with sequential `Streams`, with some additional convenience methods.
In particular it allows easier collecting into common `Collections` without having to use `Collectors`,
better handling of `Maps` by use of the built-in `Pair` class which allows transformation and filtering of
`Map` `Entries` as first-class citizens, and tighter integration with pre-Java 8 `Collection` classes by being
implemented in terms of `Iterable` and `Iterators`.

```
List<String> evens = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                             .filter(x -> x % 2 == 0)
                             .map(Objects::toString)
                             .toList();

assertThat(evens, contains("2", "4", "6", "8"));
```

`Maps` are handled as `Sequences` of `Pairs` of values, with special transformation methods that convert to/from `Maps`.

```
Sequence<Integer> keys = Sequence.of(1, 2, 3);
Sequence<String> values = Sequence.of("1", "2", "3");

Map<Integer, String> map = keys.interleave(values).toMap();

assertThat(map, is(equalTo(Maps.builder(1, "1").put(2, "2").put(3, "3").build())));
```

`Sequences` use Java 8 lambdas in much the same way as `Streams` do, but is based on `Iterables` and `Iterators` instead
of a pipeline, and is built for convenience and compatibility with the rest of Java. It's for programmers wanting
to perform common data processing tasks on moderately small collections. If you need parallel iteration or Very
Large collection processing (> 2G entries) use `Streams`. If your data doesn't fit in an array you probably need
`Streams` instead. Having said that, `Sequences` go to great lengths to be as lazy and late-evaluating as possible,
with minimal overhead.

Because `Sequences` are `Iterables` you can for example use them in foreach loops, and re-use them safely after you
have already traversed them (as long as they're backed by an `Iterable`/`Collection`, not an `Iterator` or `Stream`,
of course).

```
Sequence<Integer> singulars = Sequence.recurse(1, i -> i + 1).limit(10); // Digits 1..10

// using sequence of ints 1..10 first time to get odd numbers between 1 and 10
Sequence<Integer> odds = singulars.step(2);
int x = 0, expectedOdds[] = {1, 3, 5, 7, 9};
for (int odd : odds)
    assertThat(odd, is(expectedOdds[x++]));

// re-using the same sequence again to get squares of numbers between 4 and 9
Sequence<Integer> squares = singulars.map(i -> i * i).skip(3).limit(5);
int y = 0, expectedSquares[] = {16, 25, 36, 49, 64};
for (int square : squares)
    assertThat(square, is(expectedSquares[y++]));
```

`Sequences` interoperate beautifully with `Streams`, through the expected `from(Stream)` and `.stream()` methods.

```
Stream<String> abcd = Arrays.asList("a", "b", "c", "d").stream();
Stream<String> abbccd = Sequence.from(abcd).pair().<String>flatten().stream();
assertThat(abbccd.collect(Collectors.toList()), contains("a", "b", "b", "c", "c", "d"));
```

There is full support for infinite recursive `Sequences`, including termination at a known value. Example of how to 
compute Fibonacci:

```
Sequence<Integer> fibonacci = Sequence.recurse(Pair.of(0, 1), p -> Pair.of(p.second(), p.apply(Integer::sum)))
                                      .map(Pair::first)
                                      .until(55);
assertThat(fibonacci.limit(10), contains(0, 1, 1, 2, 3, 5, 8, 13, 21, 34));
```

Go ahead give it a try, and experience a leaner way to Stream your Sequences!

Developed with [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/)!
