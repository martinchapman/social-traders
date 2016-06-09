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

import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;

/**
 * <p>
 * A task of dispatching auction events to listeners on the server side. It uses
 * a mapping from {@link java.lang.String} to {@link AuctionEventListener} and
 * can dispatch events to a specifid subset of the mapped listeners, which is
 * useful on the server side to dispatch events to only certain clients.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.1 $
 */

public class EventDispatchingTaskOnServerSide extends EventDispatchingTask {

	static Logger logger = Logger
			.getLogger(EventDispatchingTaskOnServerSide.class);

	protected String[] receiverIds;

	protected Map<String, ? extends AuctionEventListener> adaptors;

	public EventDispatchingTaskOnServerSide(
			final Map<String, ? extends AuctionEventListener> adaptors,
			final AuctionEvent event, final String[] receiverIds) {
		this.adaptors = adaptors;
		this.event = event;
		this.receiverIds = receiverIds;
	}

	public void run() {
		AuctionEventListener listener;
		for (final Object receiverId : receiverIds) {
			listener = adaptors.get(receiverId);
			if (listener == null) {
				EventDispatchingTaskOnServerSide.logger.fatal("Dispatching " + event
						+ " to non-existing listener " + receiverId + " !\n");
				failedOn(receiverId);
			} else {
				try {
					listener.eventOccurred(event);
				} catch (final RuntimeException e) {
					EventDispatchingTaskOnServerSide.logger.error(
							"Exception occurred in dispatching " + event + " to listener "
									+ receiverId + " !", e);
					failedOn(receiverId);
				}
			}
		}

		/* remove the references to observers */
		deleteObservers();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " (" + event + ")";
	}
}
