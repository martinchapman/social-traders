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

package edu.cuny.util;

/**
 * <p>
 * Utility class for handling reflection.
 * </p>
 * 
 * <p>
 * This originally appeared in
 * 
 * Generics and Collections in Java 5, Maurice Naftalin and Philip Wadler,
 * O'Reilly Media Inc., 2005.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

@SuppressWarnings("unchecked")
public class GenericReflection {

	public static <T> T newInstance(final T object)
			throws InstantiationException, IllegalAccessException {
		return (T) object.getClass().newInstance(); // unchecked cast
	}

	public static <T> Class<T> getComponentType(final T[] a) {
		return (Class<T>) a.getClass().getComponentType(); // unchecked cast
	}

	public static <T> T[] newInstance(final Class<T> k, final int size) {
		if (k.isPrimitive()) {
			throw new IllegalArgumentException("Argument cannot be primitive: " + k);
		}
		return (T[]) java.lang.reflect.Array. // unchecked cast
				newInstance(k, size);
	}

	public static <T> T[] newInstance(final T[] a, final int size) {
		return GenericReflection.newInstance(GenericReflection.getComponentType(a),
				size);
	}
}