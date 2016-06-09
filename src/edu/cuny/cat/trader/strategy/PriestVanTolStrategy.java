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

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.stat.HistoricalReport;
import edu.cuny.cat.trader.AbstractTradingAgent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Utils;

/**
 * 
 * An implementation of the Priest and van Tol strategy. <b>Please note however
 * that the performance of this implementation is significantly worse than what
 * they claimed.</b>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.history</tt><br>
 * <font size=-1> </font></td>
 * <td valign=top>(the parameter base for
 * {@link edu.cuny.cat.stat.HistoricalReport} used by this strategy)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>pvt_strategy</tt></td>
 * </tr>
 * </table>
 * 
 * @see edu.cuny.cat.stat.HistoricalReport
 * 
 * @author Steve Phelps
 * @version $Revision: 1.7 $
 */

public class PriestVanTolStrategy extends MomentumStrategy implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(PriestVanTolStrategy.class);

	public static final String P_DEF_BASE = "pvt_strategy";

	public static final String P_HISTORY = "history";

	protected HistoricalReport historicalReport;

	public PriestVanTolStrategy() {
		this(null);
	}

	public PriestVanTolStrategy(final AbstractTradingAgent agent) {
		super(agent);

		historicalReport = new HistoricalReport();
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		historicalReport.setup(parameters, base
				.push(PriestVanTolStrategy.P_HISTORY));
		historicalReport.initialize();
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		if (historicalReport != null) {
			historicalReport.eventOccurred(event);
		}

		super.eventOccurred(event);
	}

	/**
	 * the implementation is slightly different from the one in jasa:
	 * 
	 * here, when a trader is inactive, it never reduces its profit margin, not
	 * even letting its learner to learn; while in jasa, the trader's learner
	 * continues to learn, though the current price remains the same. The
	 * additional learning of reduced margins in jasa would be reflected on the
	 * next trading day, however, when the trader becomes active again.
	 * 
	 * This implementation is believed what Priest and van Tol meant to do, since
	 * the very aim is not to cause those inactive traders to reduce profit
	 * margins when the traders become active again.
	 */
	@Override
	protected void adjustMargin() {

		final double aMin = historicalReport.getLowestUnacceptedAskPrice();
		final double bMax = historicalReport.getHighestUnacceptedBidPrice();

		double targetMargin = 0.0;

		// 1. calculate targeted margin
		if (agent.isBuyer()) {
			if (aMin > bMax) {
				targetMargin = targetMargin(bMax + perterb(bMax));
			} else {
				targetMargin = targetMargin(aMin + perterb(aMin));
			}
		} else {
			if (aMin > bMax) {
				targetMargin = targetMargin(aMin - perterb(aMin));
			} else {
				targetMargin = targetMargin(bMax - perterb(bMax));
			}
		}

		// logger.info("targetMargin: " + targetMargin);

		// 2. if adjustable, adjust margin to targeted margin
		if (agent.isActive()) {
			adjustMargin(targetMargin);
		} else {
			final double price = calculatePrice(targetMargin);
			if (agent.isBuyer() && (price > currentPrice)) {
				adjustMargin(targetMargin);
			} else if (agent.isSeller() && (price < currentPrice)) {
				adjustMargin(targetMargin);
			}
		}
	}

	@Override
	public String toString() {
		String s = super.toString();

		s += "\n" + Utils.indent(historicalReport.toString());

		return s;
	}

}