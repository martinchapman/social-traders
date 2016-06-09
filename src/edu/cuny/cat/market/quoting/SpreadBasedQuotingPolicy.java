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

package edu.cuny.cat.market.quoting;

import org.apache.log4j.Logger;

import edu.cuny.cat.market.accepting.QuoteBeatingAcceptingPolicy;
import edu.cuny.cat.market.matching.ShoutEngine;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * <p>
 * A quoting policy extending
 * {@link edu.cuny.cat.market.quoting.DoubleSidedQuotingPolicy} that adjusts ask
 * and bid quotes when the ask quote falls below the bid quote. When that
 * happens, the ask quote is set to be a point above their average and the bid
 * quote below the average. The spread between the quotes is configurable.
 * </p>
 * 
 * <p>
 * This quoting policy is useful when
 * {@link edu.cuny.cat.market.matching.LazyMaxVolumeShoutEngine} is used.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.spread</tt><br>
 * <font size=-1>double > 0 (10 by default)</font></td>
 * <td valign=top>(the spread to be maintained between the ask quote and the bid
 * quote when the former falls below the latter.)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>spread_based_quoting</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class SpreadBasedQuotingPolicy extends DoubleSidedQuotingPolicy {

	static Logger logger = Logger.getLogger(SpreadBasedQuotingPolicy.class);

	public final static String P_SPREAD = "spread";

	public final static String P_DEF_BASE = "spread_based_quoting";

	public final static double DEFAULT_SPREAD = 10;

	protected double spread;

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(SpreadBasedQuotingPolicy.P_DEF_BASE);
		spread = parameters.getDoubleWithDefault(base
				.push(SpreadBasedQuotingPolicy.P_SPREAD), defBase
				.push(SpreadBasedQuotingPolicy.P_SPREAD),
				SpreadBasedQuotingPolicy.DEFAULT_SPREAD);
		if (spread < 0) {
			SpreadBasedQuotingPolicy.logger
					.error("Negative spread is not allowed. Default spread used instead.");
			spread = SpreadBasedQuotingPolicy.DEFAULT_SPREAD;
		}
	}

	/**
	 * gets the ask quote as the minimum of the lowest unmatched ask and the
	 * lowest matched bid. If a bid beats it, the bid will be matched with some
	 * ask.
	 * 
	 * With {@link QuoteBeatingAcceptingPolicy}, an ask beating it will be
	 * accepted.
	 * 
	 * @param shoutEngine
	 *          the shout engine processing shouts
	 * @return ask quote
	 */
	@Override
	public double askQuote(final ShoutEngine shoutEngine) {
		if (super.askQuote(shoutEngine) < super.bidQuote(shoutEngine)) {
			return (super.askQuote(shoutEngine) + super.bidQuote(shoutEngine) + spread) / 2;
		} else {
			return super.askQuote(shoutEngine);
		}
	}

	/**
	 * gets the bid quote as the maximum of the highest matched ask and the
	 * highest unmatched bid. If an ask beats it, the ask will be matched with
	 * some bid.
	 * 
	 * With {@link QuoteBeatingAcceptingPolicy}, a bid beating it will be
	 * accepted.
	 * 
	 * @param shoutEngine
	 *          the shout engine processing shouts
	 * @return bid quote
	 */
	@Override
	public double bidQuote(final ShoutEngine shoutEngine) {
		if (super.askQuote(shoutEngine) < super.bidQuote(shoutEngine)) {
			return (super.askQuote(shoutEngine) + super.bidQuote(shoutEngine) - spread) / 2;
		} else {
			return super.bidQuote(shoutEngine);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " " + SpreadBasedQuotingPolicy.P_SPREAD + ":"
				+ spread;
	}
}
