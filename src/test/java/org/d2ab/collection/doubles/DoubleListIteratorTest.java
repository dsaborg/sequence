package org.d2ab.collection.doubles;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DoubleListIteratorTest {
	DoubleListIterator emptyListIterator = DoubleListIterator.of();
	DoubleListIterator listIterator = DoubleListIterator.of(1, 2, 3, 4, 5);

	@Test
	public void listIteratorEmpty() {
		assertThat(emptyListIterator.hasNext(), is(false));
		assertThat(emptyListIterator.nextIndex(), is(0));
		assertThat(emptyListIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyListIterator::nextDouble);
		expecting(NoSuchElementException.class, emptyListIterator::previousDouble);

		expecting(UnsupportedOperationException.class, () -> emptyListIterator.add(17));
		assertThat(emptyListIterator.hasNext(), is(false));
		assertThat(emptyListIterator.nextIndex(), is(0));
		assertThat(emptyListIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyListIterator::nextDouble);
		expecting(NoSuchElementException.class, emptyListIterator::previousDouble);
	}

	@Test
	public void listIterator() {
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.nextDouble(), is(1.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextDouble(), is(2.0));
		expecting(UnsupportedOperationException.class, () -> listIterator.add(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.previousDouble(), is(2.0));
		expecting(UnsupportedOperationException.class, () -> listIterator.set(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextDouble(), is(2.0));
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

		expecting(UnsupportedOperationException.class, () -> emptyListIterator.add((Double) 17.0));
		assertThat(emptyListIterator.hasNext(), is(false));
		assertThat(emptyListIterator.nextIndex(), is(0));
		assertThat(emptyListIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyListIterator::next);
		expecting(NoSuchElementException.class, emptyListIterator::previous);
	}

	@Test
	public void listIteratorBoxing() {
		DoubleList list = DoubleList.create(1, 2, 3, 4, 5);
		DoubleListIterator listIterator = list.listIterator();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.next(), is(1.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is(2.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		listIterator.add((Double) 17.0);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.next(), is(3.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		listIterator.set((Double) 18.0);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.next(), is(4.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(5));
		assertThat(listIterator.previousIndex(), is(4));

		listIterator.remove();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(list, containsDoubles(1, 2, 17, 18, 5));
	}

	@Test
	public void defaultRemove() {
		DoubleListIterator listIterator = new DoubleListIterator() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public double nextDouble() {
				return 0;
			}

			@Override
			public boolean hasPrevious() {
				return false;
			}

			@Override
			public double previousDouble() {
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
