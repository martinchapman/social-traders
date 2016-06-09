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

import edu.cuny.cat.market.AuctioneerPolicy;
import edu.cuny.cat.market.matching.ShoutEngine;

/**
 * The interface defined for the method of quote generation.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public abstract class QuotingPolicy extends AuctioneerPolicy {

	/**
	 * gets the ask quote
	 * 
	 * @param shoutEngine
	 *          the shout engine processing shouts
	 * @return ask quote
	 */
	public abstract double askQuote(ShoutEngine shoutEngine);

	/**
	 * gets the bid quote
	 * 
	 * @param shoutEngine
	 *          the shout engine processing shouts
	 * @return bid quote
	 */
	public abstract double bidQuote(ShoutEngine shoutEngine);

	/**
	 * 
	 * @param shoutEngine
	 *          the shout engine processing shouts
	 * @return the mid point between the ask quote and the bid quote.
	 */
	public double midQuote(ShoutEngine shoutEngine) {
		return (askQuote(shoutEngine) + bidQuote(shoutEngine)) / 2;
	}
}
