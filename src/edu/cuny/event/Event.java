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

package edu.cuny.event;

import java.util.EventObject;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.cuny.util.Utils;

/**
 * <p>
 * Defines a type of events that can carry a user object and a map of
 * properties.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class Event extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Object userObject;

	private SortedMap<String, Object> pairs;

	public Event(final Object source) {
		this(source, null);
	}

	public Event(final Object source, final Object userObject) {
		super(source);
		this.userObject = userObject;
	}

	public void setSource(final Object source) {
		this.source = source;
	}

	public void setUserObject(final Object userObject) {
		this.userObject = userObject;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void clearValues() {
		if (pairs != null) {
			pairs.clear();
		}
	}

	public Set<String> getKeys() {
		if (pairs == null) {
			return null;
		} else {
			return pairs.keySet();
		}
	}

	public void setValue(final String key, final Object value) {
		if (pairs == null) {
			pairs = new TreeMap<String, Object>();
		}

		pairs.put(key, value);
	}

	public Object getValue(final String key) {
		if (pairs == null) {
			return null;
		}

		return pairs.get(key);
	}

	public String getString(final String key) {
		return (String) getValue(key);
	}

	@Override
	public String toString() {
		String s = super.toString();

		if (pairs != null) {
			for (final String key : pairs.keySet()) {
				s += "\n" + Utils.indent(key + ":" + pairs.get(key));
			}
		}

		return s;
	}
}
