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

package edu.cuny.cat.market.pricing;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.market.MarketQuote;
import edu.cuny.util.FixedLengthQueue;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * A discriminatory pricing policy that uses the average of the last <i>n</i>
 * pair of bid and ask prices leading to transactions as the clearing price. In
 * case of the price falls out of the range between the current bid and ask, the
 * nearest boundary is used.
 * 
 * <p>
 * <b>Parameters </b>
 * </p>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.n</tt><br>
 * <font size=-1>int >= 1 (10 by default)</font></td>
 * <td valign=top>(the number of latest successful shout pairs used to determine
 * next clearing price)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>n_pricing</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class NPricingPolicy extends PricingPolicy {

	static Logger logger = Logger.getLogger(NPricingPolicy.class);

	public static final String P_N = "n";

	public static final String P_DEF_BASE = "n_pricing";

	public static final int DEFAULT_N = 10;

	protected int n;

	protected FixedLengthQueue queue;

	public NPricingPolicy() {
		this(NPricingPolicy.DEFAULT_N);
	}

	public NPricingPolicy(final int n) {
		this.n = n;
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);
		n = parameters.getIntWithDefault(base.push(NPricingPolicy.P_N),
				new Parameter(NPricingPolicy.P_DEF_BASE).push(NPricingPolicy.P_N),
				NPricingPolicy.DEFAULT_N);
	}

	@Override
	public void initialize() {
		super.initialize();

		queue = new FixedLengthQueue(2 * n);
	}

	@Override
	public void reset() {
		super.reset();
		queue.reset();
	}

	@Override
	public double determineClearingPrice(final Shout bid, final Shout ask,
			final MarketQuote clearingQuote) {

		queue.newData(bid.getPrice());
		queue.newData(ask.getPrice());
		final double avg = queue.getMean();

		final double price = (avg >= bid.getPrice()) ? bid.getPrice()
				: ((avg <= ask.getPrice()) ? ask.getPrice() : avg);

		return price;
	}

	@Override
	public String toString() {
		return super.toString() + " " + NPricingPolicy.P_N + ":" + n;
	}
}
