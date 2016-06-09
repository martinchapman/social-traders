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

package edu.cuny.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * <p>
 * Provides a mechnism for synchronized event dispatching of {@link Event}s.
 * </p>
 * 
 * <p>
 * The class is synchronized, so be careful in multithreading scenario to avoid
 * deadlock.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.14 $
 */

public class EventEngine {

	protected static Logger logger = Logger.getLogger(EventEngine.class);

	protected boolean enabled;

	protected Map<Object, EventListener> dispatchTable;

	public EventEngine() {
		dispatchTable = new HashMap<Object, EventListener>();
	}

	public synchronized void start() {
		setEnabled(true);
	}

	public synchronized void stop() {
		setEnabled(false);
	}

	public synchronized void checkIn(final Object type, final EventListener l) {
		if (!isEnabled()) {
			return;
		}

		addListener(type, l);
	}

	public synchronized void checkOut(final Object type, final EventListener l) {
		if (!isEnabled()) {
			return;
		}

		removeListener(type, l);
	}

	public synchronized void dispatchEvent(final Object type, final Event te) {
		if (!isEnabled()) {
			return;
		}

		synchronizedDispatch(type, te);
	}

	protected final void addListener(final Object type, final EventListener l) {
		setListenerColl(type, EventMulticaster.addEventListener(
				getListenerColl(type), l));
	}

	protected final void removeListener(final Object type, final EventListener l) {
		setListenerColl(type, EventMulticaster.removeEventListener(
				getListenerColl(type), l));
	}

	private EventListener getListenerColl(final Object type) {
		return dispatchTable.get(type);
	}

	private EventListener setListenerColl(final Object type,
			final EventListener lc) {
		if (lc == null) {
			dispatchTable.remove(type);
			return null;
		}

		return dispatchTable.put(type, lc);
	}

	public void synchronizedDispatch(final Object type, final Event te) {
		final EventListener lc = getListenerColl(type);
		if (lc != null) {
			lc.eventOccurred(te);
		}
	}

	// /**
	// * PENDING: asynchronized event dispatching
	// */
	// public final void AsynchronizedDispatch(Event te) {
	// if (!isEnabled())
	// return;
	//
	// // to send in an asynchronized way
	// }

	protected void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	protected boolean isEnabled() {
		return enabled;
	}
}
