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
 * This class processes a request/response session initiated by the current
 * party.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.16 $
 */

public class CatpProactiveSession extends Session<CatpMessage> {

	protected CatpRequest request;

	static Logger logger = Logger.getLogger(CatpProactiveSession.class);

	/**
	 * indicates whether or not this session has completed or not.
	 * 
	 * @see #processResponse(CatpResponse)
	 */
	protected boolean completed;

	public CatpProactiveSession(final Connection<CatpMessage> connection) {
		this(connection, null);
	}

	public CatpProactiveSession(final Connection<CatpMessage> connection,
			final CatpRequest request) {
		super(connection);
		this.request = request;
		completed = false;
	}

	public void sendRequest() throws CatException {
		if (request != null) {
			sendMessage(request);
		} else {
			throw new CatpMessageErrorException("Attempted to send null request !");
		}
	}

	public void processResponse(final CatpResponse response) throws CatException {
		setCompleted(true);

		if (response == null) {
			throw new CatpMessageErrorException("Response to " + request.getType()
					+ " request expected !");
		}
	}

	@Override
	public boolean forceOut() {
		setCompleted(true);
		return super.forceOut();
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(final boolean completed) {
		this.completed = completed;
	}

	public CatpRequest getRequest() {
		return request;
	}

	public void setRequest(final CatpRequest request) {
		this.request = request;
	}
}
