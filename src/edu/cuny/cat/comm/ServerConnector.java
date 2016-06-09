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
 * Defines how a server listens and establishes {@link Connection}s from
 * clients.
 * </p>
 * 
 * @param <M>
 *          the type of messages that can be transmitted through the connection.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public interface ServerConnector<M extends Message> extends Connector {

	/**
	 * waits until a {@link ClientConnector} connects in and establish a
	 * {@link Connection}.
	 * 
	 * @return the {@link Connection} established with the client.
	 * @throws ConnectionException
	 */
	public Connection<M> accept() throws ConnectionException;

	/**
	 * closes this connector.
	 * 
	 * @throws ConnectionException
	 */
	public void close() throws ConnectionException;

}
