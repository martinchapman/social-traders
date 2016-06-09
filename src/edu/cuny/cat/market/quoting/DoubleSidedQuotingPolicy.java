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

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.market.accepting.QuoteBeatingAcceptingPolicy;
import edu.cuny.cat.market.matching.ShoutEngine;

/**
 * A quoting policy that sets the market quotes based on the boundary matched
 * and unmatched ask and bid in the auction's shout engine. It is named so
 * because it uses both the competitive side and the opposite side to determine
 * a market quote.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class DoubleSidedQuotingPolicy extends QuotingPolicy {

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
		return Shout.minPrice(shoutEngine.getLowestUnmatchedAsk(), shoutEngine
				.getLowestMatchedBid());
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
		return Shout.maxPrice(shoutEngine.getHighestMatchedAsk(), shoutEngine
				.getHighestUnmatchedBid());
	}
}
