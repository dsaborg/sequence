package org.d2ab.collection.chars;

import org.d2ab.function.CharConsumer;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A primitive specialization of {@link Spliterator} for {@code char} values.
 */
public interface CharSpliterator extends Spliterator.OfPrimitive<Character, CharConsumer, CharSpliterator> {
	@Override
	CharSpliterator trySplit();

	@Override
	boolean tryAdvance(CharConsumer action);

	@Override
	default void forEachRemaining(CharConsumer action) {
		//noinspection StatementWithEmptyBody
		while (tryAdvance(action)) {
			// keep going
		}
	}

	@Override
	default boolean tryAdvance(Consumer<? super Character> action) {
		if (action instanceof CharConsumer)
			return tryAdvance((CharConsumer) action);
		else
			return tryAdvance((CharConsumer) action::accept);
	}

	@Override
	default void forEachRemaining(Consumer<? super Character> action) {
		if (action instanceof CharConsumer)
			forEachRemaining((CharConsumer) action);
		else
			forEachRemaining((CharConsumer) action::accept);
	}
}
