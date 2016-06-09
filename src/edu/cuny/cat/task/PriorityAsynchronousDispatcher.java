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
 * An asynchronous dispatcher that runs a thread to process tasks. It allows to
 * use a task comparator and the task IDs to support a stable priority queue of
 * tasks.
 * 
 * @see PrioritySynchronousDispatcher
 * 
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public class PriorityAsynchronousDispatcher extends Thread implements
		Dispatcher {

	protected static Logger logger = Logger
			.getLogger(PriorityAsynchronousDispatcher.class);

	protected final PriorityQueue<DispatchingTask> tasks;

	protected int counter;

	public PriorityAsynchronousDispatcher(Comparator<DispatchingTask> comparator) {
		tasks = new PriorityQueue<DispatchingTask>(20, comparator);
		counter = 0;
		start();
	}

	@Override
	public void run() {
		setPriority(Thread.MAX_PRIORITY);

		DispatchingTask task = null;
		while (!isInterrupted()) {
			if (!tasks.isEmpty()) {
				synchronized (this) {
					task = tasks.remove();
				}
				try {
					task.run();
				} catch (final RuntimeException e) {
					PriorityAsynchronousDispatcher.logger.error(
							"Exception occurred while dispatching !", e);
				}
			} else {
				try {
					Thread.sleep(100);
				} catch (final InterruptedException e) {
					// e.printStackTrace();
				}
			}
		}

		/* TODO: there may be tasks left in the queue */
		tasks.clear();

		PriorityAsynchronousDispatcher.logger.info(this
				+ " stopped to dispatch events.\n");

	}

	public synchronized void addTask(final DispatchingTask task) {
		task.tid = counter++;
		tasks.add(task);
	}

	public void process() {
		// do nothing
	}

	public void processTask(final DispatchingTask task) {
		addTask(task);
		process();
	}

	public void terminate() {
		interrupt();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public DispatchingTask[] getTasks() {
		final DispatchingTask template[] = new DispatchingTask[0];
		return tasks.toArray(template);
	}
}
