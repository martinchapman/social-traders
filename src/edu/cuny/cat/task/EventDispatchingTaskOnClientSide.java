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

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;

/**
 * <p>
 * A task of dispatching auction events to listeners on the client side. It
 * dispatches events to a given list of listeners in order.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.1 $
 */

public class EventDispatchingTaskOnClientSide extends EventDispatchingTask {

	static Logger logger = Logger
			.getLogger(EventDispatchingTaskOnClientSide.class);

	protected LinkedList<AuctionEventListener> listeners;

	public EventDispatchingTaskOnClientSide(
			final LinkedList<AuctionEventListener> listeners, final AuctionEvent event) {
		this.listeners = listeners;
		this.event = event;
	}

	public void run() {

		if (listeners != null) {
			final Iterator<AuctionEventListener> i = listeners.iterator();
			while (i.hasNext()) {
				final AuctionEventListener listener = i.next();
				try {
					listener.eventOccurred(event);
				} catch (final RuntimeException e) {
					EventDispatchingTaskOnClientSide.logger.error(
							"Exception occurred in dispatching " + event + " to " + listener
									+ " !", e);
					failedOn(listener);
				}
			}
		}

		/* remove the references to observers */
		deleteObservers();
	}
}
