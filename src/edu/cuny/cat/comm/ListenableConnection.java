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
 * The class wraps the usual querying-based {@link Connection} to support
 * actively pushing {@link Message} to a listener.
 * </p>
 * 
 * <p>
 * It should be used in the following way:
 * 
 * <pre>
 *   Connection&lt;Message&gt; conn;
 * 
 *   ...
 *       
 *   ReactiveConnection&lt;Message&gt; reactiveConn = ListenableConnection.makeReactiveConnection(conn);
 *   reactiveConn.setListener(listener);
 *   reactiveConn.start();
 *   
 *   ...
 *       
 *   reactiveConn.stop();
 *   reactiveConn.setListener(null); // (optional) ...
 * </pre>
 * 
 * </p>
 * 
 * @param <M>
 *          the type of messages that can be transmitted through the connection.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class ListenableConnection<M extends Message> implements Runnable,
		ReactiveConnection<M> {
	/*
	 * about 10k for a thread's stack space
	 */
	public static final long THREAD_STACK_SIZE = 10000;

	protected ConnectionListener<M> listener;

	protected ProactiveConnection<M> connection;

	protected Thread thread;

	protected static ThreadGroup threadGroup = new ThreadGroup(
			ListenableConnection.class.getSimpleName());

	static Logger logger = Logger.getLogger(ListenableConnection.class);

	public ListenableConnection(final ProactiveConnection<M> connection) {
		this.connection = connection;
	}

	public CatpMessage getMessage() throws CatException {
		throw new CatException("This method should not be used !");
	}

	public void sendMessage(final M msg) throws CatException {
		if (connection.isClosed()) {
			throw new ConnectionException(this
					+ ": Error in sending message due to closed connection !");
		} else {
			connection.sendMessage(msg);
		}
	}

	/**
	 * loops to attempt to read messages and notifies the listener of the arrival.
	 * 
	 * @see ConnectionListener#messageArrived(Message)
	 */
	public void run() {
		M msg = null;

		while (!connection.isClosed()) {

			try {
				msg = connection.getMessage();
			} catch (final Exception e) {
				if (!connection.isClosed()) {
					e.printStackTrace();
					msg = null;
				}
			}

			if (listener != null) {
				listener.messageArrived(msg);
			} else if (msg != null) {
				ListenableConnection.logger.error("Message received in " + this
						+ " without a single listener !");
			}

			if (msg == null) {
				break;
			}
		}
	}

	public void setListener(final ConnectionListener<M> listener) {
		this.listener = listener;
	}

	public ConnectionListener<M> getListener() {
		return listener;
	}

	public void open() throws ConnectionException {
		if (listener == null) {
			throw new ConnectionException("No listener setup !");
		}

		if (thread != null) {
			throw new ConnectionException("Connection already opened !");
		} else {
			connection.open();
			thread = new Thread(ListenableConnection.threadGroup, this, "",
					ListenableConnection.THREAD_STACK_SIZE);
			thread.start();
			// logger.info("threads: " + threadGroup.activeCount());
		}
	}

	/**
	 * TODO: not safe to simply set listener null.
	 * 
	 */
	public void close() throws ConnectionException {
		listener = null;

		connection.close();

		// NOTE: stop the possibly running thread; otherwise connection may be being
		// read from and cannot be closed.
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	public boolean isClosed() {
		return (thread == null) || !thread.isAlive();
	}

	public String getLocalAddressInfo() {
		if (connection != null) {
			return connection.getLocalAddressInfo();
		} else {
			return null;
		}
	}

	public String getRemoteAddressInfo() {
		if (connection != null) {
			return connection.getRemoteAddressInfo();
		} else {
			return null;
		}
	}

	/**
	 * wraps a {@link ProactiveConnection} with {@link ListenableConnection} if
	 * necessary to make it support registering-and-listening message passing,
	 * i.e. an instance of {@link ReactiveConnection}.
	 */
	public static <T extends Message> ReactiveConnection<T> makeReactiveConnection(
			final Connection<T> conn) {
		if (conn instanceof ProactiveConnection) {
			return new ListenableConnection<T>((ProactiveConnection<T>) conn);
		} else if (conn instanceof ReactiveConnection) {
			return (ReactiveConnection<T>) conn;
		} else {
			ListenableConnection.logger.fatal("Unsupported catp connection type: "
					+ conn.getClass().getSimpleName() + " !");
			return null;
		}
	}

	/**
	 * This is needed only for debug purpose.
	 * 
	 * @return the thread that runs to repeatedly read from the proactive
	 *         {@link #connection} and call {@link #listener} to process messages.
	 */
	public Thread getThread() {
		return thread;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
