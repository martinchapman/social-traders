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

import org.apache.log4j.Logger;

import gnu.trove.TDoubleArrayList;

/**
 * An distribution which keeps actual cases in memory as well as updating
 * moments dynamically. This implementation of a Distribution is capable of
 * calculating trimmed means. Note that calculating the trimmed mean results in
 * a sorting operation, and hence is not as efficient as calculating the
 * untrimmed mean.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.2 $
 */

public class HeavyweightDistribution extends CumulativeDistribution {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected TDoubleArrayList data;

	protected boolean hasChanged = false;

	private static final int INITIAL_SIZE = 10000;

	static Logger logger = Logger.getLogger(HeavyweightDistribution.class);

	public HeavyweightDistribution() {
		this(null);
	}

	public HeavyweightDistribution(final String name) {
		super(name);

		init0();
	}

	private void init0() {
		data = new TDoubleArrayList(HeavyweightDistribution.INITIAL_SIZE);
		hasChanged = false;
	}

	@Override
	public void reset() {
		super.reset();
		data.reset();
		hasChanged = true;
	}

	@Override
	public void newData(final double datum) {
		super.newData(datum);
		data.add(datum);
		hasChanged = true;
	}

	@Override
	public double getTrimmedMean(final double p) {
		if (p == 0) {
			return getMean();
		}
		if (hasChanged) {
			data.sort();
		}
		final int trimmedN = (int) ((p / 2) * n);
		double trimmedTotal = 0;
		for (int i = trimmedN; i < (n - trimmedN); i++) {
			trimmedTotal += data.get(i);
		}
		final double trimmedMean = trimmedTotal / (n - trimmedN * 2);
		hasChanged = false;
		return trimmedMean;
	}

	@Override
	public void combine(final Distribution other) {
		final HeavyweightDistribution d = (HeavyweightDistribution) other;
		for (int i = 0; i < d.data.size(); i++) {
			newData(d.data.get(i));
		}
	}
}
