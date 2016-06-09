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

package edu.cuny.cat.trader.strategy;

import java.io.Serializable;

import cern.jet.random.AbstractContinousDistribution;
import cern.jet.random.Uniform;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.trader.AbstractTradingAgent;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * <p>
 * A trading strategy that in which we bid a different random markup on our
 * agent's private value in each auction round. This strategy is often referred
 * to as Zero Intelligence Constrained (ZI-C) in the literature.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.maxmarkup</tt><br>
 * <font size=-1>double &gt;= 0</font></td>
 * <td valign=top>(the maximum markup to bid for)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>random_constrained_strategy</tt></td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.14 $
 */

public class RandomConstrainedStrategy extends FixedQuantityStrategyImpl
		implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected double maxMarkup = RandomConstrainedStrategy.DEFAULT_MARKUP;

	protected AbstractContinousDistribution markupDistribution;

	public static final String P_DEF_BASE = "random_constrained_strategy";

	public static final String P_MAX_MARKUP = "maxmarkup";

	public static final double DEFAULT_MARKUP = 50;

	public RandomConstrainedStrategy() {
		this(null, RandomConstrainedStrategy.DEFAULT_MARKUP);
	}

	public RandomConstrainedStrategy(final AbstractTradingAgent agent,
			final double maxMarkup) {
		super(agent);
		this.maxMarkup = maxMarkup;
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);
		maxMarkup = parameters.getDoubleWithDefault(base
				.push(RandomConstrainedStrategy.P_MAX_MARKUP), new Parameter(
				RandomConstrainedStrategy.P_DEF_BASE)
				.push(RandomConstrainedStrategy.P_MAX_MARKUP), maxMarkup);
	}

	@Override
	public void initialize() {
		super.initialize();

		markupDistribution = new Uniform(0, maxMarkup, Galaxy.getInstance()
				.getDefaultTyped(GlobalPRNG.class).getEngine());
	}

	@Override
	public boolean modifyShout(final Shout.MutableShout shout) {

		final double markup = markupDistribution.nextDouble();
		double price = 0;
		if (agent.isBuyer()) {
			price = agent.getPrivateValue() - markup;
		} else {
			price = agent.getPrivateValue() + markup;
		}
		if (price > 0) {
			shout.setPrice(price);
		} else {
			shout.setPrice(0);
		}
		shout.setQuantity(quantity);

		return super.modifyShout(shout);
	}

	public double getMaxMarkup() {
		return maxMarkup;
	}

	public void setMaxMarkup(final double maxMarkup) {
		this.maxMarkup = maxMarkup;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += " " + RandomConstrainedStrategy.P_MAX_MARKUP + ":" + maxMarkup;
		return s;
	}

}
