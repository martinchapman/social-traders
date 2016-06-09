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
 * An event that is fired every time a shout is rejected.
 * 
 * @author Kai Cai
 * @version $Revision: 1.10 $
 */

public class ShoutRejectedEvent extends AuctionEvent {
	/**
	 * The shout that led to this event.
	 */
	protected Shout shout;

	public ShoutRejectedEvent(final Shout shout) {
		this.shout = shout;
	}

	public Shout getShout() {
		return shout;
	}

	@Override
	public String toString() {
		return super.toString() + "[" + shout.getId() + "]";
	}

}
