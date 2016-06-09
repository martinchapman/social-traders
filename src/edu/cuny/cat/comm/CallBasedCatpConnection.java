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

package edu.cuny.cat.comm;

import org.apache.log4j.Logger;

/**
 * <p>
 * The implementation of {@link Connection} when
 * {@link CallBasedInfrastructureImpl} is used.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class CallBasedCatpConnection implements ReactiveConnection<CatpMessage> {

	static Logger logger = Logger.getLogger(CallBasedCatpConnection.class);

	/**
	 * object listening to the incoming messages
	 */
	protected ConnectionListener<CatpMessage> listener;

	/**
	 * the {@link Connector} that requested to create this connection
	 */
	protected Object connector;

	/**
	 * the other side of the connection where outgoing messages go
	 */
	protected CallBasedCatpConnection peer;

	/**
	 * an identifier for this connection
	 */
	protected String id;

	public CallBasedCatpConnection(final Object connector, final String id) {
		this.connector = connector;
		this.id = id;
	}

	public Object getConnector() {
		return connector;
	}

	public void setConnector(final Object connector) {
		this.connector = connector;
	}

	public CallBasedCatpConnection getPeer() {
		return peer;
	}

	public void setPeer(final CallBasedCatpConnection peer) {
		this.peer = peer;
	}

	public void sendMessage(final CatpMessage msg) throws CatException {
		if (isClosed()) {
			throw new ConnectionException(getClass().getSimpleName()
					+ " is closed or not open yet for writing !");
		}

		peer.relayMessage(msg);
	}

	public synchronized void relayMessage(final CatpMessage msg)
			throws CatException {
		if (listener == null) {
			try {
				wait();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}

		listener.messageArrived(msg);
	}

	public synchronized void setListener(
			final ConnectionListener<CatpMessage> listener) {
		this.listener = listener;
		if (listener != null) {
			notifyAll();
		}
	}

	public ConnectionListener<CatpMessage> getListener() {
		return listener;
	}

	public void open() throws ConnectionException {
		// do nothing to open
	}

	public void close() throws ConnectionException {
		// do nothing to close
	}

	public synchronized boolean isClosed() {
		// connection is always open once created till abandoned
		return false;
	}

	public String getLocalAddressInfo() {
		return id + "_local";
	}

	public String getRemoteAddressInfo() {
		return id + "_remote";
	}
}