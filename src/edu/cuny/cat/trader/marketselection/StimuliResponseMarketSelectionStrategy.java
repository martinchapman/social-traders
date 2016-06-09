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

import edu.cuny.ai.learning.StimuliResponseLearner;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * <p>
 * An adaptive market selection strategy using a stimuli response learner, which
 * learns by receiving the agent's profit as reward and makes decision on
 * choosing market.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.learner</tt><br>
 * <font size=-1>name of class, implementing
 * {@link edu.cuny.ai.learning.StimuliResponseLearner}</font></td>
 * <td valign=top>(the learning algorithm to adapt market selection)</td>
 * <tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.14 $
 * 
 */

public class StimuliResponseMarketSelectionStrategy extends
		AdaptiveMarketSelectionStrategy {

	static Logger logger = Logger
			.getLogger(StimuliResponseMarketSelectionStrategy.class);

	protected StimuliResponseLearner srLearner;

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		if (learner instanceof StimuliResponseLearner) {
			srLearner = (StimuliResponseLearner) learner;
		}
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);
		if (event instanceof DayClosedEvent) {
			if (hasValidCurrentMarket()) {
				reward(agent.getLastDayProfit());
			}
		}
	}

	public void reward(final double reward) {
		srLearner.reward(reward);
	}
}
