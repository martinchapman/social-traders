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

import java.util.Collection;

import edu.cuny.cat.core.Trader;

/**
 * <p>
 * An event announcing the list of traders in the game.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 * 
 */

public class AvailableTradersAnnouncedEvent extends AuctionEvent {

	protected Collection<Trader> traders;

	public AvailableTradersAnnouncedEvent(final Collection<Trader> traders) {
		this.traders = traders;
	}

	/**
	 * @return a collection of {@link edu.cuny.cat.core.Trader}s.
	 */
	public Collection<Trader> getTraders() {
		return traders;
	}
}
