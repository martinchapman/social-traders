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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.cuny.cat.comm.CatpInfrastructure;
import edu.cuny.cat.comm.TimableCatpProactiveSession;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.ResourcePool;

/**
 * <p>
 * monitors time-sensitive actions and invokes timeout handlers if necessary.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.sessiontimeout</tt><br>
 * <font size=-1>int >=1 (60000 by default)</font></td>
 * <td valign=top>(the number of milli-seconds the server waits for a catp
 * session to terminate)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>timemout</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class TimeoutController implements Parameterizable {

	public static final String P_SESSIONTIMEOUT = "sessiontimeout";

	public static final String P_DEF_BASE = "timeout";

	public final long DEFAULT_SESSIONTIMEOUT = 60000;

	protected long sessionTimeout;

	protected ScheduledExecutorService executor;

	protected ResourcePool<SessionTimeoutTask> taskPool;

	protected CatpInfrastructure infrast;

	static final Logger logger = Logger.getLogger(TimeoutController.class);

	public TimeoutController() {
		infrast = Galaxy.getInstance().getDefaultTyped(CatpInfrastructure.class);
		executor = Executors.newScheduledThreadPool(5);
		taskPool = new ResourcePool<SessionTimeoutTask>(
				new SessionTimeoutTaskFactory());
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		sessionTimeout = parameters.getLongWithDefault(base
				.push(TimeoutController.P_SESSIONTIMEOUT), new Parameter(
				TimeoutController.P_DEF_BASE).push(TimeoutController.P_SESSIONTIMEOUT),
				DEFAULT_SESSIONTIMEOUT);
		if (sessionTimeout < 5) {
			TimeoutController.logger.warn("Session timeout value is too low !");
		}
	}

	public long getSessionTimeout() {
		return sessionTimeout;
	}

	/**
	 * schedules a {@link SessionTimeoutTask} to monitor the duration of the
	 * session via a catp connection with the default timeout.
	 * 
	 * @param adaptor
	 * @param session
	 */
	public TimeoutTask monitor(final ConnectionAdaptor adaptor,
			final TimableCatpProactiveSession session) {
		return monitor(adaptor, session, sessionTimeout);
	}

	/**
	 * schedules a {@link SessionTimeoutTask} to monitor the duration of the
	 * session via a catp connection with a specified timeout.
	 * 
	 * @param adaptor
	 * @param session
	 * @param sessionTimeout
	 */
	public synchronized TimeoutTask monitor(final ConnectionAdaptor adaptor,
			final TimableCatpProactiveSession session, final long sessionTimeout) {
		if (infrast.isSynchronous()) {
			return null;
		} else {
			final SessionTimeoutTask task = taskPool.get();
			task.setAdaptor(adaptor);
			task.setSession(session);
			final ScheduledFuture<?> future = executor.schedule(task, sessionTimeout,
					TimeUnit.MILLISECONDS);
			task.setFuture(future);

			return task;

		}
	}

	public void demonitor(final SessionTimeoutTask task) {
		taskPool.put(task);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " "
				+ TimeoutController.P_SESSIONTIMEOUT + ":" + sessionTimeout;
	}
}
