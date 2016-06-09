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

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.trader.AbstractTradingAgent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * <p>
 * A class representing a strategy in which we adapt our bids using a discrete
 * learning algorithm.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.markupscale</tt><br>
 * <font size=-1>double &gt;= 0</font></td>
 * <td valign=top>(scaling factor by which to multiply the output from the
 * learner)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>discrete_learner_strategy</tt></td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.15 $
 */

public abstract class DiscreteLearnerStrategy extends AdaptiveStrategyImpl
		implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A scaling factor used to multiply-up the output from the learning
	 * algorithm.
	 */
	protected double markupScale = 1;

	public static final String P_DEF_BASE = "discrete_learner_strategy";

	static final String P_MARKUPSCALE = "markupscale";

	static Logger logger = Logger.getLogger(DiscreteLearnerStrategy.class);

	public DiscreteLearnerStrategy() {
		this(null);
	}

	public DiscreteLearnerStrategy(final AbstractTradingAgent agent) {
		super(agent);
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);
		markupScale = parameters.getDoubleWithDefault(base
				.push(DiscreteLearnerStrategy.P_MARKUPSCALE), new Parameter(
				DiscreteLearnerStrategy.P_DEF_BASE)
				.push(DiscreteLearnerStrategy.P_MARKUPSCALE), markupScale);
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof RoundClosedEvent) {
			if (agent.isActive()) {
				learn();
			}
		}
	}

	@Override
	public boolean modifyShout(final Shout.MutableShout shout) {

		// Generate an action from the learning algorithm
		final int action = act();

		// Now turn the action into a price
		double price;
		if (agent.isSeller()) {
			price = agent.getPrivateValue() + action * markupScale;
		} else {
			price = agent.getPrivateValue() - action * markupScale;
		}
		if (price < 0) {
			// logger.debug(this + ": set negative price- clipping at 0");
			price = 0;
		}

		shout.setPrice(price);
		shout.setQuantity(quantity);

		return super.modifyShout(shout);
	}

	public double getMarkupScale() {
		return markupScale;
	}

	public void setMarkupScale(final double markupScale) {
		this.markupScale = markupScale;
	}

	/**
	 * Generate an action from the learning algorithm.
	 */
	public abstract int act();

	/**
	 * Perform learning.
	 */
	public abstract void learn();

	@Override
	public String toString() {
		String s = super.toString();
		s += " " + DiscreteLearnerStrategy.P_MARKUPSCALE + ":" + markupScale;
		return s;
	}

}
