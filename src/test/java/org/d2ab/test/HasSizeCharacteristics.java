/*  Copyright (c) 2000-2006 hamcrest.org
 */
package org.d2ab.test;

import org.d2ab.collection.SizedIterable;
import org.d2ab.collection.SizedIterable.SizeType;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import static org.d2ab.collection.SizedIterable.SizeType.*;
import static org.d2ab.test.IsIterableBeginningWith.beginsWith;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

public class HasSizeCharacteristics extends BaseMatcher<SizedIterable<?>> {
	private final SizeType sizeType;
	private final int size;
	private final boolean isEmpty;

	public static Matcher<SizedIterable<?>> emptySizedIterable() {
		return Matchers.<SizedIterable<?>>both(emptyIterable()).and(hasKnownSize(0));
	}

	public static Matcher<SizedIterable<?>> emptyUnsizedIterable() {
		return Matchers.<SizedIterable<?>>both(emptyIterable()).and(hasComputedSize(0));
	}

	@SafeVarargs
	public static <T> Matcher<SizedIterable<? extends T>> containsSized(T... items) {
		return Matchers.<SizedIterable<? extends T>>both(contains(items)).and(hasKnownSize(items.length));
	}

	@SafeVarargs
	public static <T> Matcher<SizedIterable<? extends T>> containsSized(Matcher<? super T>... itemMatchers) {
		return Matchers.<SizedIterable<? extends T>>both(contains(itemMatchers))
				.and(hasKnownSize(itemMatchers.length));
	}

	@SafeVarargs
	public static <T> Matcher<SizedIterable<? extends T>> containsUnsized(T... items) {
		return Matchers.<SizedIterable<? extends T>>both(contains(items)).and(hasComputedSize(items.length));
	}

	@SafeVarargs
	public static <T> Matcher<SizedIterable<? extends T>> containsUnsized(Matcher<? super T>... itemMatchers) {
		return Matchers.<SizedIterable<? extends T>>both(contains(itemMatchers))
				.and(hasComputedSize(itemMatchers.length));
	}

	@SafeVarargs
	public static <T> Matcher<SizedIterable<? extends T>> infiniteBeginningWith(T... items) {
		return Matchers.<SizedIterable<? extends T>>both(beginsWith(items))
				.and(hasInfiniteSize());
	}

	@SafeVarargs
	public static <T> Matcher<SizedIterable<? extends T>> infiniteBeginningWith(Matcher<? super T>... itemMatchers) {
		return Matchers.<SizedIterable<? extends T>>both(beginsWith(itemMatchers))
				.and(hasInfiniteSize());
	}

	public static Matcher<SizedIterable<?>> hasKnownSize(int size) {
		return new HasSizeCharacteristics(AVAILABLE, size, size == 0);
	}

	public static Matcher<SizedIterable<?>> hasComputedSize(int size) {
		return new HasSizeCharacteristics(UNAVAILABLE, size, size == 0);
	}

	public static Matcher<SizedIterable<?>> hasInfiniteSize() {
		return new HasSizeCharacteristics(INFINITE, -1, false);
	}

	public HasSizeCharacteristics(SizeType sizeType, int size, boolean isEmpty) {
		this.sizeType = sizeType;
		this.size = size;
		this.isEmpty = isEmpty;
	}

	@Override
	public boolean matches(Object actualValue) {
		if (!(actualValue instanceof SizedIterable<?>))
			throw new IllegalStateException();

		SizedIterable<?> iterable = (SizedIterable<?>) actualValue;
		if (iterable.sizeType() != sizeType)
			return false;

		if (size == -1) {
			try {
				iterable.size();
				return false;
			} catch (UnsupportedOperationException expected) {
				// expected; continue
			}
		} else {
			if (iterable.size() != size)
				return false;
		}

		return iterable.isEmpty() == isEmpty;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("has ");
		description.appendText(sizeType.toString());
		description.appendText(" size of ");
		description.appendValue(size);
		description.appendText(" and ");
		description.appendText(isEmpty ? "is empty" : "is not empty");
	}

	@Override
	public void describeMismatch(Object item, Description description) {
		if (!(item instanceof SizedIterable<?>))
			throw new IllegalStateException();

		SizedIterable<?> iterable = (SizedIterable<?>) item;
		description.appendText("had ");
		description.appendText(iterable.sizeType().toString());
		description.appendText(" size of ");
		description.appendValue(iterable.size());
		description.appendText(" and ");
		description.appendText(iterable.isEmpty() ? "was empty" : "was not empty");
	}
}
