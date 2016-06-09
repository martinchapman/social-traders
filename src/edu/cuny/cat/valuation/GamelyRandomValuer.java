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

package edu.cuny.cat.valuation;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.GameOverEvent;

/**
 * A valuation policy in which we are allocated a new random valuation at the
 * end of each game (iteration).
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class GamelyRandomValuer extends RandomValuer {

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);
		if (event instanceof GameOverEvent) {
			drawRandomValue();
		}
	}
}
