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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

/**
 * <p>
 * An implementation of a simple telnet-oriented server connector.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public class TelnetServerConnector {

	static Logger logger = Logger.getLogger(TelnetServerConnector.class);

	private ServerSocketChannel serverChannel;

	public TelnetServerConnector(final int port) {
		try {
			final InetSocketAddress address = new InetSocketAddress(InetAddress
					.getLocalHost(), port);
			final int backlog = 1000;

			serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(true);
			serverChannel.socket().bind(address, backlog);

			// logger.info("\t\tserver: " + address.getHostName());
			// logger.info("\t\tport: " + address.getPort() + " ...");

		} catch (final IOException e) {
			TelnetServerConnector.logger
					.fatal("Failed in preparing server socket channel on port " + port
							+ " !", e);
		}
	}

	public TelnetConnection accept() throws ConnectionException {

		SocketChannel socketChannel = null;
		try {
			socketChannel = serverChannel.accept();
		} catch (final AsynchronousCloseException e) {
			throw new ConnectionException(
					"Server socket channel closed by another thread !");
		} catch (final IOException e) {
			e.printStackTrace();
			throw new ConnectionException(
					"IOException occurred with the listening server socket channel !");
		}

		return new TelnetConnection(socketChannel);
	}

	public void close() throws ConnectionException {
		try {
			serverChannel.close();
		} catch (final IOException e) {
			throw new ConnectionException("Failed in closing server socket channel !");
		}
	}
}
