# Sequence
## A lightweight companion to the Java 8 Stream library

The Sequence library is a leaner alternative to sequential Java 8 Streams, used in similar ways but with a lighter step, and with
better integration with the rest of Java.

It aims to be roughly feature complete with sequential `Streams`, with some additional convenience methods.
In particular it allows easier collecting into common `Collections` without having to use `Collectors`,
better handling of `Maps` by use of the built-in `Pair` class which allows transformation and filtering of
`Map` `Entries` as first-class citizens,
and tighter integration with pre-Java 8 `Collection` classes by being implemented in terms of `Iterable` and `Iterators`.

```
List<String> evens = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                             .filter(x -> x % 2 == 0)
                             .map(Objects::toString)
                             .toList();

assertThat(evens, contains("2", "4", "6", "8"));
```

```
Map<String, Integer> countsByName =...

Map<Person, Statistics> statisticsByPerson = Sequence.from(countsByName)
                                                     .filter(pair -> pair.second() > 1000)
                                                     .map(pair -> pair.map(Person::new, Statistics::new))
                                                     .pairsToMap(Function.identity());

statisticsByPerson.get("John Doe").setCount(732);
```

It uses Java 8 lambdas in much the same way as Streams do, but is based on Iterables and Iterators instead of a custom pipeline framework,
and is built for convenience and integration, which makes it interoperate with the rest of Java beautifully. It's a framework
for programmers to perform common programming tasks on moderately small collections. If you need parallel iteration use
Streams.

Because Sequences are Iterables you can for example use Sequences in foreach loops, and re-use them safely
AFTER you have already traversed them (as long as they're backed by an Iterable/Collection, not an Iterator or Stream, of course).
They also integrate fine with Mockito's assertThat and hamcrest matchers checking Iterables, and all the tests are written
in terms of Iterable traversal using e.g. `Matchers.contains(...)``.

```
Sequence<Integer> singulars = Sequence.recurse(1, i -> i + 1).limit(10);

// using sequence of ints 1..10 first time to get odd numbers between 1 and 10
int x = 0, odds[] = {1, 3, 5, 7, 9};
for (int odd : singulars.step(2))
    assertThat(odd, is(odds[x++]));

// re-using the same sequence again to get squares of numbers between 4 and 9
int y = 0, squares[] = {16, 25, 36, 49, 64};
for (int square : singulars.map(i -> i * i).skip(3).limit(5))
    assertThat(square, is(squares[y++]));
```

Naturally no documentation of a sequential processing library would be complete without an example of how to compute fibonacci in it:

```
Sequence<Integer> fibonacci = Sequence.recurse(Pair.of(0, 1), p -> Pair.of(p.second(), p.apply(Integer::sum)))
                                      .map(Pair::first);
assertThat(fibonacci.limit(10), contains(0, 1, 1, 2, 3, 5, 8, 13, 21, 34));
```
Please go ahead and give it a try, and experience a leaner way to Stream your Sequences!
