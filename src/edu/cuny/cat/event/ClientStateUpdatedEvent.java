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

package edu.cuny.cat.event;

import edu.cuny.cat.core.AccountHolder;
import edu.cuny.cat.server.ClientState;

/**
 * <p>
 * An event that is fired when the status of a catp client is updated. For
 * example, the catp server lost the connection with a client.
 * </p>
 * 
 * <p>
 * Please note that this event has not yet used.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class ClientStateUpdatedEvent extends AuctionEvent {

	protected AccountHolder client;

	protected ClientState currentState;

	protected ClientState previousState;

	protected AuctionEvent triggeringEvent;

	public ClientStateUpdatedEvent(final AccountHolder client,
			final ClientState status) {
		this(client, null, status, null);
	}

	public ClientStateUpdatedEvent(final AccountHolder client,
			final ClientState currentState, final AuctionEvent triggeringEvent) {
		this(client, null, currentState, triggeringEvent);
	}

	public ClientStateUpdatedEvent(final AccountHolder client,
			final ClientState previousState, final ClientState currentState,
			final AuctionEvent triggeringEvent) {
		this.client = client;
		this.previousState = previousState;
		this.currentState = currentState;
		this.triggeringEvent = triggeringEvent;
	}

	public AccountHolder getClient() {
		return client;
	}

	public ClientState getCurrentState() {
		return currentState;
	}

	public ClientState getPreviousState() {
		return previousState;
	}

	public AuctionEvent getTriggeringEvent() {
		return triggeringEvent;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " previousState:" + previousState
				+ " currentState:" + currentState + " triggeringEvent:"
				+ triggeringEvent;
	}
}
