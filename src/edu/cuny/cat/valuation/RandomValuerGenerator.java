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

package edu.cuny.cat.valuation;

import org.apache.log4j.Logger;

import cern.jet.random.AbstractDistribution;
import edu.cuny.cat.Game;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.random.StateCopyable;
import edu.cuny.random.Uniform;
import edu.cuny.util.Galaxy;
import edu.cuny.util.ParamClassLoadException;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * This valuer generator creates valuation policies in which we randomly
 * determine our valuation across all auctions and all units at
 * agent-initialisation time. Valuations are drawn from a certain distribution
 * with the specified range.
 * 
 * </p>
 * <p>
 * <b>Parameters </b>
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.minvalue</tt><br>
 * <font size=-1>double &gt;= 0 </font></td>
 * <td valign=top>(the minimum valuation)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.maxvalue</tt><br>
 * <font size=-1>double &gt;=0 </font></td>
 * <td valign=top>(the maximum valuation)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.distribution</tt><br>
 * <font size=-1>full name of class inheriting
 * <code>cern.jet.random.AbstractDistribution</code></font></td>
 * <td valign=top>(the distribution used for choosing valuation, e.g.
 * {@link edu.cuny.random.ChiSquare})</td>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>random_valuer</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 */

public class RandomValuerGenerator implements ValuerGenerator {

	/**
	 * The minimum valuation to use.
	 */
	protected double minValue;

	/**
	 * The maximum valuation to use.
	 */
	protected double maxValue;

	/**
	 * The template distribution for generating distributions in
	 * {@link RandomValuer}.
	 */
	AbstractDistribution distribution = null;

	public static final String P_DEF_BASE = "random_valuer";

	public static final String P_MINVALUE = "minvalue";

	public static final String P_MAXVALUE = "maxvalue";

	public static final String P_DISTRIBUTION = "distribution";

	static Logger logger = Logger.getLogger(RandomValuerGenerator.class);

	public RandomValuerGenerator() {
	}

	public RandomValuerGenerator(final double minValue, final double maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(RandomValuerGenerator.P_DEF_BASE);

		minValue = parameters.getDouble(
				base.push(RandomValuerGenerator.P_MINVALUE), defBase
						.push(RandomValuerGenerator.P_MINVALUE), 0);
		maxValue = parameters.getDouble(
				base.push(RandomValuerGenerator.P_MAXVALUE), defBase
						.push(RandomValuerGenerator.P_MAXVALUE), minValue);

		try {
			distribution = parameters.getInstanceForParameterEq(base
					.push(RandomValuerGenerator.P_DISTRIBUTION), defBase
					.push(RandomValuerGenerator.P_DISTRIBUTION),
					AbstractDistribution.class);

			if (distribution instanceof Parameterizable) {
				((Parameterizable) distribution).setup(parameters, base
						.push(RandomValuerGenerator.P_DISTRIBUTION));
			}
		} catch (final ParamClassLoadException e) {
			distribution = new Uniform(minValue, maxValue, Galaxy.getInstance()
					.getDefaultTyped(GlobalPRNG.class).getEngine());
		}

		checkDistribution(distribution);
	}

	protected boolean checkDistribution(final AbstractDistribution distribution) {
		if (distribution instanceof StateCopyable) {
			return true;
		} else {
			RandomValuerGenerator.logger.fatal("Distribution must be "
					+ StateCopyable.class.getSimpleName());
			return false;
		}
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(final double maxValue) {
		this.maxValue = maxValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(final double minValue) {
		this.minValue = minValue;
	}

	public void setDistribution(final AbstractDistribution distribution) {
		if (checkDistribution(distribution)) {
			this.distribution = distribution;
		}
	}

	public AbstractDistribution getDistribution() {
		return distribution;
	}

	protected AbstractDistribution createDistribution() {

		AbstractDistribution dist = null;
		try {
			// NOTE: cloning the template distribution is not used before it will
			// cause all generated distributions to have a random engine in the same
			// situation and thus generate the same random numbers.

			if (distribution != null) {
				dist = distribution.getClass().newInstance();
				if (dist instanceof StateCopyable) {
					((StateCopyable) dist).copyStateFrom(distribution);
				} else {
					dist = null;
				}
			}
		} catch (final InstantiationException e) {
			e.printStackTrace();
			dist = null;
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
			dist = null;
		}

		if (dist == null) {
			dist = new Uniform(minValue, maxValue, Galaxy.getInstance().getTyped(
					Game.P_CAT, GlobalPRNG.class).getEngine());
		}

		return dist;
	}

	public synchronized ValuationPolicy createValuer() {
		final RandomValuer valuer = new RandomValuer();
		valuer.setGenerator(this);
		valuer.setDistribution(createDistribution());
		valuer.drawRandomValue();
		return valuer;
	}

	public void eventOccurred(final AuctionEvent event) {
		// do nothing
	}

	public void reset() {
		// do nothing
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();
		s += "\n" + Utils.indent("minValue:" + minValue);
		s += "\n" + Utils.indent("maxValue:" + maxValue);
		s += "\n" + Utils.indent("distribution:" + distribution);

		return s;
	}
}
