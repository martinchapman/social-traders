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

package edu.cuny.cat.server;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.SimulationOverEvent;

/**
 * <p>
 * A clock controller that starts the game clock immediately when the game
 * controller hands over the control and quits the simulation immediately after
 * the game completes.
 * </p>
 * <p>
 * This is useful when experiments need to run without human intervention and
 * with the server and all clients within a single local process.
 * </p>
 * 
 * @see SilentConsole
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class SynchronousClockController extends ClockController {

	static Logger logger = Logger.getLogger(SynchronousClockController.class);

	@Override
	protected void startClock() {
		// runs the game in a synchronous way, without starting a thread
		GameController.getInstance().getClock().run();
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof SimulationOverEvent) {
			stop();
		}
	}
}
