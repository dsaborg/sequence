package org.da2ab.sequence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class Pair<A, B> {
	@Nullable
	private final A first;
	@Nullable
	private final B second;

	private Pair(@Nullable A first, @Nullable B second) {
		this.first = first;
		this.second = second;
	}

	public static <K, V> Pair<K, V> from(Entry<? extends K, ? extends V> entry) {
		return of(entry.getKey(), entry.getValue());
	}

	public static <A, B> Pair<A, B> of(@Nullable A a, @Nullable B b) {
		return new Pair<>(a, b);
	}

	public A first() {
		return first;
	}

	public B second() {
		return second;
	}

	@Nonnull
	public <C, D> Pair<C, D> map(@Nonnull Function<? super A, ? extends C> firstMapper, @Nonnull Function<? super B, ? extends D> secondMapper) {
		return of(firstMapper.apply(first), secondMapper.apply(second));
	}

	@Nonnull
	public <C, D> Pair<C, D> map(@Nonnull BiFunction<? super A, ? super B, ? extends Pair<C, D>> mapper) {
		return mapper.apply(first, second);
	}

	public <T> T apply(@Nonnull BiFunction<? super A, ? super B, ? extends T> function) {
		return function.apply(first, second);
	}

	public boolean test(@Nonnull Predicate<? super A> firstPredicate, @Nonnull Predicate<? super B> secondPredicate) {
		return firstPredicate.test(first) && secondPredicate.test(second);
	}

	public boolean test(@Nonnull BiPredicate<? super A, ? super B> predicate) {
		return predicate.test(first, second);
	}

	public Map<A, B> put(@Nonnull Map<A, B> map) {
		map.put(first, second);
		return map;
	}

	@Override
	public int hashCode() {
		int result = first != null ? first.hashCode() : 0;
		result = 31 * result + (second != null ? second.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Pair<?, ?> pair = (Pair<?, ?>) o;

		if (first != null ? !first.equals(pair.first) : pair.first != null)
			return false;
		return second != null ? second.equals(pair.second) : pair.second == null;
	}

	@Override
	public String toString() {
		return "(" + first + ',' + second + ')';
	}
}
