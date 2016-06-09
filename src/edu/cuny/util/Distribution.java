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

package edu.cuny.util;

/**
 * Defines the operations on the distribution of samples of some random
 * variable.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.11 $
 */

public interface Distribution {

	/**
	 * Add a new datum to the series.
	 */
	public abstract void newData(double i);

	/**
	 * Get the number of items in the series.
	 */
	public abstract int getN();

	/**
	 * Get the mean of the data.
	 */
	public abstract double getMean();

	/**
	 * Get the variance about the mean.
	 */
	public abstract double getVariance();

	/**
	 * Get the variance about the origin.
	 */
	public abstract double getVariance(double origin);

	/**
	 * Get the standard deviation from the mean.
	 */
	public abstract double getStdDev();

	/**
	 * Get the standard deviation from the origin.
	 */
	public abstract double getStdDev(double origin);

	/**
	 * Get the coefficient of variable about origin.
	 */
	public abstract double getVarCoef(double origin);

	/**
	 * Get the minimum datum.
	 */
	public abstract double getMin();

	/**
	 * Get the maximum datum.
	 */
	public abstract double getMax();

	/**
	 * Get the total of the data
	 */
	public abstract double getTotal();

	/**
	 * Combine this distribution with the supplied distribution.
	 */
	public void combine(Distribution other);

	/**
	 * @return The name of the variable.
	 */
	public abstract String getName();

	/**
	 * Output the moments of the distribution to the info log.
	 */
	public abstract void log();
}