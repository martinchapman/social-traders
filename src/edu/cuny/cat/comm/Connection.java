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
 * A {@link Message} I/O interface for a text message client or server to
 * communicate with its counterpart.
 * </p>
 * 
 * @param <M>
 *          the type of messages that can be transmitted through the connection.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 * 
 */
public interface Connection<M extends Message> {

	/**
	 * sends a {@link Message} through this connection.
	 * 
	 * @param msg
	 *          message to be sent.
	 * @throws Exception
	 */
	public abstract void sendMessage(M msg) throws CatException;

	/**
	 * opens the connection before sending or receiving any message.
	 * 
	 * @throws ConnectionException
	 */
	public abstract void open() throws ConnectionException;

	/**
	 * closes this connection.
	 * 
	 * @throws ConnectionException
	 */
	public abstract void close() throws ConnectionException;

	/**
	 * checks whether the connection is closed or not.
	 * 
	 * @return true if closed; false otherwise.
	 */
	public abstract boolean isClosed();

	/**
	 * @return a string describing the address of the remote end of this
	 *         connection
	 */
	public abstract String getRemoteAddressInfo();

	/**
	 * @return a string describing the address of the local end of this connection
	 */
	public abstract String getLocalAddressInfo();
}
