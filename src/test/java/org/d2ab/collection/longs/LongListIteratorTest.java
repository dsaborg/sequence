package org.d2ab.collection.longs;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LongListIteratorTest {
	LongListIterator emptyListIterator = LongListIterator.of();
	LongListIterator listIterator = LongListIterator.of(1, 2, 3, 4, 5);

	@Test
	public void listIteratorEmpty() {
		assertThat(emptyListIterator.hasNext(), is(false));
		assertThat(emptyListIterator.nextIndex(), is(0));
		assertThat(emptyListIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyListIterator::nextLong);
		expecting(NoSuchElementException.class, emptyListIterator::previousLong);

		expecting(UnsupportedOperationException.class, () -> emptyListIterator.add(17));
		assertThat(emptyListIterator.hasNext(), is(false));
		assertThat(emptyListIterator.nextIndex(), is(0));
		assertThat(emptyListIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyListIterator::nextLong);
		expecting(NoSuchElementException.class, emptyListIterator::previousLong);
	}

	@Test
	public void listIterator() {
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.nextLong(), is(1L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextLong(), is(2L));
		expecting(UnsupportedOperationException.class, () -> listIterator.add(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.previousLong(), is(2L));
		expecting(UnsupportedOperationException.class, () -> listIterator.set(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextLong(), is(2L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
	}

	@Test
	public void listIteratorEmptyBoxing() {
		assertThat(emptyListIterator.hasNext(), is(false));
		assertThat(emptyListIterator.nextIndex(), is(0));
		assertThat(emptyListIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyListIterator::next);
		expecting(NoSuchElementException.class, emptyListIterator::previous);

		expecting(UnsupportedOperationException.class, () -> emptyListIterator.add((Long) 17L));
		assertThat(emptyListIterator.hasNext(), is(false));
		assertThat(emptyListIterator.nextIndex(), is(0));
		assertThat(emptyListIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyListIterator::next);
		expecting(NoSuchElementException.class, emptyListIterator::previous);
	}

	@Test
	public void listIteratorBoxing() {
		LongList list = LongList.create(1, 2, 3, 4, 5);
		LongListIterator listIterator = list.listIterator();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.next(), is(1L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is(2L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		listIterator.add((Long) 17L);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.next(), is(3L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		listIterator.set((Long) 18L);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.next(), is(4L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(5));
		assertThat(listIterator.previousIndex(), is(4));

		listIterator.remove();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(list, containsLongs(1, 2, 17, 18, 5));
	}

	@Test
	public void defaultRemove() {
		LongListIterator listIterator = new LongListIterator() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public long nextLong() {
				return 0;
			}

			@Override
			public boolean hasPrevious() {
				return false;
			}

			@Override
			public long previousLong() {
				return 0;
			}

			@Override
			public int nextIndex() {
				return 0;
			}

			@Override
			public int previousIndex() {
				return 0;
			}
		};

		expecting(UnsupportedOperationException.class, listIterator::remove);
	}
}
