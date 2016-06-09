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

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.SimulationOverEvent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * A class controls the game clock by automatically starting it and quitting the
 * game both after a specified amount of time.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.startdelay</tt><br>
 * <font size=-1>long</font></td>
 * <td valign=top>(the number of milliseconds to wait before starting the game
 * clock.)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.exitdelay</tt><br>
 * <font size=-1>long</font></td>
 * <td valign=top>(the number of milliseconds to wait before quiting the game
 * after the clock stops.)</td>
 * </tr>
 * 
 * </table>
 * 
 * @see SilentConsole
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public class AutoClockController extends ClockController implements
		Parameterizable {

	public static String P_STARTDELAY = "startdelay";

	public static String P_EXITDELAY = "exitdelay";

	protected Timer timer;

	protected int startDelay;

	protected int exitDelay;

	static Logger logger = Logger.getLogger(AutoClockController.class);

	public AutoClockController() {
		timer = new Timer();
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		startDelay = parameters.getIntWithDefault(base
				.push(AutoClockController.P_STARTDELAY), null, 10000);
		exitDelay = parameters.getIntWithDefault(base
				.push(AutoClockController.P_EXITDELAY), null, 10000);
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof SimulationOverEvent) {
			stop();
		}
	}

	@Override
	public void start() {
		timer.schedule(new AutoStartTimerTask(), startDelay);

		int secs = startDelay / 1000;
		final int mins = secs / 60;
		secs = secs % 60;
		AutoClockController.logger.info("\n");
		AutoClockController.logger.info("Game will start automatically after "
				+ mins + " min(s) " + secs + " sec(s) ...");
	}

	@Override
	public void stop() {
		timer.schedule(new AutoExitTimerTask(), exitDelay);
	}

	public String getString() {
		String s = getClass().getSimpleName();
		s += "( " + AutoClockController.P_STARTDELAY + ":" + startDelay;
		s += "; " + AutoClockController.P_EXITDELAY + ":" + exitDelay;
		s += ")";

		return s;
	}

	class AutoStartTimerTask extends TimerTask {
		@Override
		public void run() {
			startClock();
		}
	}

	class AutoExitTimerTask extends TimerTask {
		@Override
		public void run() {
			quitGame();
		}
	}
}
