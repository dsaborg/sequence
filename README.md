The Sequence library is a lightweight alternative to the Java 8 Stream library, meant to be used in similar ways, but with a leaner,
lighter step.

Use it in the same way as you would use Streams, e.g.:

List<String> evens = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
  .filter(x -> x % 2 != 0)
  .map(Objects::toString)
  .toList(); // List of "2", "4", "6", "8"
  
Sequence provides som additional convenience methods to Stream, in particular easier collecting into common collections without having
to use collect, and better handling of Maps by use of the built-in Pair class which allows transformation and filtering of Map entries
as first-class Sequence citizens.
