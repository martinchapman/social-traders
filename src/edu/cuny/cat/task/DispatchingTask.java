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

import java.util.Observable;

import org.apache.log4j.Logger;

/**
 * <p>
 * Defines the interface for the task of dispatching events or messages to
 * specified receivers. It is observable and its observers are notified if the
 * task fails to accomplish.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public abstract class DispatchingTask extends Observable implements Runnable {

	static Logger logger = Logger.getLogger(DispatchingTask.class);

	/**
	 * an integer uniquely identifying this task and also providing a way to sort
	 * tasks as the order they are created
	 */
	public int tid;

	/**
	 * notifies the observers of the failure of dispatching to the specified
	 * receiver.
	 * 
	 * @param receiver
	 *          the receiver involved in the failure
	 */
	public void failedOn(final Object receiver) {
		setChanged();
		notifyObservers(receiver);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " tid:" + tid;
	}
}
