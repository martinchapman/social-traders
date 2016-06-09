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

package edu.cuny.cat.market.charging;

import org.apache.log4j.Logger;

import edu.cuny.ai.learning.MimicryLearner;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Resetable;
import edu.cuny.util.Utils;

/**
 * <p>
 * TODO: does not work well for the moment.
 * 
 * 1. use a SlidingWindowLearner to smooth exploring factor
 * 
 * 2. use a sliding distribution to oversight the variance of a certain number
 * of consecutive exploring factors.
 * </p>
 * 
 * <p>
 * a trader exploration monitor that adjusts its threshold value based on a
 * learner on the series of exploring factors.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.learner</tt><br>
 * <font size=-1>name of class, implementing
 * {@link edu.cuny.ai.learning.MimicryLearner}</font></td>
 * <td valign=top>(a learner to determine a threshold)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * 
 * <table>
 * <tr>
 * <td valign=top><tt>sliding_trader_exploring_monitor</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.12 $
 * 
 */

public class SlidingTraderExploringMonitor extends SingleDayExploringMonitor
		implements Resetable {

	public static final String P_DEF_BASE = "sliding_trader_exploring_monitor";

	public static final String P_LEARNER = "learner";

	protected MimicryLearner learner;

	static Logger logger = Logger.getLogger(SlidingTraderExploringMonitor.class);

	public SlidingTraderExploringMonitor() {
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(
				SlidingTraderExploringMonitor.P_DEF_BASE);

		learner = parameters.getInstanceForParameter(base
				.push(SlidingTraderExploringMonitor.P_LEARNER), defBase
				.push(SlidingTraderExploringMonitor.P_LEARNER), MimicryLearner.class);
		if (learner instanceof Parameterizable) {
			((Parameterizable) learner).setup(parameters, base
					.push(SlidingTraderExploringMonitor.P_LEARNER));
		}
		learner.initialize();
	}

	public void reset() {
		learner.reset();
	}

	@Override
	public double getExploringThreshold() {
		return learner.act();
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof DayClosedEvent) {
			learner.train(getExploringFactor());
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "\n" + Utils.indent(learner.toString());
	}
}