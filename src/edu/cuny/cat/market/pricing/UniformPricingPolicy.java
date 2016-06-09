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

import java.io.Serializable;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.market.MarketQuote;
import edu.cuny.cat.market.quoting.SingleSidedQuotingPolicy;

/**
 * A pricing policy in which we set the transaction price in the interval
 * between the ask quote and the bid quote as determined by the parameter k. The
 * pricing policy is uniform in the sense that individual bid and ask prices are
 * ignored, thus all agents performing transactions in the clearing operation
 * will pay the same price.
 * 
 * <p>
 * An exception is that if the transaction price determined in this way falls
 * out of the price interval of the matching ask and bid, the nearest boundary
 * of the interval will be used as the transaction price. This may happen when,
 * for example, {@link edu.cuny.cat.market.matching.LazyMaxVolumeShoutEngine} is
 * used to match shouts.
 * </p>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.10 $
 */

public class UniformPricingPolicy extends KPricingPolicy implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(UniformPricingPolicy.class);

	public UniformPricingPolicy() {
	}

	public UniformPricingPolicy(final double k) {
		super(k);
	}

	/**
	 * adjusts the price of market quote based on the given shout when the market
	 * quote does not have a valid value. This is possible when certain quoting
	 * policies, say {@link SingleSidedQuotingPolicy}, are used and the market
	 * quote is an infinite value.
	 * 
	 * @param quote
	 * @param shout
	 * @return the value of quote if it is not NaN or an infinite value, or the
	 *         price of the shout otherwise.
	 */
	protected double price(final double quote, final Shout shout) {
		if (Double.isNaN(quote) || Double.isInfinite(quote)) {
			UniformPricingPolicy.logger
					.debug("The value of a market quote of " + auctioneer.getName()
							+ " do not produce valid transaction prices !");
			UniformPricingPolicy.logger.debug("The price of " + shout.toString()
					+ " is used instead to calculate the transaction price.");

			return shout.getPrice();
		} else {
			return quote;
		}
	}

	@Override
	public double determineClearingPrice(final Shout bid, final Shout ask,
			final MarketQuote clearingQuote) {
		final double askQuote = price(clearingQuote.getAsk(), ask);
		final double bidQuote = price(clearingQuote.getBid(), bid);

		double price = kInterval(askQuote, bidQuote);

		if (price > bid.getPrice()) {
			price = bid.getPrice();
		} else if (price < ask.getPrice()) {
			price = ask.getPrice();
		}

		return price;
	}
}