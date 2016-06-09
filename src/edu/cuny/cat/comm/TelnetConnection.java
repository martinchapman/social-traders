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

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

/**
 * <p>
 * A connection on the cat game server side to a telnet client.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public class TelnetConnection extends SocketBasedConnection<TelnetMessage> {

	static Logger logger = Logger.getLogger(TelnetConnection.class);

	public TelnetConnection(final SocketChannel socketChannel) {
		super(socketChannel);
	}

	public TelnetMessage getMessage() throws CatException {

		if (isClosed()) {
			throw new ConnectionException("Connection is not ready to get messages !");
		}

		String line = null;
		try {
			while (((line = readLine()) != null) && (line.trim().length() == 0)) {
				// keep reading until non-empty line or end of stream
			}
		} catch (final ClosedByInterruptException e) {
			e.printStackTrace();
			throw new ConnectionException("Channel closed due to interrupts !");
		} catch (final AsynchronousCloseException e) {
			// e.printStackTrace();
			// throw new CatpConnectionException(
			// "Channel closed normally by another thread !");
			line = null;
		} catch (final IOException e) {
			e.printStackTrace();
			throw new ConnectionException(
					"IOException occurred while looking for message startline !");
		}

		if (line == null) {
			// end of stream
			return null;
		}

		line = line.trim();
		final TelnetMessage msg = new TelnetMessage();
		msg.setContent(line);
		return msg;
	}
}
