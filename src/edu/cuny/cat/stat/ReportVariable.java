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
 * JASA Java Auction Simulator API
 * Copyright (C) 2001-2005 Steve Phelps
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

package edu.cuny.cat.stat;

/**
 * A class representing a variable produced by a {@link GameReport}.
 * 
 * @see GameReport#getVariables
 * 
 * @author Steve Phelps
 * @version $Revision: 1.13 $
 */

public class ReportVariable implements Comparable<ReportVariable> {

	public static String SEPARATOR = ".";

	protected String name;

	protected String description;

	public ReportVariable(final String name, final String description) {
		this.name = name;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name + " (" + description + ")";
	}

	public int compareTo(final ReportVariable other) {
		return name.compareTo(other.name);
	}

	@Override
	public boolean equals(final Object other) {
		return name.equals(((ReportVariable) other).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
