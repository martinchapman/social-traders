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
 * <p>
 * This class describes the behaviors of an event source. Its instance may be
 * used to support a customized-event source.
 * </p>
 * 
 * <p>
 * But this is against the idea of using {@link EventEngine} to isolate the
 * event sources from event listeners.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.1 $
 */

public class EventSource {
	EventListener listeners = null;

	public void addEventListener(final EventListener l) {
		listeners = EventMulticaster.addEventListener(listeners, l);
	}

	public void removeEventListener(final EventListener l) {
		listeners = EventMulticaster.removeEventListener(listeners, l);
	}

	public void fireEvent(final Event event) {
		if (listeners != null) {
			listeners.eventOccurred(event);
		}
	}
}
