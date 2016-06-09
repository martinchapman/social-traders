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

import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 * <p>
 * A utility class for cumulative tracking of stats for a series of doubles.
 * Moments are incremented dynamically, rather than keeping the actual cases in
 * memory.
 * </p>
 * 
 * <p>
 * Example usage:
 * 
 * <pre>
 * Distribution series1 = new CumulativeDistribution(&quot;series1&quot;);
 * &lt;br&gt;
 * series1.newData(4.5);
 * &lt;br&gt;
 * series1.newData(5.6);
 * &lt;br&gt;
 * series1.newData(9.0);
 * &lt;br&gt;
 * System.out.println(&quot;Standard deviation of series1 = &quot; + series1.getStdDev());
 * &lt;br&gt;
 * series1.newData(5.56);
 * &lt;br&gt;
 * series1.newData(12);
 * &lt;br&gt;
 * System.out.println(&quot;And now the standard deviation = &quot; + series1.getStdDev());
 * &lt;br&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.13 $
 */

public class CumulativeDistribution implements Serializable, Cloneable,
		Resetable, Distribution {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The number of data in the series so far.
	 */
	protected int n;

	/**
	 * The cummulative total of all numbers in the series so far.
	 */
	protected double total;

	/**
	 * The total of the squares of all numbers in the series so far.
	 */
	protected double totalSq;

	/**
	 * The minimum so far.
	 */
	protected double min;

	/**
	 * The maximum so far.
	 */
	protected double max;

	/**
	 * The name of this series.
	 */
	protected String varName;

	static Logger logger = Logger.getLogger(CumulativeDistribution.class);

	public CumulativeDistribution() {
		this("");
	}

	public CumulativeDistribution(final String varName) {
		this.varName = varName;
		init0();
	}

	private void init0() {
		n = 0;
		total = 0;
		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;
		totalSq = 0;
	}

	public void reset() {
		init0();
	}

	/**
	 * Add a new datum to the series.
	 */
	public void newData(final double i) {
		n++;
		total += i;
		totalSq += i * i;
		if (i > max) {
			max = i;
		}
		if (i < min) {
			min = i;
		}
	}

	/**
	 * Add multiple new data of the same value to the series.
	 * 
	 * @param i
	 *          the value of every datum
	 * @param n
	 *          the number of data
	 */
	public void newData(final double i, final double n) {
		for (int k = 0; k < n; k++) {
			newData(i);
		}
	}

	/**
	 * Get the number of items in the series.
	 */
	public int getN() {
		return n;
	}

	/**
	 * Get the mean of the data.
	 */
	public double getMean() {
		return total / n;
	}

	/**
	 * Get the variance about the mean.
	 */
	public double getVariance() {
		return getVariance(getMean());
	}

	/**
	 * Get the variance about the origin.
	 */
	public double getVariance(final double origin) {
		final double variance = (totalSq / n + (origin - 2 * getMean()) * origin);
		if (variance < 0) {
			// NOTE: due to truncation error, variance may become negative but very
			// close to 0.
			if (MathUtil.approxEqual(variance, 0.0, MathUtil.DEFAULT_ERROR)) {
				return 0.0;
			} else {
				return variance;
			}
		} else {
			return variance;
		}
	}

	/**
	 * Get the standard deviation from the mean.
	 */
	public double getStdDev() {
		return Math.sqrt(getVariance());
	}

	/**
	 * Get the standard deviation from the origin.
	 */
	public double getStdDev(final double origin) {
		return Math.sqrt(getVariance(origin));
	}

	public double getVarCoef(final double origin) {
		return 100 * getStdDev(origin) / origin;
	}

	/**
	 * Get the minimum datum.
	 */
	public double getMin() {
		return min;
	}

	/**
	 * Get the maximum datum.
	 */
	public double getMax() {
		return max;
	}

	/**
	 * Get the total of the data
	 */
	public double getTotal() {
		return total;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String getName() {
		return varName;
	}

	public void log() {
		CumulativeDistribution.logger.info(getName());
		CumulativeDistribution.logger.info(Utils.indent("n:\t" + getN()));
		CumulativeDistribution.logger.info(Utils.indent("min:\t" + getMin()));
		CumulativeDistribution.logger.info(Utils.indent("max:\t" + getMax()));
		CumulativeDistribution.logger.info(Utils.indent("mean:\t" + getMean()));
		CumulativeDistribution.logger.info(Utils.indent("stdev:\t" + getStdDev()));
	}

	public void combine(final Distribution other) {
		final CumulativeDistribution d = (CumulativeDistribution) other;
		min = Math.min(min, d.min);
		max = Math.max(max, d.max);
		n += d.n;
		total += d.total;
		totalSq += d.totalSq;
	}

	public double getTrimmedMean(final double p) {
		if (p > 0) {
			throw new Error("method not implemented for p > 0");
		} else {
			return getMean();
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " varName:" + varName + " n:" + n
				+ " mean:" + getMean() + " stdev:" + getStdDev() + " min:" + min
				+ " max:" + max;
	}

}