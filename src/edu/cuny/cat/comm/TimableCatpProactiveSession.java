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

import edu.cuny.cat.server.TimeoutTask;

/**
 * This class processes a time-sensitive request/response session initiated by
 * the current party.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public abstract class TimableCatpProactiveSession extends CatpProactiveSession {

	TimeoutTask timeoutAction;

	public TimableCatpProactiveSession(final Connection<CatpMessage> connection) {
		super(connection);
	}

	public TimableCatpProactiveSession(final Connection<CatpMessage> connection,
			final CatpRequest request) {
		super(connection, request);
	}

	@Override
	public void processResponse(final CatpResponse response) throws CatException {
		super.processResponse(response);
		cancelTimeOutAction();
	}

	public void cancelTimeOutAction() {
		if (timeoutAction == null) {
			/* temporarily allow null timeout action */
			// CatpProactiveSession.logger.fatal(new Exception(
			// "Bug found that timeout action is NULL in "
			// + getClass().getSimpleName() + " !"));
		} else {
			timeoutAction.cancel();
			timeoutAction = null;
		}
	}

	@Override
	public boolean forceOut() {
		timeoutWrapper();
		return super.forceOut();
	}

	public void setTimeoutAction(final TimeoutTask timeoutAction) {
		this.timeoutAction = timeoutAction;
	}

	public TimeoutTask getTimeoutAction() {
		return timeoutAction;
	}

	protected void timeoutWrapper() {
		cancelTimeOutAction();
		timeout();
	}

	/**
	 * called by {@link #timeoutAction} to process a timeout.
	 */
	public abstract void timeout();
}
