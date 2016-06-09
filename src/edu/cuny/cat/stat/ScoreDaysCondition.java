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

package edu.cuny.cat.stat;

/**
 * An interface used to determine scoring game days.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public interface ScoreDaysCondition {

	/**
	 * determines whether the specified day is counted for scoring. It assumes
	 * that various invocations have non-decreasing values for the argument
	 * <code>day</code>.
	 * 
	 * @param day
	 *          the day to be considered
	 * @return true if the specified day is counted; false otherwise.
	 */
	public boolean count(int day);
}
