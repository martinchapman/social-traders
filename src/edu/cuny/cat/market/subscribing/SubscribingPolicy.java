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

package edu.cuny.cat.market.subscribing;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.market.AuctioneerPolicy;
import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.util.Galaxy;

/**
 * A subscribing policy determines whether to subscribe for information from
 * specialists and which specialists to choose.
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>subscribing</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.16 $
 */

public abstract class SubscribingPolicy extends AuctioneerPolicy {

	public static final String P_DEF_BASE = "subscribing";

	static Logger logger = Logger.getLogger(SubscribingPolicy.class);

	/**
	 * @return a list of specialists to subscribe information from.
	 */
	protected abstract String[] getSubscribees();

	@Override
	public void eventOccurred(AuctionEvent event) {
		if (event instanceof DayOpenedEvent) {
			// notify to subscribe for info from the selected specialists
			final String[] ids = getSubscribees();
			if ((ids != null) && (ids.length != 0)) {
				final Event subEvent = new Event(this, ids);
				Galaxy.getInstance().getDefaultTyped(EventEngine.class).dispatchEvent(
						auctioneer, subEvent);
			}
		}

	}
}