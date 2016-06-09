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

/**
 * An event notifying the registration of a trader with a specialist.
 * 
 * @author Kai Cai
 * @version $Revision: 1.9 $
 */
public class RegistrationEvent extends AuctionEvent implements Cloneable {

	protected String traderId;

	protected String specialistId;

	public RegistrationEvent(final String traderId, final String specialistId) {
		this.traderId = traderId;
		this.specialistId = specialistId;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		RegistrationEvent event = null;
		event = (RegistrationEvent) super.clone();

		return event;
	}

	public String getTraderId() {
		return traderId;
	}

	public String getSpecialistId() {
		return specialistId;
	}

	@Override
	public String toString() {
		return super.toString() + "[" + traderId + " to " + specialistId + "]";
	}
}
