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

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.market.MarketQuote;

/**
 * <p>
 * A pricing policy in which we set the transaction price in the interval
 * between the matched prices as determined by the parameter k.
 * </p>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.13 $
 */

public class DiscriminatoryPricingPolicy extends KPricingPolicy {

	public DiscriminatoryPricingPolicy() {
	}

	public DiscriminatoryPricingPolicy(final double k) {
		super(k);
	}

	@Override
	public double determineClearingPrice(final Shout bid, final Shout ask,
			final MarketQuote clearingQuote) {

		return kInterval(ask.getPrice(), bid.getPrice());
	}
}