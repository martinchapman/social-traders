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

/**
 * A synchronous event dispatcher without any internal thread.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

public class SynchronousDispatcher implements Dispatcher {

	static Logger logger = Logger.getLogger(SynchronousDispatcher.class);

	protected final DispatchingTaskQueue tasks;

	protected boolean running;

	public SynchronousDispatcher() {
		tasks = new DispatchingTaskQueue();
		running = false;
	}

	/**
	 * 
	 */
	public void addTask(final DispatchingTask task) {
		tasks.add(task);
	}

	public void process() {
		if (running) {
			// previously this thread was processing a task, so quit and let the
			// earlier invocation to continue!
			return;
		} else {
			running = true;
		}

		DispatchingTask task = null;
		while (tasks.hasNext()) {
			task = tasks.next();
			try {
				task.run();
			} catch (final RuntimeException e) {
				SynchronousDispatcher.logger.error(
						"Exception occurred while dispatching !", e);
			}
		}

		running = false;
	}

	public void processTask(final DispatchingTask task) {
		addTask(task);
		process();
	}

	public void terminate() {
		tasks.clear();
	}

	public DispatchingTask[] getTasks() {
		return tasks.getTasks();
	}
}