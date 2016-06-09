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

/**
 * The interface for processing dispatching tasks.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public interface Dispatcher {

	/**
	 * adds a dispatching task to process.
	 * 
	 * @param task
	 *          task to process
	 */
	public void addTask(DispatchingTask task);

	/**
	 * 
	 * @return the array of tasks yet to process.
	 */
	public DispatchingTask[] getTasks();

	/**
	 * to process the pending tasks.
	 */
	public void process();

	/**
	 * to add the task and process all pending tasks. This does exactly what
	 * {@link #addTask(DispatchingTask)} and {@link #process()} do together.
	 * 
	 * @param task
	 */
	public void processTask(DispatchingTask task);

	/**
	 * terminates the dispatcher.
	 */
	public void terminate();
}
