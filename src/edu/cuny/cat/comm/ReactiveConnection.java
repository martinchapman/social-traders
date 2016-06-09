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
 * A reactive {@link CatpMessage} I/O interface for a catp client or server to
 * communicate with other game players. It needs a listener to register in
 * advance and the listener later on will receive messages automatically.
 * </p>
 * 
 * <p>
 * Note that here <b>reactive</b> means this connection can be accessed
 * <b>reactively</b> rather than <b>proactively</b>.
 * </p>
 * 
 * @param <M>
 *          the type of messages that can be transmitted through the connection.
 * 
 * @see ProactiveConnection
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 * 
 */
public interface ReactiveConnection<M extends Message> extends Connection<M> {

	public void setListener(ConnectionListener<M> listener);

	public ConnectionListener<M> getListener();
}
