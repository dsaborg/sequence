package org.d2ab.collection.ints;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IntListIteratorTest {
	IntListIterator emptyListIterator = IntListIterator.of();
	IntListIterator listIterator = IntListIterator.of(1, 2, 3, 4, 5);

	@Test
	public void listIteratorEmpty() {
		assertThat(emptyListIterator.hasNext(), is(false));
		assertThat(emptyListIterator.nextIndex(), is(0));
		assertThat(emptyListIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyListIterator::nextInt);
		expecting(NoSuchElementException.class, emptyListIterator::previousInt);

		expecting(UnsupportedOperationException.class, () -> emptyListIterator.add(17));
		assertThat(emptyListIterator.hasNext(), is(false));
		assertThat(emptyListIterator.nextIndex(), is(0));
		assertThat(emptyListIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyListIterator::nextInt);
		expecting(NoSuchElementException.class, emptyListIterator::previousInt);
	}

	@Test
	public void listIterator() {
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.nextInt(), is(1));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextInt(), is(2));
		expecting(UnsupportedOperationException.class, () -> listIterator.add(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.previousInt(), is(2));
		expecting(UnsupportedOperationException.class, () -> listIterator.set(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextInt(), is(2));
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

		expecting(UnsupportedOperationException.class, () -> emptyListIterator.add((Integer) 17));
		assertThat(emptyListIterator.hasNext(), is(false));
		assertThat(emptyListIterator.nextIndex(), is(0));
		assertThat(emptyListIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyListIterator::next);
		expecting(NoSuchElementException.class, emptyListIterator::previous);
	}

	@Test
	public void listIteratorBoxing() {
		IntList list = IntList.create(1, 2, 3, 4, 5);
		IntListIterator listIterator = list.listIterator();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.next(), is(1));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is(2));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		listIterator.add((Integer) 17);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.next(), is(3));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		listIterator.set((Integer) 18);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.next(), is(4));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(5));
		assertThat(listIterator.previousIndex(), is(4));

		listIterator.remove();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(list, containsInts(1, 2, 17, 18, 5));
	}

	@Test
	public void defaultRemove() {
		IntListIterator listIterator = new IntListIterator() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public int nextInt() {
				return 0;
			}

			@Override
			public boolean hasPrevious() {
				return false;
			}

			@Override
			public int previousInt() {
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
