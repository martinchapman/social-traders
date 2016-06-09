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

package edu.cuny.cat.task;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;

/**
 * <p>
 * Defines the interface for the task of dispatching some auction event to
 * specified receivers. It is observable and its observers are notified if the
 * task fails to accomplish.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public abstract class EventDispatchingTask extends DispatchingTask {

	static Logger logger = Logger.getLogger(EventDispatchingTask.class);

	protected AuctionEvent event;

	public AuctionEvent getEvent() {
		return event;
	}

	public void setEvent(final AuctionEvent event) {
		this.event = event;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += " " + event.getClass().getSimpleName();
		return s;
	}
}
