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
 * The class implements a method-invocation-based infrastructure for catp.
 * </p>
 * 
 * <p>
 * With this infrstructure implementation, both the game server and the clients
 * share a single control thread, which also runs the game clock, unlike the
 * multi-threading scheme in {@link SocketBasedInfrastructureImpl} and
 * {@link QueueBasedInfrastructureImpl}. This implies that all parties run
 * inside a single process, similar to what happens in JASA. This
 * single-threading scheme overlooks the length of trading rounds, round breaks,
 * day breaks, and the like, therefore is expected to be much faster than the
 * asynchronous infrstructure implementations that have to configure with
 * sufficiently long rounds to allow message passing among the game server and
 * the clients.
 * </p>
 * 
 * <p>
 * It is worth noting that though there is a single control thread most of the
 * game duration, a game client may have a separate thread to set itself up
 * during the game initialization period.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>call_based_infrastructure</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class CallBasedInfrastructureImpl implements CatpInfrastructure {

	public static final String P_DEF_BASE = "call_based_infrastructure";

	protected Buffer<CallBasedCatpClientConnector> waitingClients;

	protected Map<Object, CallBasedCatpConnection> connections;

	protected IdAllocator idAllocator;

	static Logger logger = Logger.getLogger(CallBasedInfrastructureImpl.class);

	public CallBasedInfrastructureImpl() {
		waitingClients = BufferUtils
				.synchronizedBuffer(new UnboundedFifoBuffer<CallBasedCatpClientConnector>());
		connections = Collections
				.synchronizedMap(new HashMap<Object, CallBasedCatpConnection>());
		idAllocator = new IdAllocator();
	}

	public static CallBasedInfrastructureImpl getInstance() {
		final CatpInfrastructure infrast = Galaxy.getInstance().getDefaultTyped(
				CatpInfrastructure.class);
		if (infrast instanceof CallBasedInfrastructureImpl) {
			return (CallBasedInfrastructureImpl) infrast;
		} else {
			CallBasedInfrastructureImpl.logger
					.fatal("Unavailable CallBasedInfrastructureImpl !");
			return null;
		}
	}

	/**
	 * @return an instance of {@link CallBasedCatpClientConnector}.
	 */
	public ClientConnector<CatpMessage> createClientConnector() {
		return new CallBasedCatpClientConnector();
	}

	/**
	 * @return an instance of {@link CallBasedCatpServerConnector}.
	 */
	public ServerConnector<CatpMessage> createServerConnector() {
		return new CallBasedCatpServerConnector();
	}

	public CallBasedCatpConnection connectToServer(
			final CallBasedCatpClientConnector client) {

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

	public CallBasedCatpConnection acceptClient(
			final CallBasedCatpServerConnector server) throws ConnectionException {
		CallBasedCatpClientConnector client = null;

		synchronized (this) {
			while (waitingClients.isEmpty()) {
				try {
					wait();
				} catch (final InterruptedException e) {
					e.printStackTrace();
					CallBasedInfrastructureImpl.logger
							.error("Failed to serve any client connection request !");
					return null;
				}

				if (server.isClosed()) {
					throw new CatpServerUnavailableException(server + " closed already !");
				}
			}

			client = waitingClients.remove();
		}

		synchronized (client) {
			final long id_num = idAllocator.nextId();
			final CallBasedCatpConnection connForServer = new CallBasedCatpConnection(
					server, "callserver" + id_num);
			final CallBasedCatpConnection connForClient = new CallBasedCatpConnection(
					client, "callclient" + id_num);
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
	public void freeServerConnector(final CallBasedCatpServerConnector server) {
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
			final CallBasedCatpConnection conn = connections.remove(connector);
			if (conn.getPeer() != null) {
				conn.getPeer().setPeer(null);
			}
		} else {
			CallBasedInfrastructureImpl.logger
					.error("Try to close Unknown connection !");
		}
	}

	public boolean isSynchronous() {
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
