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

package edu.cuny.cat.market.accepting;

import edu.cuny.cat.core.IllegalShoutException;
import edu.cuny.cat.core.Shout;

import org.apache.log4j.Logger;

/**
 * implements the NYSE rule under which a shout must improve the market quote to
 * be placeable.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.13 $
 */

public class QuoteBeatingAcceptingPolicy extends
		OnlyNewShoutDecidingAcceptingPolicy {

	protected static final String DISCLAIMER = "This exception was generated in a lazy manner for performance reasons.  Beware misleading stacktraces.";

	/**
	 * Reusable exceptions for performance
	 */
	protected static NotAnImprovementOverQuoteException askException = null;

	protected static NotAnImprovementOverQuoteException bidException = null;

	static Logger logger = Logger.getLogger(QuoteBeatingAcceptingPolicy.class);

	/**
	 * implements the NYSE shout improvement rule.
	 */
	@Override
	public void check(final Shout shout) throws IllegalShoutException {
		double quote;
		if (shout.isBid()) {
			quote = auctioneer.bidQuote();
			logger.debug("Bid: " + shout.getPrice() + " shouldn't be less than " + quote); //
			if (shout.getPrice() < quote) {
				if (QuoteBeatingAcceptingPolicy.bidException == null) {
					// Only construct a new exception the once (for improved performance)
					QuoteBeatingAcceptingPolicy.bidException = new NotAnImprovementOverQuoteException(
							QuoteBeatingAcceptingPolicy.DISCLAIMER);
				}
				throw QuoteBeatingAcceptingPolicy.bidException;
			}
		} else {
			quote = auctioneer.askQuote();
			logger.debug("Ask: " + shout.getPrice() + " shouldn't be greater than " + quote); //
			if (shout.getPrice() > quote) {
				if (QuoteBeatingAcceptingPolicy.askException == null) {
					// Only construct a new exception the once (for improved performance)
					QuoteBeatingAcceptingPolicy.askException = new NotAnImprovementOverQuoteException(
							QuoteBeatingAcceptingPolicy.DISCLAIMER);
				}
				throw QuoteBeatingAcceptingPolicy.askException;
			}
		}
	}
}
