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

package edu.cuny.cat.comm;

import org.apache.log4j.Logger;

/**
 * <p>
 * This class represents a communication session.
 * </p>
 * 
 * @param <M>
 *          the type of messages that can be processed in the session.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

public class Session<M extends Message> {

	protected int MAX_ATTEMPT = 1;

	private final Connection<M> connection;

	static Logger logger = Logger.getLogger(Session.class);

	public Session(final Connection<M> connection) {
		this.connection = connection;
	}

	public void sendMessage(final M msg) throws CatException {
		int i = MAX_ATTEMPT;
		while (i-- > 0) {
			connection.sendMessage(msg);
			return;
		}
	}

	/**
	 * cleans up if this session has to terminate abnormally.
	 * 
	 * @return if the early termination is successful or not.
	 */
	public boolean forceOut() {
		Session.logger.error(this + " terminated abnormally !");
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
