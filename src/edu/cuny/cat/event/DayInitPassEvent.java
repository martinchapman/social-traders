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
 * <p>
 * An event that is fired before the starting of every trading day to notify
 * {@link edu.cuny.cat.stat.GameReport}s so that they may do initialization
 * work. Similar to {@link DayStatPassEvent}, there are multiple passes of this
 * kind of event to allow some order in the initialization of reports.
 * </p>
 * 
 * @see DayStatPassEvent
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

public class DayInitPassEvent extends AuctionEvent {

	public final static int START_PASS = 0;

	public final static int FIRST_PASS = DayInitPassEvent.START_PASS;

	public final static int SECOND_PASS = 1;

	public final static int THIRD_PASS = 2;

	public final static int END_PASS = 2;

	protected int pass;

	public DayInitPassEvent(final int pass) {
		this.pass = pass;
	}

	public int getPass() {
		return pass;
	}

	public void setPass(final int pass) {
		this.pass = pass;
	}
}
