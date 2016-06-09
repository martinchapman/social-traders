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
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

/**
 * <p>
 * An abstract implementation of {@link Connection} when the communication is
 * socket-based. Child classes should be defined to support concrete message
 * parsing, e.g., {@link SocketBasedCatpConnection} and {@link TelnetConnection}
 * .
 * </p>
 * 
 * @param <M>
 *          the type of messages that can be transmitted through the connection.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

public abstract class SocketBasedConnection<M extends Message> implements
		ProactiveConnection<M> {

	protected static Logger logger = Logger
			.getLogger(SocketBasedConnection.class);

	protected SocketChannel socketChannel;

	protected static final int BUFFER_SIZE = 40960;

	protected ByteBuffer readBuffer = null;

	protected boolean skipLF = false;

	public SocketBasedConnection(final SocketChannel socketChannel) {
		this.socketChannel = socketChannel;

		initBuffer();
	}

	protected void initBuffer() {
		readBuffer = ByteBuffer.allocate(SocketBasedConnection.BUFFER_SIZE);
		readBuffer.flip();
	}

	public void sendMessage(final M msg) throws CatException {
		if (isClosed()) {
			throw new ConnectionException(
					"Connection is not ready to send message:\n" + msg);
		}

		try {
			socketChannel.write(ByteBuffer.wrap(msg.toString().getBytes()));
		} catch (final IOException e) {
			if (socketChannel.isOpen()) {
				e.printStackTrace();
			} else {
				// assume that the connection is proactively closed.
			}
			throw new ConnectionException(
					"IOException occurred while sending message:\n" + msg);

		}
	}

	/**
	 * read a plain-text line from the socket channel. The implementation is based
	 * on the code of {@link java.io.BufferedReader#readLine()}.
	 * 
	 * @return a non-null string if successful or null if EOF was met.
	 * 
	 * @throws IOException
	 */
	protected String readLine() throws IOException {
		final StringBuilder s = new StringBuilder();

		for (;;) {

			// TODO: charset?
			if (!readBuffer.hasRemaining()) {

				// make ready for writing
				readBuffer.compact();

				int nBytes = 0;
				nBytes = socketChannel.read(readBuffer);

				// make ready for reading
				readBuffer.flip();

				if (nBytes == -1) { /* EOF */
					if (s.length() == 0) {
						return null;
					} else {
						return s.toString();
					}
				}
			}

			boolean eol = false;
			char c = 0;

			// TODO: charset?
			charLoop: while (readBuffer.hasRemaining()) {

				// TODO: charset?
				c = (char) readBuffer.get();

				switch (c) {

				case '\r':

					skipLF = true;
					eol = true;
					break charLoop;

				case '\n':

					if (skipLF && (c == '\n')) {
						/* Skip a leftover '\n', if necessary */
						skipLF = false;
						continue charLoop;
					} else {
						eol = true;
						break charLoop;
					}

				default:

					s.append(c);

				}
			}

			if (eol) {
				return s.toString();
			}
		}
	}

	public void open() throws ConnectionException {
		try {
			socketChannel.configureBlocking(true);
		} catch (final IOException e) {
			e.printStackTrace();
			SocketBasedConnection.logger.error(e);
			throw new ConnectionException(e.toString());
		}
	}

	public void close() throws ConnectionException {

		try {
			if (isClosed()) {
				return;
			} else {
				socketChannel.close();
			}
		} catch (final IOException e) {
			e.printStackTrace();
			SocketBasedConnection.logger.error(e);
			throw new ConnectionException(e.toString());
		}
	}

	public boolean isClosed() {
		return (socketChannel == null) || !socketChannel.isOpen();
	}

	public String getLocalAddressInfo() {
		if (socketChannel != null) {
			return socketChannel.socket().getLocalAddress().getHostName() + ":"
					+ socketChannel.socket().getLocalPort();
		} else {
			return null;
		}
	}

	public String getRemoteAddressInfo() {
		if (socketChannel != null) {
			return socketChannel.socket().getInetAddress().getHostName() + ":"
					+ socketChannel.socket().getPort();
		} else {
			return null;
		}
	}
}
