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
import java.util.Iterator;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.stat.HistoricalReport;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Utils;

/**
 * <p>
 * An implementation of the Gjerstad Dickhaut strategy. Agents using this
 * strategy calculate the probability of any bid being accepted and bid to
 * maximize expected profit. See
 * </p>
 * <p>
 * "Price Formation in Double Auctions" S. Gjerstad, J. Dickhaut and R. Palmer
 * </p>
 * 
 * <p>
 * Note that you must configure a logger of type HistoricalDataReport in order
 * to use this strategy.
 * </p>
 * 
 * <p>
 * Because the auction framework in JCAT differs from the one used in the above
 * paper and the one used in JASA, several alterations have been made:
 * <ul>
 * <li>Shouts in JCAT are persistent and do not expire until the end of a
 * trading day, nor can it be withdrawn. In certain scenarios, a GD may generate
 * a price which is less competitive than that of its existing shout and gets
 * rejected in a market requiring shout improvement. If no any other shouts or
 * transactions are made in the market, this GD trader may fail to sweeten its
 * offer although it still has a huge profit margin, which lowers the efficiency
 * of the market. To avoid this situation, the price of the stand shout will be
 * used to replace the latest calculated price. Shouting at the same price
 * repeatedly will graduately lower the probability that GD expects the shout to
 * be matched and eventually lead to crossing shouts and transactions.</li>
 * <li>The original implementation of GD, from JASA, interpolates between two
 * prices by 1 price unit, which may be too big. A smaller interpolation
 * distance, down to {@link AbstractStrategy#MIN_PRICE_DIFFERENCE} is
 * automatically used when the two end points are close.</li>
 * <li>JASA clears the record of matched shouts after each trading round. It is
 * unclear why JASA does it. Doing this in JCAT lowers the efficiency of market
 * as expected.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.maxprice</tt><br>
 * <font size=-1>double &gt;= 0 (200 by default) </font></td>
 * <td valign=top>(max price in auction)</td>
 * </tr>
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
 * <td valign=top><tt>gd_strategy</tt></td>
 * </tr>
 * </table>
 * 
 * @see edu.cuny.cat.stat.HistoricalReport
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.30 $
 */

public class GDStrategy extends FixedQuantityStrategyImpl implements
		Serializable, Prototypeable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(GDStrategy.class);

	public static final String P_DEF_BASE = "gd_strategy";

	public static final String P_MAXPRICE = "maxprice";

	public static final String P_HISTORY = "history";

	public static double DEFAULT_MAX_PRICE = 200;

	/**
	 * the minimum number of points to interpolate between two prices unless the
	 * two prices are too close to do so
	 * 
	 * @see AbstractStrategy#MIN_PRICE_DIFFERENCE
	 */
	public static int MIN_INTERPOLATION_POINTS = 5;

	protected double maxPrice = GDStrategy.DEFAULT_MAX_PRICE;

	protected double maxPoint = 0;

	protected double max = 0;

	protected double maxProb = 0;

	protected HistoricalReport historicalReport;

	public GDStrategy() {
		historicalReport = new HistoricalReport();
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(GDStrategy.P_DEF_BASE);
		maxPrice = parameters.getDoubleWithDefault(
				base.push(GDStrategy.P_MAXPRICE), defBase.push(GDStrategy.P_MAXPRICE),
				maxPrice);

		historicalReport.setup(parameters, base.push(GDStrategy.P_HISTORY));
		historicalReport.initialize();
	}

	public HistoricalReport getHistoricalReport() {
		return historicalReport;
	}

	/**
	 * TODO: need update to correctly clone all the configuration.
	 */
	@Override
	public Object protoClone() {
		final GDStrategy clone = new GDStrategy();
		return clone;
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		/* now send event to historical report first */
		if (historicalReport != null) {
			historicalReport.eventOccurred(event);
		}

		super.eventOccurred(event);
	}

	@Override
	public boolean requiresAuctionHistory() {
		return true;
	}

	@Override
	public boolean modifyShout(final Shout.MutableShout shout) {

		super.modifyShout(shout);

		final Iterator<Shout> sortedShouts = historicalReport.sortedShoutIterator();

		double lastPoint = 0;
		double lastP = 0;
		double currentPoint = 0;
		double currentP = 0;
		maxPoint = 0;
		max = 0;

		if (!agent.isBuyer()) {
			lastP = 1;
			currentP = 1;
		}
		
		while (sortedShouts.hasNext()) {
			final Shout nextShout = sortedShouts.next();
			if (nextShout.getPrice() > lastPoint) {			
				currentPoint = nextShout.getPrice();
				currentP = calculateProbability(currentPoint);
				getMax(lastPoint, lastP, currentPoint, currentP);
				lastPoint = currentPoint;
				lastP = currentP;
			}
		}

		currentPoint = maxPrice;
		currentP = 1;
		if (!agent.isBuyer()) {
			currentP = 0;
		}
		getMax(lastPoint, lastP, currentPoint, currentP);

		// set quote
		if (maxPoint > 0) {
			// NOTE: because jcat doesn't allow widthdrawing a shout, the new shout
			// needs to improve the current one.

			if (agent.isActive()) {
				if (agent.isBuyer() && (shout.getPrice() > maxPoint)) {
					// do nothing
					//
					// if (historicalReport.isDebugging()) {
					// logger.info(agent.getTraderId() + " tries to bid a lower price "
					// + maxPoint + ", use the current price " + shout.getPrice()
					// + " instead.");
					// }
				} else if (agent.isSeller() && (shout.getPrice() < maxPoint)) {
					// do nothing
					//
					// if (historicalReport.isDebugging()) {
					// logger.info(agent.getTraderId() + " tries to offer a higher price "
					// + maxPoint + ", use the current price " + shout.getPrice()
					// + " instead.");
					// }
				} else {
					shout.setPrice(maxPoint);
				}
			} else {
				shout.setPrice(maxPoint);
			
			}

			return true;
		} else {
			return false;
		}
	}

	public double calculateProbability(final double price) {

		double prob = 0;

		// (taken bids below price) + (all asks below price)
		// -------------------------------------------------------------------------
		// (taken bids below price) + (all asks below price) + (rejected bids above
		// price)
		if (agent.isBuyer()) {
			// return ((double) (historyStats.getNumberOfBids(-1 * price, true) +
			// historyStats
			// .getNumberOfAsks(-1 * price, false)))
			// / ((double) (historyStats.getNumberOfBids(-1 * price, true)
			// + historyStats.getNumberOfAsks(-1 * price, false) + (historyStats
			// .getNumberOfBids(price, false) - historyStats.getNumberOfBids(
			// price, true))));

			final int TBL = historicalReport.getIncreasingQueryAccelerator()
					.getNumOfAcceptedBidsBelow(price);
			final int AL = historicalReport.getIncreasingQueryAccelerator()
					.getNumOfAsksBelow(price);
			final int RBG = historicalReport.getIncreasingQueryAccelerator()
					.getNumOfRejectedBidsAbove(price);

			if (TBL != historicalReport.getNumberOfBids(-1 * price, true)) {
				GDStrategy.logger.error("****************** Wrong TBL!");
			}

			if (AL != historicalReport.getNumberOfAsks(-1 * price, false)) {
				GDStrategy.logger.error("****************** Wrong AL!");
			}

			if (RBG != historicalReport.getNumberOfBids(price, false)
					- historicalReport.getNumberOfBids(price, true)) {
				GDStrategy.logger.error("****************** Wrong RBG!");
			}

			prob = (double) (TBL + AL) / (TBL + AL + RBG);

		} else {
			// (taken asks above price) + (all bids above price)
			// -------------------------------------------------------------------
			// (taken asks above price) + (all bids above price) + (rejected asks
			// below price)

			// return ((double) (historyStats.getNumberOfAsks(price, true) +
			// historyStats
			// .getNumberOfBids(price, false)))
			// / ((double) (historyStats.getNumberOfAsks(price, true)
			// + historyStats.getNumberOfBids(price, false) + (historyStats
			// .getNumberOfAsks(-1 * price, false) - historyStats
			// .getNumberOfAsks(-1 * price, true))));

			final int TAG = historicalReport.getIncreasingQueryAccelerator()
					.getNumOfAcceptedAsksAbove(price);
			final int BG = historicalReport.getIncreasingQueryAccelerator()
					.getNumOfBidsAbove(price);
			final int RAL = historicalReport.getIncreasingQueryAccelerator()
					.getNumOfRejectedAsksBelow(price);

			if (TAG != historicalReport.getNumberOfAsks(price, true)) {
				GDStrategy.logger.error("****************** Wrong TAG!");
			}

			if (BG != historicalReport.getNumberOfBids(price, false)) {
				GDStrategy.logger.error("****************** Wrong BG!");
			}

			if (RAL != historicalReport.getNumberOfAsks(-1 * price, false)
					- historicalReport.getNumberOfAsks(-1 * price, true)) {
				GDStrategy.logger.error("****************** Wrong RAL!");
			}

			prob = (double) (TAG + BG) / (TAG + BG + RAL);
		}

		// if (historicalReport.isDebugging()) {
		// logger.info(Utils.format(price) + " prob: " + Utils.format(prob));
		// }

		return prob;
	}

	protected void getMax(double a1, final double p1, double a2, final double p2) {

		if (a1 > maxPrice) {
			a1 = maxPrice;
		}

		if (a2 > maxPrice) {
			a2 = maxPrice;
		}

		final double pvalue = agent.getPrivateValue();

		// double denom = (-6 * a1 * a1 * a2 * a2) + (4 * a1 * a1 * a1 * a2)
		// + (4 * a1 * a2 * a2 * a2) + (-1 * a1 * a1 * a1 * a1)
		// + (-1 * a2 * a2 * a2 * a2);
		// double alpha3 = (2 * ((a1 * (p1 - p2)) + (a2 * (p2 - p1)))) / denom;
		// double alpha2 = (3 * ((a1 * a1 * (p2 - p1)) + (a2 * a2 * (p1 - p2))))
		// / denom;
		// double alpha1 = (6 * (p1 - p2) * ((a1 * a1 * a2) - (a1 * a2 * a2))) /
		// denom;
		// double alpha0 = ((p1 * ((4 * a1 * a2 * a2 * a2) + (-3 * a1 * a1 * a2 *
		// a2) + (-1
		// * a2 * a2 * a2 * a2))) + (p2 * ((4 * a1 * a1 * a1 * a2)
		// + (-3 * a1 * a1 * a2 * a2) + (-1 * a1 * a1 * a1 * a1))))
		// / denom;
		final double a11 = a1 * a1;
		final double a1111 = a11 * a11;
		final double a22 = a2 * a2;
		final double a2222 = a22 * a22;
		final double a1122 = a11 * a22;
		final double a12 = a1 * a2;
		final double a1112 = a11 * a12;
		final double a1222 = a12 * a22;
		final double p12 = p1 - p2;

		final double denom = (-6 * a1122) + 4 * (a1112 + a1222) - a1111 - a2222;
		final double alpha3 = (2 * ((a1 - a2) * p12)) / denom;
		final double alpha2 = (3 * (a22 - a11) * p12) / denom;
		final double alpha1 = (6 * p12 * (a12 * (a1 - a2))) / denom;
		final double alpha0 = ((p1 * ((4 * a1222) - 3 * a1122 - a2222)) + (p2 * ((4 * a1112)
				- 3 * a1122 - a1111)))
				/ denom;
		//
		//    
		double temp = 0;

		double p = 0;

		double start = a1;
		double end = a2;
		if (agent.isBuyer()) {
			if (a2 > pvalue) {
				end = pvalue;
			}
		} else {
			if (a1 < pvalue) {
				start = pvalue;
			}
		}

		// when start and end are too close to interpolate, use smaller steps until
		// it's too small to go any further.
		double step = 1.0;
		if ((start < end)
				&& (start + GDStrategy.MIN_INTERPOLATION_POINTS * step > end)) {
			step = (end - start) / GDStrategy.MIN_INTERPOLATION_POINTS;
			if (step < AbstractStrategy.MIN_PRICE_DIFFERENCE) {
				step = AbstractStrategy.MIN_PRICE_DIFFERENCE;
			}

			// if (historicalReport.isDebugging()) {
			// logger.info(Utils.indent(agent.getTraderId()
			// + " now uses interpolation step: " + step, "\t\t"));
			// logger.info(Utils.indent("[" + start + ", " + end + ")", "\t\t"));
			// }
		} else {
			// if (historicalReport.isDebugging()) {
			// logger.info(Utils.indent("[" + start + ", " + end + ")", "\t\t"));
			// }
		}

		for (double i = start; i < end; i += step) {
			p = (alpha3 * i * i * i) + (alpha2 * i * i) + (alpha1 * i) + alpha0;

			// due to the nature of computerized calculation, p may fall a little bit
			// outside the arrange, adjust it back.
			if (p > Math.max(p1, p2)) {
				p = Math.max(p1, p2);
			} else if (p < Math.min(p1, p2)) {
				p = Math.min(p1, p2);
			}

			if (agent.isBuyer()) {
				temp = p * (pvalue - i);
			} else {
				temp = p * (i - pvalue);
			}

			if (temp > max) {
				max = temp;
				maxPoint = i;
				maxProb = p;
			}
		}
	}

	@Override
	public String toString() {
		String s = super.toString();

		s += " " + GDStrategy.P_MAXPRICE + ":" + maxPrice;
		s += "\n" + Utils.indent(historicalReport.toString());

		return s;
	}
}
