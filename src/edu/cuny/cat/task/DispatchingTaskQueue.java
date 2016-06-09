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

import org.apache.commons.collections15.Buffer;
import org.apache.commons.collections15.BufferUtils;
import org.apache.commons.collections15.buffer.UnboundedFifoBuffer;
import org.apache.log4j.Logger;

/**
 * <p>
 * Defines a queue for buffering event dispatching tasks.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public class DispatchingTaskQueue extends Observable {

	static Logger logger = Logger.getLogger(DispatchingTaskQueue.class);

	protected Buffer<DispatchingTask> tasks;

	public DispatchingTaskQueue() {
		tasks = BufferUtils
				.synchronizedBuffer(new UnboundedFifoBuffer<DispatchingTask>());
	}

	public void add(final DispatchingTask task) {
		tasks.add(task);
	}

	public boolean hasNext() {
		return !tasks.isEmpty();
	}

	public DispatchingTask next() {
		return tasks.remove();
	}

	public void clear() {
		tasks.clear();
	}

	public DispatchingTask[] getTasks() {
		final DispatchingTask template[] = new DispatchingTask[0];
		return tasks.toArray(template);
	}
}
