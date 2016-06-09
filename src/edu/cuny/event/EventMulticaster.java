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

/**
 * This supports congregation of {@link EventListener}s.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.1 $
 */

public class EventMulticaster implements EventListener {
	private final EventListener a, b;

	private EventMulticaster(final EventListener a, final EventListener b) {
		this.a = a;
		this.b = b;
	}

	public static final EventListener addEventListener(final EventListener a,
			final EventListener b) {
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		return new EventMulticaster(a, b);
	}

	public static final EventListener removeEventListener(final EventListener l,
			final EventListener oldl) {
		if ((l == oldl) || (l == null)) {
			return null;
		} else if (l instanceof EventMulticaster) {
			return ((EventMulticaster) l).remove(oldl);
		} else {
			return l;
		}
	}

	private EventListener remove(final EventListener oldl) {
		if (oldl == a) {
			return b;
		}
		if (oldl == b) {
			return a;
		}
		final EventListener a2 = EventMulticaster.removeEventListener(a, oldl);
		final EventListener b2 = EventMulticaster.removeEventListener(b, oldl);
		if ((a2 == a) && (b2 == b)) {
			return this; // it's not here
		}

		return EventMulticaster.addEventListener(a2, b2);
	}

	public void eventOccurred(final Event te) {
		a.eventOccurred(te);
		b.eventOccurred(te);
	}

}
