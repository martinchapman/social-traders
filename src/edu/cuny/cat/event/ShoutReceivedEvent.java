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

package edu.cuny.cat.event;

import edu.cuny.cat.core.Shout;

/**
 * An event that is fired every time when a shout is received in a market (may
 * not be allowed to place eventually), in contrast to ShoutPlacedEvent which
 * represents a shout that has been received and placed.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.12 $
 */

public class ShoutReceivedEvent extends AuctionEvent implements Cloneable {

	/**
	 * The shout that led to this event.
	 */
	protected Shout shout;

	public ShoutReceivedEvent(final Shout shout) {
		this.shout = shout;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ShoutReceivedEvent event = null;
		event = (ShoutReceivedEvent) super.clone();

		if (event.shout != null) {
			event.shout = (Shout) event.shout.clone();
		}

		return event;
	}

	public Shout getShout() {
		return shout;
	}

	@Override
	public String toString() {
		return super.toString() + "[" + shout.getId() + "]";
	}
}
