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
 * An event announcing the price list of the specified specialist in the game.
 * </p>
 * 
 * @author Kai Cai
 * @version $Revision: 1.14 $
 * 
 */

public class FeesAnnouncedEvent extends AuctionEvent implements Cloneable {

	protected Specialist specialist;

	public FeesAnnouncedEvent(final Specialist specialist) {
		this.specialist = specialist;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		FeesAnnouncedEvent event = null;
		event = (FeesAnnouncedEvent) super.clone();

		if (event.specialist != null) {
			event.specialist = (Specialist) event.specialist.clone();
		}

		return event;
	}

	public double[] getFees() {
		return specialist.getFees();
	}

	public Specialist getSpecialist() {
		return specialist;
	}

	public double getRegistrationFee() {
		return specialist.getRegistrationFee();
	}

	public double getInformationFee() {
		return specialist.getInformationFee();
	}

	public double getShoutFee() {
		return specialist.getShoutFee();
	}

	public double getTransactionFee() {
		return specialist.getTransactionFee();
	}

	public double getProfitFee() {
		return specialist.getProfitFee();
	}

	@Override
	public String toString() {
		return super.toString() + "[" + specialist.getId() + "]";
	}
}
