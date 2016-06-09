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
 * The implementation of {@link Connection} when
 * {@link SocketBasedInfrastructureImpl} is used.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.32 $
 */

public class SocketBasedCatpConnection extends
		SocketBasedConnection<CatpMessage> {

	static Logger logger = Logger.getLogger(SocketBasedCatpConnection.class);

	public SocketBasedCatpConnection(final SocketChannel socketChannel) {
		super(socketChannel);
	}

	/*
	 * returns a {@link CatpMessage}.
	 * 
	 * @see edu.cuny.cat.comm.SocketBasedConnection#getMessage()
	 */
	public CatpMessage getMessage() throws CatException {

		if (isClosed()) {
			throw new ConnectionException("Connection is not ready to get messages !");
		}

		CatpMessage msg = null;
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
		if (line.equalsIgnoreCase(CatpMessage.ASK)
				|| line.equalsIgnoreCase(CatpMessage.BID)
				|| line.equalsIgnoreCase(CatpMessage.TRANSACTION)
				|| line.equalsIgnoreCase(CatpMessage.GET)
				|| line.equalsIgnoreCase(CatpMessage.POST)
				|| line.equalsIgnoreCase(CatpMessage.SUBSCRIBE)
				|| line.equalsIgnoreCase(CatpMessage.OPTIONS)
				|| line.equalsIgnoreCase(CatpMessage.CHECKIN)
				|| line.equalsIgnoreCase(CatpMessage.REGISTER)) {
			// request
			msg = new CatpRequest();
		} else if (line.equalsIgnoreCase(CatpMessage.OK)
				|| line.equalsIgnoreCase(CatpMessage.ERROR)
				|| line.equalsIgnoreCase(CatpMessage.INVALID)) {
			// response
			msg = new CatpResponse();
		} else {
			throw new CatpMessageErrorException(
					"Invalid CatpMessage starting with \"" + line + "\"");
		}

		msg.setStartLine(line);
		try {
			while ((line = readLine()) != null) {
				line = line.trim();
				if (line.length() == 0) {
					// empty line, ending the message, return
					return msg;
				} else {
					// get even trailing empty string
					final String strings[] = line.split(":", -1);
					if (strings.length == 2) {
						msg.addHeader(strings[0].trim(), strings[1].trim());
					} else {
						throw new CatpMessageErrorException(
								"Invalid format in message header \"" + line + "\"");
					}
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
			throw new ConnectionException(
					"IOException occurred while reading field headers !");
		}

		throw new ConnectionException("Unexpected end of steam !");
	}
}
