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

package edu.cuny.jfree.data.general;

/**
 * Defines a numeric range for displaying in JFreeChart.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public class Interval {

	public double low;

	public double high;

	public Interval() {
		low = Double.NaN;
		high = Double.NaN;
	}

	public boolean isNaN() {
		return Double.isNaN(low) || Double.isNaN(high);
	}

	public boolean isInfinite() {
		return Double.isInfinite(low) || Double.isInfinite(high);
	}

	public boolean isMeaningful() {
		return !isNaN() && !isInfinite();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + low + ", " + high + ")";
	}
}
