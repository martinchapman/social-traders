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

import java.util.Comparator;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

/**
 * A synchronous task dispatcher without any internal thread. It allows to use a
 * task comparator and the task IDs to support a stable priority queue of tasks.
 * 
 * @see PriorityAsynchronousDispatcher
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

public class PrioritySynchronousDispatcher implements Dispatcher {

	static Logger logger = Logger.getLogger(PrioritySynchronousDispatcher.class);

	protected final PriorityQueue<DispatchingTask> tasks;

	protected boolean running;

	protected int counter;

	public PrioritySynchronousDispatcher(Comparator<DispatchingTask> comparator) {
		tasks = new PriorityQueue<DispatchingTask>(20, comparator);
		running = false;
		counter = 0;
	}

	/**
	 * 
	 */
	public void addTask(final DispatchingTask task) {
		task.tid = counter++;
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
		while (!tasks.isEmpty()) {
			task = tasks.remove();
			try {
				task.run();
			} catch (final RuntimeException e) {
				PrioritySynchronousDispatcher.logger.error(
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
		final DispatchingTask template[] = new DispatchingTask[0];
		return tasks.toArray(template);
	}
}