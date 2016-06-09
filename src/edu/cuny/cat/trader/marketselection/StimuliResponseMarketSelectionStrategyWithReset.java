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

package edu.cuny.cat.trader.marketselection;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.random.Uniform;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Utils;

/**
 * <p>
 * An adaptive market selection strategy using a stimuli response learner, which
 * learns by receiving the agent's profit as reward and makes decision on
 * choosing market, and resets its memory daily with a probability.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.prob</tt><br>
 * <font size=-1>double in [0,1] (0.1 by default)</font></td>
 * <td valign=top>(the probability with which the strategy resets the
 * information it learned each day)</td>
 * <tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.14 $
 * 
 */

public class StimuliResponseMarketSelectionStrategyWithReset extends
		StimuliResponseMarketSelectionStrategy {

	static Logger logger = Logger
			.getLogger(StimuliResponseMarketSelectionStrategyWithReset.class);

	public final static String P_PROB = "prob";

	public final static double DEFAULT_PROBABILITY = 0.1;

	protected double prob = StimuliResponseMarketSelectionStrategyWithReset.DEFAULT_PROBABILITY;

	protected Uniform uniform;

	public StimuliResponseMarketSelectionStrategyWithReset() {
		uniform = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		// NOTE: use the default base of parent class
		final Parameter defBase = new Parameter(
				AdaptiveMarketSelectionStrategy.P_DEF_BASE);

		prob = parameters.getDoubleWithDefault(base
				.push(StimuliResponseMarketSelectionStrategyWithReset.P_PROB), defBase
				.push(StimuliResponseMarketSelectionStrategyWithReset.P_PROB), prob);
		if ((prob > 1) || (prob < 0)) {
			StimuliResponseMarketSelectionStrategyWithReset.logger
					.error("Resetting probability must be between 0 and 1 inclusively !");
			StimuliResponseMarketSelectionStrategyWithReset.logger
					.error("Use default value instead.");
			prob = StimuliResponseMarketSelectionStrategyWithReset.DEFAULT_PROBABILITY;
		}
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);
		if (event instanceof DayClosedEvent) {
			final double rand = uniform.nextDouble();
			if (rand < prob) {
				srLearner.reset();
			}
		}
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n"
				+ Utils.indent(StimuliResponseMarketSelectionStrategyWithReset.P_PROB
						+ ":" + prob);
		return s;
	}
}
