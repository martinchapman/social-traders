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
 * An event notifying of the subscription of a trader/specialist to the
 * information on activities at a specialist.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.10 $
 */
public class SubscriptionEvent extends AuctionEvent implements Cloneable {

	protected String subscriberId;

	protected String specialistId;

	public SubscriptionEvent(final String subscriberId, final String specialistId) {
		this.subscriberId = subscriberId;
		this.specialistId = specialistId;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		SubscriptionEvent event = null;
		event = (SubscriptionEvent) super.clone();

		return event;
	}

	public String getSubscriberId() {
		return subscriberId;
	}

	public String getSpecialistId() {
		return specialistId;
	}

	@Override
	public String toString() {
		return super.toString() + "[" + subscriberId + " to " + specialistId + "]";
	}
}
