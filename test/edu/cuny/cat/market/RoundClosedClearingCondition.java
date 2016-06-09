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

package edu.cuny.cat.market;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.market.clearing.MarketClearingCondition;

/**
 * A problematic clearing condition that always attempts to clear market after
 * rounds are closed. It is used to test whether the game server is stable to
 * deal with this bizaar behavior.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 * 
 */

public class RoundClosedClearingCondition extends MarketClearingCondition {

	/**
	 * clears the market when each round is closed.
	 */
	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof RoundClosedEvent) {
			triggerClearing();
		}
	}
}