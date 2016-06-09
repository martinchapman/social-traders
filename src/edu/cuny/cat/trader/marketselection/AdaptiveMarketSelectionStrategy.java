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

import java.util.Collection;

import org.apache.log4j.Logger;

import edu.cuny.ai.learning.DiscreteLearner;
import edu.cuny.cat.core.Specialist;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * An adaptive market selection strategy using a discrete learner, which learns
 * and makes decision on choosing market.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.learner</tt><br>
 * <font size=-1>name of class, implementing
 * {@link edu.cuny.ai.learning.DiscreteLearner}</font></td>
 * <td valign=top>(the learning algorithm to adapt market selection)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>adaptive_market_selection_strategy</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.19 $
 * 
 */

public class AdaptiveMarketSelectionStrategy extends
		AbstractMarketSelectionStrategy {

	static Logger logger = Logger
			.getLogger(AdaptiveMarketSelectionStrategy.class);

	public static final String P_LEARNER = "learner";

	public static final String P_DEF_BASE = "adaptive_market_selection_strategy";

	protected DiscreteLearner learner;

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(
				AdaptiveMarketSelectionStrategy.P_DEF_BASE);
		learner = parameters
				.getInstanceForParameter(base
						.push(AdaptiveMarketSelectionStrategy.P_LEARNER), defBase
						.push(AdaptiveMarketSelectionStrategy.P_LEARNER),
						DiscreteLearner.class);
		if (learner instanceof Parameterizable) {
			((Parameterizable) learner).setup(parameters, base
					.push(AdaptiveMarketSelectionStrategy.P_LEARNER));
		}
		learner.initialize();
	}

	@Override
	public void reset() {
		super.reset();
		learner.reset();
	}

	@Override
	protected void setupMarkets(final Collection<Specialist> marketColl) {
		super.setupMarkets(marketColl);
		learner.setNumberOfActions(markets.length);
		learner.initialize();
		learner.reset();
	}

	@Override
	public void selectMarket() {
		currentMarketIndex = learner.act(activeMarkets);
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n" + Utils.indent(learner.toString());
		return s;
	}
}
