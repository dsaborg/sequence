/*
 * Copyright 2016 Daniel Skogquist Åborg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.d2ab.sequence;

import org.d2ab.collection.ReverseList;
import org.d2ab.iterable.ChainingIterable;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

/**
 * A {@link Sequence} backed by a {@link List}.
 */
public abstract class ListSequence<T> implements Sequence<T> {
	public static <T> Sequence<T> empty() {
		return from(emptyList());
	}

	public static <T> Sequence<T> of(T item) {
		return from(singletonList(item));
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> of(T... items) {
		return from(asList(items));
	}

	public static <T> Sequence<T> from(List<T> list) {
		return new ListSequence<T>() {
			@Override
			public List<T> toList() {
				return list;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Sequence<T> append(Iterable<T> iterable) {
				if (iterable instanceof List)
					return ChainedListSequence.from(toList(), (List<T>) iterable);

				return new ChainingIterable<>(this, iterable)::iterator;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Sequence<T> append(T... items) {
				return append(asList(items));
			}
		};
	}

	@Override
	public Iterator<T> iterator() {
		return toList().iterator();
	}

	@Override
	public Sequence<T> skip(long skip) {
		return new ListSequence<T>() {
			@Override
			public List<T> toList() {
				List<T> list = ListSequence.this.toList();
				return list.subList(Math.min(list.size(), (int) skip), list.size());
			}
		};
	}

	@Override
	public Sequence<T> limit(long limit) {
		return new ListSequence<T>() {
			@Override
			public List<T> toList() {
				List<T> list = ListSequence.this.toList();
				return list.subList(0, Math.min(list.size(), (int) limit));
			}
		};
	}

	@Override
	public abstract List<T> toList();

	@Override
	public <U extends Collection<T>> U collectInto(U collection) {
		collection.addAll(toList());
		return collection;
	}

	@Override
	public Sequence<T> reverse() {
		return new ListSequence<T>() {
			@Override
			public List<T> toList() {
				return ReverseList.from(ListSequence.this.toList());
			}
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public Sequence<T> sorted() {
		return new ListSequence<T>() {
			@Override
			public List<T> toList() {
				@SuppressWarnings("unchecked")
				List list = ListSequence.this.toList();
				List sorted = new ArrayList<>(list);
				Collections.sort(sorted);
				return (List<T>) unmodifiableList(sorted);
			}
		};
	}

	@Override
	public Sequence<T> sorted(Comparator<? super T> comparator) {
		return new ListSequence<T>() {
			@Override
			public List<T> toList() {
				@SuppressWarnings("unchecked")
				List<T> list = ListSequence.this.toList();
				List<T> sorted = new ArrayList<>(list);
				Collections.sort(sorted, comparator);
				return unmodifiableList(sorted);
			}
		};
	}

	@Override
	public Sequence<T> shuffle() {
		return new ListSequence<T>() {
			@Override
			public List<T> toList() {
				@SuppressWarnings("unchecked")
				List<T> list = ListSequence.this.toList();
				List<T> shuffled = new ArrayList<>(list);
				Collections.shuffle(shuffled);
				return unmodifiableList(shuffled);
			}
		};
	}

	@Override
	public Sequence<T> shuffle(Random md) {
		return new ListSequence<T>() {
			@Override
			public List<T> toList() {
				@SuppressWarnings("unchecked")
				List<T> list = ListSequence.this.toList();
				List<T> shuffled = new ArrayList<>(list);
				Collections.shuffle(shuffled, md);
				return unmodifiableList(shuffled);
			}
		};
	}

	@Override
	public Optional<T> get(long index) {
		List<T> list = toList();
		if (list.size() < index + 1)
			return Optional.empty();

		return Optional.of(list.get((int) index));
	}

	@Override
	public Optional<T> last() {
		List<T> list = toList();
		if (list.size() < 1)
			return Optional.empty();

		return Optional.of(list.get(list.size() - 1));
	}

	@Override
	public Stream<T> stream() {
		return toList().stream();
	}

	@Override
	public void removeAll() {
		toList().clear();
	}

	@Override
	public boolean isEmpty() {
		return toList().isEmpty();
	}
}
