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

package edu.cuny.cat.event;

import edu.cuny.cat.core.Shout;

/**
 * An event that is fired to notify a subscriber that a shout is placed in an
 * auction. It differs from {@link ShoutPlacedEvent} in the sense that the
 * latter is used to notify a trader or a market that a shout by the trader is
 * placed at the market.
 * 
 * @see ShoutPlacedEvent
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public class ShoutPostedEvent extends AuctionEvent implements Cloneable {

	/**
	 * The shout that led to this event.
	 */
	protected Shout shout;

	public ShoutPostedEvent(final Shout shout) {
		this.shout = shout;
	}

	public Shout getShout() {
		return shout;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ShoutPostedEvent event = null;
		event = (ShoutPostedEvent) super.clone();

		if (event.shout != null) {
			event.shout = (Shout) event.shout.clone();
		}

		return event;
	}

	@Override
	public String toString() {
		return super.toString() + "[" + shout.getId() + "]";
	}
}
