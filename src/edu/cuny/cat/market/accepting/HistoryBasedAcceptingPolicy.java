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

package edu.cuny.cat.market.accepting;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.IllegalShoutException;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.stat.HistoricalReport;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Utils;

/**
 * <p>
 * This accepting policy calculates the probability of a shout to be matched as
 * {@link edu.cuny.cat.trader.strategy.GDStrategy} does based on
 * {@link edu.cuny.cat.stat.HistoricalReport}, and compares it against a
 * specified threshold. If the probability is higher than the threshold, the
 * shout will be accepted; otherwise rejected.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.threshold</tt><br>
 * <font size=-1>0 <=double <=1 (0.5 by default) </font></td>
 * <td valign=top>(the threshold probability to accept a shout)</td>
 * <tr>
 * </table>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.history</tt><br>
 * <font size=-1> </font></td>
 * <td valign=top>(the parameter base for
 * {@link edu.cuny.cat.stat.HistoricalReport} used by this policy)</td>
 * </tr>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>history_based_accepting</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class HistoryBasedAcceptingPolicy extends
		OnlyNewShoutDecidingAcceptingPolicy {

	static Logger logger = Logger.getLogger(HistoryBasedAcceptingPolicy.class);

	public static final String P_DEF_BASE = "history_based_accepting";

	public static final String P_HISTORY = "history";

	public static final String P_THRESHOLD = "threshold";

	public static final double DEFAULT_THRESHOLD = 0.5;

	protected double threshold;

	protected HistoricalReport report;

	public HistoryBasedAcceptingPolicy() {
		report = new HistoricalReport();
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(
				HistoryBasedAcceptingPolicy.P_DEF_BASE);

		threshold = parameters.getDoubleWithDefault(base
				.push(HistoryBasedAcceptingPolicy.P_THRESHOLD), defBase
				.push(HistoryBasedAcceptingPolicy.P_THRESHOLD),
				HistoryBasedAcceptingPolicy.DEFAULT_THRESHOLD);

		report.setup(parameters, base.push(HistoryBasedAcceptingPolicy.P_HISTORY));
		report.initialize();
	}

	/**
	 * TODO: currently historicalreport resets itself on game starting, which
	 * should be changed to comply with the principle followed elsewhere. It does
	 * not seem to hurt if historicalreport is reset here one more time.
	 */
	@Override
	public void reset() {
		super.reset();

		report.reset();
	}

	/**
	 * accepts only those shouts that will be matched with a probability higher
	 * than {@link #threshold}.
	 * 
	 * @see edu.cuny.cat.market.accepting.OnlyNewShoutDecidingAcceptingPolicy#check(edu.cuny.cat.core.Shout)
	 */
	@Override
	public void check(final Shout shout) throws IllegalShoutException {
		final double prob = calculateProbability(shout);

		// logger.info(shout.toPrettyString() + "\t" +
		// Utils.formatter.format(prob));

		if (prob < threshold) {
			throw new IllegalShoutException(
					"Shout is expected to be matched with too low probability !");
		}
	}

	protected double calculateProbability(final Shout shout) {

		if (shout.isBid()) {
			// TODO: to deal with the case when the denominator is 0
			return ((double) (report.getIncreasingQueryAccelerator()
					.getNumOfAcceptedBidsBelow(shout.getPrice()) + report
					.getIncreasingQueryAccelerator().getNumOfAsksBelow(shout.getPrice())))
					/ ((double) (report.getIncreasingQueryAccelerator()
							.getNumOfAcceptedBidsBelow(shout.getPrice())
							+ report.getIncreasingQueryAccelerator().getNumOfAsksBelow(
									shout.getPrice()) + report.getIncreasingQueryAccelerator()
							.getNumOfRejectedBidsAbove(shout.getPrice())));

		} else {
			// TODO: to deal with the case when the denominator is 0
			return ((double) (report.getIncreasingQueryAccelerator()
					.getNumOfAcceptedAsksAbove(shout.getPrice()) + report
					.getIncreasingQueryAccelerator().getNumOfBidsAbove(shout.getPrice())))
					/ ((double) (report.getIncreasingQueryAccelerator()
							.getNumOfAcceptedAsksAbove(shout.getPrice())
							+ report.getIncreasingQueryAccelerator().getNumOfBidsAbove(
									shout.getPrice()) + report.getIncreasingQueryAccelerator()
							.getNumOfRejectedAsksBelow(shout.getPrice())));
		}
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		report.eventOccurred(event);
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += " " + "threshold:" + threshold;
		s += "\n" + Utils.indent(report.toString());

		return s;
	}
}
