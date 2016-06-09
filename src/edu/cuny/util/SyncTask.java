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
/*
 * JAF - Java Application Framework
 * Copyright (C) 1999-2006 Jinzhong Niu
 */

package edu.cuny.util;

import org.apache.log4j.Logger;

import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.event.EventListener;

/**
 * <p>
 * A class enabling one task to wait until a certain number of events of
 * specified type occur. The waiting may time out and the task then be triggered
 * after a certain period. This mechanism is implemented based on
 * {@link edu.cuny.event.Event}.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.18 $
 */

public class SyncTask implements EventListener {

	static Logger logger = Logger.getLogger(SyncTask.class);

	/**
	 * the type of {@link edu.cuny.event.Event} to wait to occur.
	 */
	protected Object type;

	/**
	 * used to further distinguish between events of the same type; can be viewed
	 * as a filter so as to process only a subset of the events of the given type.
	 */
	protected Object tag;

	/**
	 * number of events to wait to occur.
	 */
	protected int count;

	/**
	 * the maximal waiting time
	 */
	protected int timeout;

	public boolean debug = false;

	/**
	 * creates a synchronization task with null tag and one event being waited.
	 * 
	 * @see #SyncTask(Object, Object, int, int)
	 */
	public SyncTask(final Object type, final int timeout) {
		this(type, null, 0, timeout);
	}

	/**
	 * @param type
	 *          the type of event to wait to occur
	 * @param tag
	 *          only those events of the type with this tag as user object is
	 *          counted
	 * @param count
	 *          the number of events to wait
	 * @param timeout
	 *          the period to wait before timing out
	 */
	public SyncTask(final Object type, final Object tag, final int count,
			final int timeout) {
		this.type = type;
		this.tag = tag;
		this.count = count;
		this.timeout = timeout;

		Galaxy.getInstance().getDefaultTyped(EventEngine.class).checkIn(type, this);
	}

	public void setType(final Object type) {
		this.type = type;
	}

	public Object getType() {
		return type;
	}

	public synchronized int getCount() {
		return count;
	}

	public synchronized void setCount(final int count) {
		this.count = count;
	}

	public synchronized void addCount(final int num) {
		count += num;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(final Object tag) {
		this.tag = tag;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	/**
	 * called by the thread that will execute the task to synchronize with events
	 * 
	 */
	public synchronized void sync() {
		if (debug) {
			SyncTask.logger.info("sync(): " + tag + " count: " + count);
		}

		if (count > 0) {
			try {
				wait(timeout);
			} catch (final InterruptedException e) {
				// e.printStackTrace();
			}
		}

		if (count > 0) {
			SyncTask.logger.warn("Timeout in synchronizing with " + type + " | "
					+ tag + " ! " + count);
		} else if (debug) {
			SyncTask.logger.info("sync(): " + tag + " done");
		}
	}

	/**
	 * give a green light to the waiting task explicitly
	 * 
	 */
	public synchronized void release() {
		notify();
	}

	protected synchronized void dec() {
		count--;

		if (debug) {
			SyncTask.logger.info("sync(): " + tag + " " + count);
		}

		if (count <= 0) {
			notify();
		}
	}

	public void eventOccurred(final Event event) {
		if (event.getUserObject() == tag) {
			dec();
		}
	}

	public void terminate() {
		release();
		Galaxy.getInstance().getDefaultTyped(EventEngine.class)
				.checkOut(type, this);
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();

		s += " type:" + type + " tag:" + tag + " count:" + count;

		return s;
	}
}
