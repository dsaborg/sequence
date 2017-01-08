package org.d2ab.collection.chars;

import org.d2ab.iterator.chars.CharIterator;

import java.util.Collection;

/**
 * An {@link CharList} implementation backed by a {@link CharCollection}. Supports forward iteration only.
 */
public class CollectionCharList extends CharList.Base implements IterableCharList {
	private final CharCollection collection;

	public static CharList from(final CharCollection collection) {
		return new CollectionCharList(collection);
	}

	private CollectionCharList(CharCollection collection) {
		this.collection = collection;
	}

	@Override
	public CharIterator iterator() {
		return collection.iterator();
	}

	@Override
	public int size() {
		return collection.size();
	}

	@Override
	public boolean addChar(char x) {
		return collection.addChar(x);
	}

	@Override
	public boolean addAll(Collection<? extends Character> c) {
		return collection.addAll(c);
	}

	@Override
	public boolean addAllChars(char... xs) {
		return collection.addAllChars(xs);
	}

	@Override
	public boolean addAllChars(CharCollection xs) {
		return collection.addAllChars(xs);
	}
}
