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

import edu.cuny.cat.core.Specialist;

/**
 * <p>
 * Informs the number of traders having been registered with a specialist on a
 * day.
 * </p>
 * 
 * <p>
 * It is only used on the catp client side.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 * 
 */

public class RegisteredTradersAnnouncedEvent extends AuctionEvent {

	protected Specialist specialist;

	protected int numOfTraders;

	public RegisteredTradersAnnouncedEvent(final Specialist specialist,
			final int numOfTraders) {
		this.specialist = specialist;
		this.numOfTraders = numOfTraders;
	}

	public Specialist getSpecialist() {
		return specialist;
	}

	public int getNumOfTraders() {
		return numOfTraders;
	}
}
