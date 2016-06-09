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
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

/**
 * <p>
 * The implementation of {@link ClientConnector} when
 * {@link SocketBasedInfrastructureImpl} is used.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.15 $
 */

public class SocketBasedCatpClientConnector implements
		ClientConnector<CatpMessage> {

	static Logger logger = Logger.getLogger(SocketBasedCatpClientConnector.class);

	/**
	 * connects to the catp server whose address and port number are defined in
	 * the current {@link SocketBasedInfrastructureImpl}.
	 * 
	 * @return an instance of {@link SocketBasedCatpConnection}.
	 * @throws ConnectionException
	 */
	public Connection<CatpMessage> connect() throws ConnectionException {

		final SocketBasedInfrastructureImpl infrast = SocketBasedInfrastructureImpl
				.getInstance();
		if (infrast != null) {
			try {
				final InetSocketAddress address = new InetSocketAddress(infrast
						.getServer(), infrast.getPort());
				final SocketChannel socketChannel = SocketChannel.open(address);
				return new SocketBasedCatpConnection(socketChannel);
			} catch (final UnknownHostException e) {
				e.printStackTrace();
				throw new CatpServerUnavailableException("Invalid server address !");
			} catch (final ConnectException e) {
				// do not show error, since the caller will try again
				throw new ConnectionException("Server is not listening !");
			} catch (final IOException e) {
				e.printStackTrace();
				throw new ConnectionException(
						"IOException occurred while connecting to server !");
			} catch (final SecurityException e) {
				final String s = "Failed to setup connection to server for the sake of security !";
				SocketBasedCatpClientConnector.logger.error(s);
				throw new ConnectionException(s);
			}
		} else {
			throw new ConnectionException(
					"Unavailable SocketBasedInfrastructureImpl !");
		}
	}
}
