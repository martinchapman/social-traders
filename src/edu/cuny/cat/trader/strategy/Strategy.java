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

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.cat.trader.AbstractTradingAgent;
import edu.cuny.util.Prototypeable;

/**
 * <p>
 * Classes implementing this interface define trading strategies for round-robin
 * traders.
 * </p>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.11 $
 */

public interface Strategy extends Prototypeable, AuctionEventListener {

	/**
	 * Modify the trader's current shout according to the trading strategy being
	 * implemented.
	 * 
	 * @param shout
	 *          The shout to be updated
	 * @return The new shout, or null if no shout is to be placed.
	 */
	public Shout modifyShout(Shout shout);

	public void setAgent(AbstractTradingAgent agent);

	public int determineQuantity();

	public boolean requiresAuctionHistory();

}