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
 * An asynchronous dispatcher that runs a thread to process dispatching tasks
 * buffered in a queue.
 * 
 * TODO: to use {@link java.util.concurrent.Executors} to schedule tasks.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public class AsynchronousDispatcher extends Thread implements Dispatcher {

	protected static Logger logger = Logger
			.getLogger(AsynchronousDispatcher.class);

	private final DispatchingTaskQueue tasks;

	public AsynchronousDispatcher() {
		tasks = new DispatchingTaskQueue();
		start();
	}

	// TODO: this version is problematic. It and connection adaptors may wait for
	// each other to first release locks.

	// synchronized version:

	// public void run() {
	// setPriority(Thread.MAX_PRIORITY);
	//
	// DispatchingTask task = null;
	// while (!isInterrupted()) {
	// synchronized (this) {
	// if (tasks.hasNext()) {
	// task = tasks.next();
	// } else {
	// task = null;
	// try {
	// wait();
	// continue;
	// } catch (InterruptedException e) {
	// // e.printStackTrace();
	// continue;
	// }
	// }
	// }
	//
	// try {
	// task.run();
	// } catch (RuntimeException e) {
	// logger.error("Exception occurred while dispatching !", e);
	// }
	// }
	//
	// logger.info("Stopped to dispatch asynchronously.");
	// }
	//
	// public void addTask(DispatchingTask task) {
	// tasks.add(task);
	// }
	//
	// public synchronized void process() {
	// notify();
	// }
	//
	// public void processTask(DispatchingTask task) {
	// addTask(task);
	// process();
	// }

	// unsynchronized version:

	@Override
	public void run() {
		setPriority(Thread.MAX_PRIORITY);

		DispatchingTask task = null;
		while (!isInterrupted()) {
			if (tasks.hasNext()) {
				task = tasks.next();
				try {
					task.run();
				} catch (final RuntimeException e) {
					AsynchronousDispatcher.logger.error(
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

		AsynchronousDispatcher.logger.info(this + " stopped to dispatch events.\n");

	}

	public synchronized void addTask(final DispatchingTask task) {
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
		return tasks.getTasks();
	}

}
