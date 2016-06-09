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

import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import edu.cuny.cat.comm.TimableCatpProactiveSession;

/**
 * An interface for creating all kinds of resources.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class SessionTimeoutTask implements TimeoutTask {

	static final Logger logger = Logger.getLogger(SessionTimeoutTask.class);

	protected ConnectionAdaptor adaptor;

	protected TimableCatpProactiveSession session;

	protected ScheduledFuture<?> future;

	public SessionTimeoutTask() {
	}

	public SessionTimeoutTask(final ConnectionAdaptor adaptor,
			final TimableCatpProactiveSession session) {
		this.adaptor = adaptor;
		this.session = session;
	}

	public void setAdaptor(final ConnectionAdaptor adaptor) {
		this.adaptor = adaptor;
	}

	public void setSession(final TimableCatpProactiveSession session) {
		this.session = session;
	}

	public void setFuture(final ScheduledFuture<?> future) {
		this.future = future;
	}

	protected void release() {
		GameController.getInstance().getTimeController().demonitor(this);
	}

	/**
	 * @return true if the scheduled task is cancelled successfully, or false
	 *         otherwise.
	 */
	public synchronized boolean cancel() {
		if (future != null) {
			try {
				future.cancel(false);
			} catch (final RuntimeException e) {
				SessionTimeoutTask.logger.error(
						"Exception occurred while canceling session timeout action !", e);
			}

			future = null;
			session = null;
			adaptor = null;

			release();

			return true;
		} else {
			return false;
		}
	}

	public void run() {
		final ConnectionAdaptor adaptor = this.adaptor;
		final TimableCatpProactiveSession session = this.session;

		final boolean succeeded = cancel();

		if (succeeded) {
			try {
				adaptor.timeout(session);
			} catch (final RuntimeException e) {
				SessionTimeoutTask.logger.error(
						"Exception occurred while processing the timeout of session "
								+ session + " !", e);
			}
		}
	}
}
