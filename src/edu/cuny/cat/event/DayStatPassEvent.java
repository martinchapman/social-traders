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
 * An event that is fired after the end of every trading day to notify
 * {@link edu.cuny.cat.stat.GameReport}s so that they may calculate various
 * stats.
 * </p>
 * 
 * <p>
 * Since game reports may rely on each other to do their own calculation,
 * multiple passes of event notification of this kind enables reports that act
 * during later passes to make use of results produced in the early passes.
 * </p>
 * 
 * @see DayInitPassEvent
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class DayStatPassEvent extends AuctionEvent {

	public final static int START_PASS = 0;

	public final static int FIRST_PASS = DayStatPassEvent.START_PASS;

	public final static int SECOND_PASS = 1;

	public final static int THIRD_PASS = 2;

	public final static int END_PASS = 2;

	protected int pass;

	public DayStatPassEvent(final int pass) {
		this.pass = pass;
	}

	public int getPass() {
		return pass;
	}

	public void setPass(final int pass) {
		this.pass = pass;
	}
}
