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

package edu.cuny.random;

import java.io.Serializable;

import org.apache.log4j.Logger;

import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.CumulativeDistribution;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Resetable;

/**
 * A class representing a discrete probability distribution which can used to
 * generate random events according to the specified distribution. The output
 * from a uniform PRNG is used to to select from the different possible events.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.2 $
 */

public class DiscreteProbabilityDistribution implements Resetable,
		Serializable, Prototypeable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The probability distribution.
	 */
	protected double p[];

	/**
	 * The number of possible events for this distribution.
	 */
	protected int k;

	/**
	 * The log4j logger.
	 */
	static Logger logger = Logger
			.getLogger(DiscreteProbabilityDistribution.class);

	/**
	 * Construct a new distribution with k possible events.
	 * 
	 * @param k
	 *          The number of possible events for this random variable
	 */
	public DiscreteProbabilityDistribution(final int k) {
		this.k = k;
		p = new double[k];
		for (int i = 0; i < k; i++) {
			p[i] = 1.0 / k;
		}
	}

	public Object protoClone() {
		DiscreteProbabilityDistribution clonedDistribution;
		try {
			clonedDistribution = (DiscreteProbabilityDistribution) clone();
			clonedDistribution.p = p.clone();
		} catch (final CloneNotSupportedException e) {
			throw new Error(e);
		}
		return clonedDistribution;
	}

	/**
	 * Set the probability of the ith event.
	 * 
	 * @param i
	 *          The event
	 * @param probability
	 *          The probability of event i occuring
	 */
	public void setProbability(final int i, final double probability) {
		p[i] = probability;
	}

	/**
	 * Get the probability of the ith event.
	 * 
	 * @param i
	 *          The event
	 */
	public double getProbability(final int i) {
		return p[i];
	}

	/**
	 * Generate a random event according to the probability distribution.
	 * 
	 * @return An integer value representing one of the possible events.
	 */
	public int generateRandomEvent() {
		final double rand = Galaxy.getInstance().getDefaultTyped(GlobalPRNG.class)
				.getEngine().raw();
		double cummProb = 0;
		for (int i = 0; i < k; i++) {
			cummProb += p[i];
			if (rand < cummProb) {
				return i;
			}
		}
		throw new ProbabilityError(this);
	}

	public void reset() {
		for (int i = 0; i < k; i++) {
			p[i] = 0;
		}
	}

	/**
	 * Compute the expected value of the random variable defined by this
	 * distribution.
	 * 
	 * @return The expected value of the distribution
	 */
	public double computeMean() {
		double total = 0;
		for (int i = 0; i < k; i++) {
			total += i * p[i];
		}
		return total;
	}

	/**
	 * Compute the minimum value of the random variable defined by this
	 * distribution.
	 * 
	 * @return The minimum integer value
	 */
	public int computeMin() {
		for (int i = 0; i < k; i++) {
			if (p[i] > 0) {
				return i;
			}
		}
		throw new ProbabilityError(this);
	}

	/**
	 * Compute the maximum value of the random variable defined by this
	 * distribution.
	 * 
	 * @return The maximum integer value
	 */
	public int computeMax() {
		for (int i = k - 1; i >= 0; i--) {
			if (p[i] > 0) {
				return i;
			}
		}
		throw new ProbabilityError(this);
	}

	public void computeStats(final CumulativeDistribution stats) {
	}

	@Override
	public String toString() {
		final StringBuffer s = new StringBuffer(getClass().getSimpleName());
		for (int i = 0; i < p.length; i++) {
			s.append(" p[" + i + "]:" + p[i]);
		}
		return s.toString();
	}

	public static class ProbabilityError extends Error {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ProbabilityError(final DiscreteProbabilityDistribution p) {
			super("Probabilities do not sum to 1: " + p);
		}

	}

}
