package org.d2ab.test;

import org.d2ab.collection.chars.CharListIterator;

public class StrictCharListIterator extends StrictCharIterator implements CharListIterator {
	private final CharListIterator iterator;

	public static CharListIterator of(char... values) {
		return from(CharListIterator.of(values));
	}

	public static CharListIterator from(CharListIterator iterator) {
		return new StrictCharListIterator(iterator);
	}

	public StrictCharListIterator(CharListIterator iterator) {
		super(iterator);
		this.iterator = iterator;
	}

	@Override
	public boolean hasPrevious() {
		return iterator.hasPrevious();
	}

	@Override
	public char previousChar() {
		return iterator.previousChar();
	}

	@Override
	public Character previous() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int nextIndex() {
		return iterator.nextIndex();
	}

	@Override
	public int previousIndex() {
		return iterator.previousIndex();
	}

	@Override
	public void set(char x) {
		iterator.set(x);
	}

	@Override
	public void set(Character x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(char x) {
		iterator.add(x);
	}

	@Override
	public void add(Character x) {
		throw new UnsupportedOperationException();
	}
}
