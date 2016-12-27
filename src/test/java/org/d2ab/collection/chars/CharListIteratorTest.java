package org.d2ab.collection.chars;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CharListIteratorTest {
	CharListIterator emptyListIterator = CharListIterator.of();
	CharListIterator listIterator = CharListIterator.of('a', 'b', 'c', 'd', 'e');

	@Test
	public void listIteratorEmpty() {
		assertThat(emptyListIterator.hasNext(), is(false));
		assertThat(emptyListIterator.nextIndex(), is(0));
		assertThat(emptyListIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyListIterator::nextChar);
		expecting(NoSuchElementException.class, emptyListIterator::previousChar);

		expecting(UnsupportedOperationException.class, () -> emptyListIterator.add('q'));
		assertThat(emptyListIterator.hasNext(), is(false));
		assertThat(emptyListIterator.nextIndex(), is(0));
		assertThat(emptyListIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyListIterator::nextChar);
		expecting(NoSuchElementException.class, emptyListIterator::previousChar);
	}

	@Test
	public void listIterator() {
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.nextChar(), is('a'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextChar(), is('b'));
		expecting(UnsupportedOperationException.class, () -> listIterator.add('q'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.previousChar(), is('b'));
		expecting(UnsupportedOperationException.class, () -> listIterator.set('q'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextChar(), is('b'));
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

		expecting(UnsupportedOperationException.class, () -> emptyListIterator.add((Character) 'q'));
		assertThat(emptyListIterator.hasNext(), is(false));
		assertThat(emptyListIterator.nextIndex(), is(0));
		assertThat(emptyListIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyListIterator::next);
		expecting(NoSuchElementException.class, emptyListIterator::previous);
	}

	@Test
	public void listIteratorBoxing() {
		CharList list = CharList.create('a', 'b', 'c', 'd', 'e');
		CharListIterator listIterator = list.listIterator();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.next(), is('a'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is('b'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		listIterator.add((Character) 'q');
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.next(), is('c'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		listIterator.set((Character) 'p');
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.next(), is('d'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(5));
		assertThat(listIterator.previousIndex(), is(4));

		listIterator.remove();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(list, containsChars('a', 'b', 'q', 'p', 'e'));
	}

	@Test
	public void defaultRemove() {
		CharListIterator listIterator = new CharListIterator() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public char nextChar() {
				return 0;
			}

			@Override
			public boolean hasPrevious() {
				return false;
			}

			@Override
			public char previousChar() {
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
