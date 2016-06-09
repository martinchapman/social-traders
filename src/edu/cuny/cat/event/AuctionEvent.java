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
/*
 * JASA Java Auction Simulator API
 * Copyright (C) 2001-2005 Steve Phelps
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
 * <p>
 * Superclass for all types of auction event.
 * </p>
 * 
 * <p>
 * Each auction event carries a time stamp, including current day, current
 * round, and how many ticks into the round.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.11 $
 */

public abstract class AuctionEvent {

	private static int DAY = 0;

	private static int ROUND = 1;

	private static int TICK = 2;

	protected int time[];

	public void setTime(final int time[]) {
		this.time = time;
	}

	public int[] getTime() {
		return time;
	}

	public int getDay() {
		if (time != null) {
			return time[AuctionEvent.DAY];
		} else {
			return -1;
		}
	}

	public int getRound() {
		if (time != null) {
			return time[AuctionEvent.ROUND];
		} else {
			return -1;
		}
	}

	public int getTick() {
		if (time != null) {
			return time[AuctionEvent.TICK];
		} else {
			return -1;
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
