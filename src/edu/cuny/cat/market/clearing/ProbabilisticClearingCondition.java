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

package edu.cuny.cat.market.clearing;

import org.apache.log4j.Logger;

import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * <p>
 * Enables a market to clear with a probability when a new shout arrives.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.threshold</tt><br>
 * <font size=-1>double [0,1] (1 by default)</font></td>
 * <td valign=top>(the probability to clear the auction following the placing of
 * a shout, the lower end of the range being clearing houses and the higher
 * being continuous double auctions)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>probabilistic_clearing</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.22 $
 * 
 */

public class ProbabilisticClearingCondition extends RoundClearingCondition {

	static Logger logger = Logger.getLogger(ProbabilisticClearingCondition.class);

	public static final String P_DEF_BASE = "probabilistic_clearing";

	public static final String P_THRESHOLD = "threshold";

	protected Uniform uniformDistribution;

	protected double threshold = 1;

	public ProbabilisticClearingCondition() {
		final RandomEngine prng = Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine();
		uniformDistribution = new Uniform(0, 1, prng);
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		threshold = parameters.getDoubleWithDefault(base
				.push(ProbabilisticClearingCondition.P_THRESHOLD), new Parameter(
				ProbabilisticClearingCondition.P_DEF_BASE)
				.push(ProbabilisticClearingCondition.P_THRESHOLD), threshold);
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof ShoutPlacedEvent) {
			final Shout shout = ((ShoutPlacedEvent) event).getShout();
			if ((shout.getSpecialist() != null)
					&& shout.getSpecialist().getId().equals(auctioneer.getName())) {
				// only deal with shouts placed at this speecialist
				final double d = uniformDistribution.nextDouble();
				if (d < threshold) {
					triggerClearing();
				}
			}
		}
	}

	public void setThreshold(final double threshold) {
		this.threshold = threshold;
	}

	public double getThreshold() {
		return threshold;
	}

	@Override
	public String toString() {
		return super.toString() + " " + ProbabilisticClearingCondition.P_THRESHOLD
				+ ":" + threshold;
	}
}
