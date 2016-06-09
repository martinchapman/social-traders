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
 * A comparator for double values allowing no rounding errors.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

public class ExactNumberComparator implements NumberComparator {

	public boolean equal(final double x, final double y) {
		return x == y;
	}

	public boolean bigger(final double x, final double y) {
		return x > y;
	}

	public boolean smaller(final double x, final double y) {
		return x < y;
	}

}