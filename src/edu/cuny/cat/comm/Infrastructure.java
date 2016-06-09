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

/**
 * <p>
 * The interface representing a communication infrastructure on which a client
 * can connect to a listening server and communicates.
 * </p>
 * 
 * <p>
 * An infrastructure may be synchronous or asynchronous. In the former case,
 * messages are sent and processed before the control returns and the whole catp
 * system, including the game server and the clients, is typically in a
 * single-threaded process, while in the latter case, messages are sent and the
 * control immediately returns and there are multiple threads with each for the
 * game server or one of the clients.
 * </p>
 * 
 * <p>
 * For example, {@link SocketBasedInfrastructureImpl} and
 * {@link QueueBasedInfrastructureImpl} are asynchronous, and
 * {@link CallBasedInfrastructureImpl} is synchronous.
 * {@link SocketBasedInfrastructureImpl} is used in a distributed actual
 * competition, and {@link CallBasedInfrastructureImpl} can speed up the
 * execution of an experiment significantly.
 * 
 * @param <M>
 *          the type of messages that can be transmitted through the
 *          infrastructure between a client and a server.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.11 $
 */

public interface Infrastructure<M extends Message> {

	/**
	 * can be invoked by a {@link edu.cuny.cat.GameClient} to create a
	 * {@link ClientConnector} so as to connect to a
	 * {@link edu.cuny.cat.GameServer}.
	 * 
	 * @return an instance of {@link ClientConnector}
	 */
	public ClientConnector<M> createClientConnector();

	/**
	 * can be invoked by a {@link edu.cuny.cat.GameServer} to create a
	 * {@link ServerConnector} so as to be able to wait for connection requests
	 * from {@link edu.cuny.cat.GameClient}s.
	 * 
	 * @return an instance of {@link ServerConnector}
	 */
	public ServerConnector<M> createServerConnector();

	/**
	 * @return true if the message passing based on this infrastructure is
	 *         synchronous; false otherwise.
	 */
	public boolean isSynchronous();

	/**
	 * cleans up after finishing using this infrastructure.
	 */
	public void cleanUp();
}
