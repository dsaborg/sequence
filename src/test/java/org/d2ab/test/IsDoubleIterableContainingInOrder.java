/*
 * BSD License
 *
 * Copyright (c) 2000-2015 www.hamcrest.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce
 * the above copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Hamcrest nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.d2ab.test;

import org.d2ab.collection.doubles.DoubleIterable;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.internal.NullSafety;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.IsEqual.equalTo;

public class IsDoubleIterableContainingInOrder extends TypeSafeDiagnosingMatcher<DoubleIterable> {
    private final List<Matcher<? super Double>> matchers;

    public IsDoubleIterableContainingInOrder(List<Matcher<? super Double>> matchers) {
        this.matchers = matchers;
    }

    @Override
    protected boolean matchesSafely(DoubleIterable iterable, Description mismatchDescription) {
        final DoubleMatchSeries matchSeries = new DoubleMatchSeries(matchers, mismatchDescription);
        for (DoubleIterator iterator = iterable.iterator(); iterator.hasNext(); ) {
            double item = iterator.nextDouble();
            if (!matchSeries.matches(item)) {
                return false;
            }
        }

        return matchSeries.isFinished();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("double iterable containing ").appendList("[", ", ", "]", matchers);
    }

    private static class DoubleMatchSeries {
        private final List<Matcher<? super Double>> matchers;
        private final Description mismatchDescription;
        private int nextMatchIx = 0;

        public DoubleMatchSeries(List<Matcher<? super Double>> matchers, Description mismatchDescription) {
            this.mismatchDescription = mismatchDescription;
            if (matchers.isEmpty()) {
                throw new IllegalArgumentException("Should specify at least one expected double");
            }
            this.matchers = matchers;
        }

        public boolean matches(double item) {
          if (matchers.size() <= nextMatchIx) {
            mismatchDescription.appendText("not matched: ").appendValue(item);
            return false;
          }

          return isMatched(item);
        }

        public boolean isFinished() {
            if (nextMatchIx < matchers.size()) {
                mismatchDescription.appendText("no item was ").appendDescriptionOf(matchers.get(nextMatchIx));
                return false;
            }
            return true;
        }

        private boolean isMatched(double item) {
            final Matcher<? super Double> matcher = matchers.get(nextMatchIx);
            if (!matcher.matches(item)) {
                describeMismatch(matcher, item);
                return false;
            }
            nextMatchIx++;
            return true;
        }

      private void describeMismatch(Matcher<? super Double> matcher, double item) {
            mismatchDescription.appendText("item " + nextMatchIx + ": ");
            matcher.describeMismatch(item, mismatchDescription);
        }
    }

    /**
     * Creates a matcher for {@link DoubleIterable}s that matches when a single pass over the
     * examined {@link DoubleIterable} yields a series of items, each logically equal to the
     * corresponding item in the specified items.  For a positive match, the examined iterable
     * must be of the same length as the number of specified items.
     *
     * @param items
     *     the items that must equal the items provided by an examined {@link DoubleIterable}
     */
    public static Matcher<DoubleIterable> containsDoubles(double... items) {
        List<Matcher<? super Double>> matchers = new ArrayList<>();
        for (double item : items) {
            matchers.add(equalTo(item));
        }

        return containsDoubles(matchers);
    }

    /**
     * Creates a matcher for {@link DoubleIterable}s that matches when a single pass over the
     * examined {@link DoubleIterable} yields a single item that satisfies the specified matcher.
     * For a positive match, the examined iterable must only yield one item.
     *
     * @param itemMatcher
     *     the matcher that must be satisfied by the single item provided by an
     *     examined {@link DoubleIterable}
     */
    @SuppressWarnings("unchecked")
    public static Matcher<DoubleIterable> containsDoubles(final Matcher<? super Double> itemMatcher) {
        return containsDoubles(new ArrayList<>(singletonList(itemMatcher)));
    }

    /**
     * Creates a matcher for {@link DoubleIterable}s that matches when a single pass over the
     * examined {@link DoubleIterable} yields a series of items, each satisfying the corresponding
     * matcher in the specified matchers.  For a positive match, the examined iterable
     * must be of the same length as the number of specified matchers.
     *
     * @param itemMatchers
     *     the matchers that must be satisfied by the items provided by an examined {@link Iterable}
     */
    @SafeVarargs
    public static Matcher<DoubleIterable> containsDoubles(Matcher<? super Double>... itemMatchers) {
        // required for JDK 1.6
        final List<Matcher<? super Double>> nullSafeWithExplicitTypeMatchers = NullSafety.nullSafe(itemMatchers);
    	return containsDoubles(nullSafeWithExplicitTypeMatchers);
    }

    /**
     * Creates a matcher for {@link DoubleIterable}s that matches when a single pass over the
     * examined {@link DoubleIterable} yields a series of items, each satisfying the corresponding
     * matcher in the specified list of matchers.  For a positive match, the examined iterable
     * must be of the same length as the specified list of matchers.
     *
     * @param itemMatchers
     *     a list of matchers, each of which must be satisfied by the corresponding item provided by
     *     an examined {@link DoubleIterable}
     */
    public static Matcher<DoubleIterable> containsDoubles(List<Matcher<? super Double>> itemMatchers) {
        return new IsDoubleIterableContainingInOrder(itemMatchers);
    }
}
