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

/**
 * A silent console that providing no interaction feature at all. This is useful
 * for experiments that runs without human intervention.
 * 
 * @see SynchronousClockController
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public class SilentConsole implements Console {

	public void start() {
		// do nothing
	}

	public void stop() {
		// do nothing
	}

	public boolean isInteractive() {
		return false;
	}

	public void eventOccurred(final AuctionEvent e) {
		// do nothing
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
