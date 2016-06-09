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
 * An event that is fired notifying a specialist checked in by the cat server
 * 
 * @author Kai Cai
 * @version $Revision: 1.9 $
 */

public class SpecialistCheckInEvent extends AuctionEvent implements Cloneable {

	protected Specialist specialist;

	public SpecialistCheckInEvent(final Specialist specialist) {
		this.specialist = specialist;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		SpecialistCheckInEvent event = null;
		event = (SpecialistCheckInEvent) super.clone();

		return event;
	}

	public Specialist getSpecialist() {
		return specialist;
	}
}