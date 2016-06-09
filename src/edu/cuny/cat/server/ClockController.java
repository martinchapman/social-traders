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

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;

/**
 * <p>
 * A clock control controls the game clock. The game controller hands over the
 * control to a clock control after a game is initialized. It is abstract
 * because it is left to child classes to determine when {@link #stop()} will is
 * called.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public abstract class ClockController implements AuctionEventListener {

	public static final String P_DEF_BASE = "clockcontroller";

	public void eventOccurred(final AuctionEvent event) {
	}

	/**
	 * starts the game clock, or asks an external source to decide when to start.
	 */
	public void start() {
		startClock();
	}

	/**
	 * stops the game clock. When this method will be called is up to the child
	 * class that extends this class.
	 */
	public void stop() {
		quitGame();
	}

	protected void startClock() {
		GameController.getInstance().getClock().start();
	}

	protected void pauseClock() {
		GameController.getInstance().getClock().pause();
	}

	protected void resumeClock() {
		GameController.getInstance().getClock().resume();
	}

	protected void quitGame() {
		GameController.getInstance().stop();
		GameController.getInstance().exit();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}