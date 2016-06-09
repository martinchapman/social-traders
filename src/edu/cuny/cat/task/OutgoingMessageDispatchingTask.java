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

import edu.cuny.cat.comm.CatException;
import edu.cuny.cat.comm.CatpMessage;
import edu.cuny.cat.comm.Session;
import edu.cuny.util.Utils;

/**
 * <p>
 * A task of dispatching a CATP message to a client on the server side.
 * </p>
 * 
 * <p>
 * NOTE: this class is currently not used and may be removed in the future.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.1 $
 */

public class OutgoingMessageDispatchingTask extends MessageDispatchingTask {

	static Logger logger = Logger.getLogger(OutgoingMessageDispatchingTask.class);

	protected Session<CatpMessage> session;

	public OutgoingMessageDispatchingTask(final CatpMessage msg,
			final Session<CatpMessage> session, final String clientId) {
		super(msg, clientId);
		this.session = session;
	}

	public void run() {
		try {
			session.sendMessage(message);
		} catch (final CatException e) {
			OutgoingMessageDispatchingTask.logger.error(
					"Exception occurred in dispatching " + message + " to client "
							+ clientId + " !", e);
			failedOn(clientId);
		} catch (final RuntimeException e) {
			OutgoingMessageDispatchingTask.logger.error(
					"Exception occurred in dispatching " + message + " to client "
							+ clientId + " !", e);
			failedOn(clientId);
		}

		/* remove the references to observers */
		deleteObservers();
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName() + " ("
				+ session.getClass().getSimpleName() + " for " + clientId + ")";
		s += "\n" + Utils.indent(message.toString());
		return s;
	}
}
