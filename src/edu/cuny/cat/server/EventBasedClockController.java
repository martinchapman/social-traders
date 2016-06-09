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

import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.event.EventListener;
import edu.cuny.util.Galaxy;

/**
 * <p>
 * A clock controller that controls the clock based upon
 * {@link edu.cuny.event.Event}s sent from an external source. It enables a
 * human user to control the simulation through a graphical or command line
 * interface.
 * </p>
 * 
 * @see edu.cuny.cat.ui.GuiConsole
 * @see edu.cuny.cat.server.TelnetConsole
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class EventBasedClockController extends ClockController implements
		EventListener {

	static Logger logger = Logger.getLogger(EventBasedClockController.class);

	public static String START = "start";

	public static String PAUSE = "pause";

	public static String RESUME = "resume";

	public static String QUIT = "quit";

	public EventBasedClockController() {
	}

	@Override
	public void start() {
		// do nothing except for listening for event.
		Galaxy.getInstance().getDefaultTyped(EventEngine.class).checkIn(
				GameClock.class, this);
	}

	@Override
	public void stop() {
		// stop listening for event
		Galaxy.getInstance().getDefaultTyped(EventEngine.class).checkOut(
				GameClock.class, this);

		quitGame();
	}

	public void eventOccurred(final Event te) {
		if (EventBasedClockController.START.equalsIgnoreCase((String) te
				.getUserObject())) {
			startClock();
		} else if (EventBasedClockController.PAUSE.equalsIgnoreCase((String) te
				.getUserObject())) {
			pauseClock();
		} else if (EventBasedClockController.RESUME.equalsIgnoreCase((String) te
				.getUserObject())) {
			resumeClock();
		} else if (EventBasedClockController.QUIT.equalsIgnoreCase((String) te
				.getUserObject())) {
			stop();
		}
	}
}
