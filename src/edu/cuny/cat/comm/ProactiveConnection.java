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
 * A proactive {@link Message} I/O interface for a plain-text message client or
 * server to communicate with its peers, which need proactively retrieve
 * messages from the communication channel.
 * </p>
 * 
 * <p>
 * Note that here <b>proactive</b> means this connection should be accessed
 * <b>proactively</b> rather than <b>reactively</b>.
 * </p>
 * 
 * @param <M>
 *          the type of messages that can be transmitted through the connection.
 * 
 * @see ReactiveConnection
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 * 
 */
public interface ProactiveConnection<M extends Message> extends Connection<M> {

	/**
	 * reads a {@link Message} from this connection. It blocks if no messages are
	 * available.
	 * 
	 * @return message received over the connection.
	 * @throws MessageException
	 */
	public abstract M getMessage() throws CatException;
}
