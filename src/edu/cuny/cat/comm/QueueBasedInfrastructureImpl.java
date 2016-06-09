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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Buffer;
import org.apache.commons.collections15.BufferUtils;
import org.apache.commons.collections15.buffer.UnboundedFifoBuffer;
import org.apache.log4j.Logger;

import edu.cuny.util.Galaxy;
import edu.cuny.util.IdAllocator;

/**
 * <p>
 * The class implements a queue-based message-passing infrastructure for catp.
 * </p>
 * 
 * <p>
 * It is similar to {@link SocketBasedInfrastructureImpl} in the sense that both
 * are asynchronous. They differ in two aspects:
 * {@link QueueBasedInfrastructureImpl} does not require network resources, thus
 * for instance avoiding possible port conflicts as in
 * {@link SocketBasedInfrastructureImpl}, but
 * {@link SocketBasedInfrastructureImpl} supports the real distributed game
 * playing over the Internet and is the only available infrastructure
 * implementation for actual competitions while
 * {@link QueueBasedInfrastructureImpl} implies multiple threads with each for
 * the game server or one of the waitingClients inside a single process.
 * </p>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>queue_based_infrastructure</tt></td>
 * </tr>
 * </table>
 * 
 * @see SocketBasedInfrastructureImpl
 * @see CallBasedInfrastructureImpl
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.10 $
 */

public class QueueBasedInfrastructureImpl implements CatpInfrastructure {

	public static final String P_DEF_BASE = "queue_based_infrastructure";

	protected Buffer<QueueBasedCatpClientConnector> waitingClients;

	protected Map<Object, QueueBasedCatpConnection> connections;

	IdAllocator idAllocator;

	static Logger logger = Logger.getLogger(QueueBasedInfrastructureImpl.class);

	public QueueBasedInfrastructureImpl() {
		waitingClients = BufferUtils
				.synchronizedBuffer(new UnboundedFifoBuffer<QueueBasedCatpClientConnector>());
		connections = Collections
				.synchronizedMap(new HashMap<Object, QueueBasedCatpConnection>());
		idAllocator = new IdAllocator();
	}

	public static QueueBasedInfrastructureImpl getInstance() {
		final CatpInfrastructure infrast = Galaxy.getInstance().getDefaultTyped(
				CatpInfrastructure.class);
		if (infrast instanceof QueueBasedInfrastructureImpl) {
			return (QueueBasedInfrastructureImpl) infrast;
		} else {
			QueueBasedInfrastructureImpl.logger
					.fatal("Unavailable QueueBasedInfrastructureImpl !");
			return null;
		}
	}

	/**
	 * @return an instance of {@link QueueBasedCatpClientConnector}.
	 */
	public ClientConnector<CatpMessage> createClientConnector() {
		return new QueueBasedCatpClientConnector();
	}

	/**
	 * @return an instance of {@link QueueBasedCatpServerConnector}.
	 */
	public ServerConnector<CatpMessage> createServerConnector() {
		return new QueueBasedCatpServerConnector();
	}

	public QueueBasedCatpConnection connectToServer(
			final QueueBasedCatpClientConnector client) {

		synchronized (this) {
			waitingClients.add(client);
			notifyAll();
		}

		synchronized (client) {
			try {
				if (!connections.containsKey(client)) {
					client.wait();
				}
				return connections.get(client);
			} catch (final InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public QueueBasedCatpConnection acceptClient(
			final QueueBasedCatpServerConnector server) throws ConnectionException {
		QueueBasedCatpClientConnector client = null;

		synchronized (this) {
			while (waitingClients.isEmpty()) {
				try {
					wait();
				} catch (final InterruptedException e) {
					e.printStackTrace();
					QueueBasedInfrastructureImpl.logger
							.error("Failed to serve any client connection request !");
					return null;
				}
			}

			if (server.isClosed()) {
				throw new CatpServerUnavailableException(server + " closed already !");
			}

			client = waitingClients.remove();
		}

		synchronized (client) {
			final long id_num = idAllocator.nextId();
			final QueueBasedCatpConnection connForServer = new QueueBasedCatpConnection(
					server, "queueserver" + id_num);
			final QueueBasedCatpConnection connForClient = new QueueBasedCatpConnection(
					client, "queueclient" + id_num);
			connForServer.setPeer(connForClient);
			connForClient.setPeer(connForServer);
			connections.put(client, connForClient);

			client.notifyAll();

			return connForServer;
		}
	}

	/**
	 * frees the waiting {@link ServerConnector}.
	 * 
	 * @param server
	 *          the waiting {@link ServerConnector}.
	 */
	public void freeServerConnector(final QueueBasedCatpServerConnector server) {
		synchronized (this) {
			notifyAll();
		}
	}

	public void cleanUp() {
		waitingClients.clear();
		connections.clear();
	}

	public void closeConnection(final Object connector) {
		if (connections.containsKey(connector)) {
			final QueueBasedCatpConnection conn = connections.remove(connector);
			if (conn.getPeer() != null) {
				conn.getPeer().setPeer(null);
			}
		} else {
			QueueBasedInfrastructureImpl.logger
					.error("Try to close Unknown connection !");
		}
	}

	public boolean isSynchronous() {
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
