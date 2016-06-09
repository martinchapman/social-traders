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
 * An event that is fired every time a shout is placed in an auction, in
 * contrast to {@link ShoutReceivedEvent}, which represents a shout is received
 * by the cat server but may not be accepted by the corresponding market. It
 * also differs from {@link ShoutPostedEvent} in the sense that the latter is
 * the way of notifying subscribers of placed shouts while a
 * {@link ShoutPlacedEvent} tells the trader that placed the shout and the
 * market where the shout is placed.
 * 
 * @see ShoutPostedEvent
 * @see ShoutReceivedEvent
 * 
 * @author Steve Phelps
 * @version $Revision: 1.11 $
 */

public class ShoutPlacedEvent extends AuctionEvent implements Cloneable {

	/**
	 * The shout that led to this event.
	 */
	protected Shout shout;

	public ShoutPlacedEvent(final Shout shout) {
		this.shout = shout;
	}

	public Shout getShout() {
		return shout;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ShoutPlacedEvent event = null;
		event = (ShoutPlacedEvent) super.clone();

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
