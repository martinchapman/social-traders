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

import edu.cuny.cat.comm.CatpMessage;
import edu.cuny.cat.comm.MessageHandler;

/**
 * <p>
 * A task of dispatching a CATP message from a client to its adaptor.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.1 $
 */

public class IncomingMessageDispatchingTask extends MessageDispatchingTask {

	static Logger logger = Logger.getLogger(IncomingMessageDispatchingTask.class);

	protected MessageHandler<CatpMessage> handler;

	public IncomingMessageDispatchingTask(final CatpMessage msg,
			final MessageHandler<CatpMessage> handler, final String clientId) {
		super(msg, clientId);
		this.handler = handler;
	}

	public void run() {
		try {
			handler.handleMessage(message);
		} catch (final RuntimeException e) {
			IncomingMessageDispatchingTask.logger.error(
					"Exception occurred in handling incoming message " + message
							+ " to client " + clientId + " !", e);
			failedOn(clientId);
		}

		/* remove the references to observers */
		deleteObservers();
	}
}
