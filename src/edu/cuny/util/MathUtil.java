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

import java.util.Vector;

import org.apache.log4j.Logger;

import cern.jet.random.Uniform;
import edu.cuny.cat.comm.Message;
import edu.cuny.prng.GlobalPRNG;

/**
 * Miscalleneous mathematical functions.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.18 $
 */

public class MathUtil {

	static final Logger logger = Logger.getLogger(MathUtil.class);

	public static final double DEFAULT_ERROR = 0.0000001;

	public static final double DEFAULT_BIG_ERROR = 0.0001;

	/**
	 * TODO: this static uniform distribution should be avoided later since
	 * multiple systems in terms of {@link ObjectRegistry} may use it and lead to
	 * different results of inner systems.
	 * 
	 * @see #maxIndex(double[])
	 * @see #maxIndex(double[], NumberComparator)
	 */
	protected static final Uniform uniform = new Uniform(0, 1, Galaxy
			.getInstance().getDefaultTyped(GlobalPRNG.class).getEngine());

	/**
	 * rounds up a double up to the specified number of digits after period.
	 * 
	 * @param x
	 *          the double to be rounded to the specified digits after period
	 * @param digits
	 *          the number of digits to be rounded up after period
	 * @return the rouned-up double
	 */
	public static double round(final double x, final int digits) {
		final double d = Math.pow(10, digits);
		return Math.round(x * d) / d;
	}

	/**
	 * Calculate the square of x.
	 */
	public static double squared(final double x) {
		return x * x;
	}

	/**
	 * Calculate the difference of the squares of x and y.
	 */
	public static double diffSq(final double x, final double y) {
		return MathUtil.squared(x) - MathUtil.squared(y);
	}

	/**
	 * 
	 * @param series
	 *          an array of double numbers
	 * @return the sum of data items in the series.
	 */
	public static double sum(final double[] series) {
		double total = 0;
		for (final double serie : series) {
			total += serie;
		}
		return total;
	}

	/**
	 * Returns true if the difference between x and y is less than error.
	 */
	public static boolean approxEqual(final double x, final double y,
			final double error) {
		return Math.abs(x - y) <= error;
	}

	/**
	 * Returns true if the difference between x and y is less than #DEFAULT_ERROR.
	 */
	public static boolean approxEqual(final double x, final double y) {
		return MathUtil.approxEqual(x, y, MathUtil.DEFAULT_ERROR);
	}

	/**
	 * Returns true if x is bigger y when the comparison is loosened by the amount
	 * of error.
	 */
	public static boolean approxBigger(final double x, final double y,
			final double error) {
		return x + error > y;
	}

	/**
	 * Returns true if x is bigger y when the comparison is loosened by the amount
	 * of #DEFAULT_ERROR.
	 */
	public static boolean approxBigger(final double x, final double y) {
		return MathUtil.approxBigger(x, y, MathUtil.DEFAULT_ERROR);
	}

	/**
	 * Returns true if x is smaller y when the comparison is loosened by the
	 * amount of error.
	 */
	public static boolean approxSmaller(final double x, final double y,
			final double error) {
		return x - error < y;
	}

	/**
	 * Returns true if x is smaller y when the comparison is loosened by the
	 * amount of #DEFAULT_ERROR.
	 */
	public static boolean approxSmaller(final double x, final double y) {
		return MathUtil.approxSmaller(x, y, MathUtil.DEFAULT_ERROR);
	}

	/**
	 * finds the index of the maximal entry in an array. If multiple maximal
	 * entries exist, one of them is returned randomly.
	 * 
	 * @param values
	 *          the array
	 * @param distribution
	 *          a {@link edu.cuny.random.Uniform} distribution
	 * @return the index of the maximal entry
	 */

	public static int maxIndex(final double[] values, final Uniform distribution) {
		double max = Double.NEGATIVE_INFINITY;
		int maxIndex = -1;
		for (int i = 0; i < values.length; i++) {
			if (values[i] > max) {
				max = values[i];
				maxIndex = i;
			} else if (values[i] == max) {
				return MathUtil.maxIndex_slow(values, distribution);
			}
		}

		return maxIndex;
	}

	public static int maxIndex(final double[] values) {
		return MathUtil.maxIndex(values, MathUtil.uniform);
	}

	protected static int maxIndex_slow(final double[] values,
			final Uniform distribution) {

		double max = Double.NEGATIVE_INFINITY;
		final Vector<Integer> maxIndices = new Vector<Integer>();
		for (int i = 0; i < values.length; i++) {
			if (values[i] > max) {
				maxIndices.clear();
				maxIndices.add(new Integer(i));
				max = values[i];
			} else if (values[i] == max) {
				maxIndices.add(new Integer(i));
			}
		}

		if (maxIndices.size() <= 0) {
			MathUtil.logger.fatal("Failed to find max elements in ["
					+ Message.concatenate(values) + "]");
			return -1;
		} else {
			final Integer i = maxIndices.elementAt(distribution.nextIntFromTo(0,
					maxIndices.size() - 1));
			return i.intValue();
		}
	}

	/**
	 * finds the index of the maximal entry in an array. If multiple maximal
	 * entries exist, one of them is returned randomly.
	 * 
	 * @param values
	 *          the array
	 * @param distribution
	 *          a {@link edu.cuny.random.Uniform} distribution
	 * @param comparator
	 *          a utility that does the actual comparison and may or may not allow
	 *          certain degree of rounding error.
	 * @return the index of the maximal entry
	 */
	public static int maxIndex(final double[] values, final Uniform distribution,
			final NumberComparator comparator) {
		double max = Double.NEGATIVE_INFINITY;
		int maxIndex = -1;
		for (int i = 0; i < values.length; i++) {
			if (comparator.equal(values[i], max)) {
				return MathUtil.maxIndex_slow(values, distribution, comparator);
			} else if (comparator.bigger(values[i], max)) {
				max = values[i];
				maxIndex = i;
			}
		}

		return maxIndex;
	}

	public static int maxIndex(final double[] values,
			final NumberComparator comparator) {
		return MathUtil.maxIndex(values, MathUtil.uniform, comparator);
	}

	/**
	 * TODO: If a certain degree of rounding error allows, more complex algorithm
	 * is needed to avoid problems. The current implementation may lead to an
	 * accumulation of unequal values one within the rounding error distance from
	 * another.
	 * 
	 * @param values
	 * @param distribution
	 * @param comparator
	 * @return the index of the maximal entry
	 */
	protected static int maxIndex_slow(final double[] values,
			final Uniform distribution, final NumberComparator comparator) {

		double max = Double.NEGATIVE_INFINITY;
		final Vector<Integer> maxIndices = new Vector<Integer>();
		for (int i = 0; i < values.length; i++) {
			if (comparator.equal(values[i], max)) {
				maxIndices.add(new Integer(i));
			} else if (comparator.bigger(values[i], max)) {
				maxIndices.clear();
				maxIndices.add(new Integer(i));
				max = values[i];
			}
		}

		if (maxIndices.size() <= 0) {
			MathUtil.logger.fatal("Failed to find max elements in ["
					+ Message.concatenate(values) + "]");
			return -1;
		} else {
			final Integer i = maxIndices.elementAt(distribution.nextIntFromTo(0,
					maxIndices.size() - 1));
			return i.intValue();
		}
	}
}