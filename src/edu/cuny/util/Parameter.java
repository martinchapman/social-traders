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
 Copyright 2006 by Sean Luke
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */

package edu.cuny.util;

import java.io.Serializable;

/**
 * <p>
 * A {@link Parameter} is an object which the {@link ParameterDatabase} class
 * uses as a key to associate with strings, forming a key-value pair. Parameters
 * are designed to be hierarchical in nature, consisting of "path items"
 * separated by a path separator. Parameters are created either from a single
 * path item, from an array of path items, or both. For example, a parameter
 * with the path foo.bar.baz might be created from
 * <tt>new Parameter(new String[] {"foo","bar","baz"})</tt>
 * 
 * <p>
 * Parameters are not mutable -- but once a parameter is created, path items may
 * be pushed an popped from it, forming a new parameter. For example, if a
 * parameter p consists of the path foo.bar.baz, p.pop() results in a new
 * parameter whose path is foo.bar This pushing and popping isn't cheap, so be
 * sparing.
 * 
 * <p>
 * Because this system internally uses "." as its path separator, you should not
 * use that character in parts of the path that you provide; however if you need
 * some other path separator, you can change the delimiter in the code
 * trivially. In fact, you can create a new Parameter with a path foo.bar.baz
 * simply by calling <tt>new Parameter("foo.bar.baz")</tt> but you'd better know
 * what you're doing.
 * 
 * <p>
 * Additionally, parameters must not contain "#", "=", non-ascii values, or
 * whitespace. Yes, a parameter path item may be empty.
 * 
 * <p>
 * This is a modified version of the class from the original ECJ package by Sean
 * Luke, et al..
 * </p>
 * 
 * @author Sean Luke
 * @version $Revision: 1.8 $
 */

public class Parameter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String param;

	public static final char delimiter = '.';

	/** Creates a new parameter by joining the path items in s into a single path. */
	public Parameter(final String[] s) throws BadParameterException {
		if (s.length == 0) {
			throw new BadParameterException("Parameter created with length 0");
		}
		for (int x = 0; x < s.length; x++) {
			if (s[x] == null) {
				throw new BadParameterException("Parameter created with null string");
			}
			if (x == 0) {
				param = s[x];
			} else {
				param += (Parameter.delimiter + s[x]);
			}
		}
	}

	/** Creates a new parameter from the single path item in s. */
	public Parameter(final String s) throws BadParameterException {
		if (s == null) {
			throw new BadParameterException("Parameter created with null string");
		}
		param = s;
	}

	/**
	 * Creates a new parameter from the path item in s, plus the path items in s2.
	 * s2 may be null or empty, but not s
	 */
	public Parameter(final String s, final String[] s2) {
		if (s == null) {
			throw new BadParameterException("Parameter created with null string");
		}
		param = s;
		for (final String element : s2) {
			if (element == null) {
				throw new BadParameterException("Parameter created with null string");
			} else {
				param += (Parameter.delimiter + element);
			}
		}
	}

	/** Returns a new parameter with s added to the end of the current path items. */
	public Parameter push(final String s) {
		if (s == null) {
			throw new BadParameterException("Parameter pushed with null string");
		}
		return new Parameter(param + Parameter.delimiter + s);
	}

	/**
	 * Returns a new parameter with the path items in s added to the end of the
	 * current path items.
	 */
	public Parameter push(final String[] s) {
		return new Parameter(param, s);
	}

	/**
	 * Returns a new parameter with one path item popped off the end. If this
	 * would result in a parameter with an empty collection of path items, null is
	 * returned.
	 */
	public Parameter pop() {
		final int x = param.lastIndexOf(Parameter.delimiter);
		if (x == -1) {
			return null;
		} else {
			return new Parameter(param.substring(0, x));
		}
	}

	/**
	 * Returns a new parameter with n path items popped off the end. If this would
	 * result in a parameter with an empty collection of path items, null is
	 * returned.
	 */
	public Parameter popn(final int n) {
		String s = param;

		for (int y = 0; y < n; y++) {
			final int x = param.lastIndexOf(Parameter.delimiter);
			if (x == -1) {
				return null;
			} else {
				s = param.substring(0, x);
			}
		}
		return new Parameter(s);
	}

	/** Returns the path item at the far end of the parameter. */
	public String top() {
		final int x = param.lastIndexOf(Parameter.delimiter);
		if (x == -1) {
			return param;
		} else {
			return param.substring(x + 1);
		}
	}

	@Override
	public String toString() {
		return param;
	}

}
