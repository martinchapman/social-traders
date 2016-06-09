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

import edu.cuny.cat.core.Specialist;

/**
 * <p>
 * An event announcing the list of specialists in the game.
 * </p>
 * 
 * @author Kai Cai
 * @version $Revision: 1.9 $
 * 
 */

public class AvailableMarketsAnnouncedEvent extends AuctionEvent {

	protected Collection<Specialist> markets;

	public AvailableMarketsAnnouncedEvent(final Collection<Specialist> markets) {
		super();
		this.markets = markets;
	}

	/**
	 * @return a collection of {@link edu.cuny.cat.core.Specialist}s.
	 */
	public Collection<Specialist> getMarkets() {
		return markets;
	}
}
