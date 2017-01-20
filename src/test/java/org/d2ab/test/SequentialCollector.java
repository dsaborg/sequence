package org.d2ab.test;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Collections.emptySet;

public class SequentialCollector<T, A, R> implements Collector<T, A, R> {
	private final Supplier<A> supplier;
	private final BiConsumer<A, T> accumulator;
	private final Function<A, R> finisher;

	public SequentialCollector(Supplier<A> supplier, BiConsumer<A, T> accumulator, Function<A, R> finisher) {
		this.supplier = supplier;
		this.accumulator = accumulator;
		this.finisher = finisher;
	}

	@Override
	public Supplier<A> supplier() {
		return supplier;
	}

	@Override
	public BiConsumer<A, T> accumulator() {
		return accumulator;
	}

	@Override
	public BinaryOperator<A> combiner() {
		return null;
	}

	@Override
	public Function<A, R> finisher() {
		return finisher;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return emptySet();
	}
}
