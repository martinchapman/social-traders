/*
 * JCAT - TAC Market Design Competition Platform
 * Copyright (C) 2006-2010 Jinzhong Niu, Kai Cai
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
/*
 * JAF - Java Application Framework
 * Copyright (C) 1999-2006 Jinzhong Niu
 */

package edu.cuny.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.ListIterator;

import org.apache.commons.collections15.list.TreeList;

/**
 * <p>
 * A tree-based sorted list.
 * </p>
 * 
 * @param <E>
 *          the type of values that the tree list can contain.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.12 $
 */

public class SortedTreeList<E extends Comparable<E>> extends TreeList<E> {

	private String name;

	/**
	 * when null, elements compared directly
	 */
	protected Comparator<E> comparator = null;

	public SortedTreeList(final String name) {
		this(name, null, null);
	}

	public SortedTreeList(final String name, final Collection<E> c) {
		this(name, c, null);
	}

	public SortedTreeList(final String name, final Comparator<E> comparator) {
		this(name, null, comparator);
	}

	public SortedTreeList(final String name, final Collection<E> c,
			final Comparator<E> comparator) {
		this.name = name;
		this.comparator = comparator;
		if ((c != null) && !c.isEmpty()) {
			addAll(c);
		}
	}

	public void setComparator(final Comparator<E> comparator) {
		this.comparator = comparator;
	}

	public Comparator<E> getComparator() {
		return comparator;
	}

	/**
	 * adds <code>o</code> into the list maintaining its sorted nature.
	 * 
	 * @param o
	 * @return always returns true
	 */
	@Override
	public boolean add(final E o) {
		add(indexOfIfAdded(o), o);
		return true;
	}

	public int indexOfIfAdded(final E o) {
		return indexOfIfAdded(0, size() - 1, o);
	}

	/**
	 * determines its index if an object is added into the list
	 * 
	 * @param b
	 * @param e
	 * @param o
	 * @return the index
	 */
	public int indexOfIfAdded(final int b, final int e, final E o) {
		if (b > e) {
			return b;
		} else {
			int c = 0;
			if (comparator != null) {
				c = comparator.compare(o, get((b + e) / 2));
			} else {
				c = o.compareTo(get((b + e) / 2));
			}

			if (c > 0) {
				// o has higher price
				return indexOfIfAdded(1 + ((b + e) / 2), e, o);
			} else if (c < 0) {
				// o has lower price
				return indexOfIfAdded(b, ((b + e) / 2) - 1, o);
			} else {
				int i = (b + e) / 2 + 1;
				while (i < size()) {
					if (comparator != null) {
						c = comparator.compare(o, get(i));
					} else {
						c = o.compareTo(get(i));
					}

					if (c < 0) {
						break;
					} else {
						i++;
					}
				}
				return i;
			}
		}
	}

	@Override
	public String toString() {
		String s = " [";
		final ListIterator<E> iterator = listIterator();
		while (iterator.hasNext()) {
			s += iterator.next() + " ";
		}
		s += "] ";
		return "(" + name + ", " + s + " size: " + size() + ")";
	}
}