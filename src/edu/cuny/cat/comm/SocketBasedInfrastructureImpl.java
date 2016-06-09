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
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * The class implements a socket-based infrastructure for catp.
 * </p>
 * 
 * <p>
 * It is similar to {@link QueueBasedInfrastructureImpl} in the sense that both
 * are asynchronous. They differ in two aspects:
 * {@link QueueBasedInfrastructureImpl} does not require network resources, thus
 * for instance avoiding possible port conflicts as in
 * {@link SocketBasedInfrastructureImpl}, but
 * {@link SocketBasedInfrastructureImpl} supports the real distributed game
 * playing over the Internet and is the only available infrastructure
 * implementation for actual competitions while
 * {@link QueueBasedInfrastructureImpl} implies multiple threads with each for
 * the game server or one of the clients inside a single process.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.server</tt><br>
 * <font size=-1>string (default: localhost)</tt></font></td>
 * <td valign=top>(the domain name or IP address of the cat game server)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.port</tt><br>
 * <font size=-1>int (default: 9090)</font></td>
 * <td valign=top>(the port number the cat game server will be listening to)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>socket_based_infrastructure</tt></td>
 * </tr>
 * </table>
 * 
 * 
 * @see QueueBasedInfrastructureImpl
 * @see CallBasedInfrastructureImpl
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.22 $
 */

public class SocketBasedInfrastructureImpl implements CatpInfrastructure,
		Parameterizable {

	static Logger logger = Logger.getLogger(SocketBasedInfrastructureImpl.class);

	public static final String P_DEF_BASE = "socket_based_infrastructure";

	public static final String P_SERVER = "server";

	public static final String P_PORT = "port";

	protected int port = 9090;

	protected String server = null;

	public static SocketBasedInfrastructureImpl getInstance() {
		final CatpInfrastructure infrast = Galaxy.getInstance().getDefaultTyped(
				CatpInfrastructure.class);
		if (infrast instanceof SocketBasedInfrastructureImpl) {
			return (SocketBasedInfrastructureImpl) infrast;
		} else {
			SocketBasedInfrastructureImpl.logger
					.fatal("Unavailable SocketBasedInfrastructureImpl !");
			return null;
		}
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(
				SocketBasedInfrastructureImpl.P_DEF_BASE);

		server = parameters.getStringWithDefault(base
				.push(SocketBasedInfrastructureImpl.P_SERVER), defBase
				.push(SocketBasedInfrastructureImpl.P_SERVER), server);

		port = parameters.getIntWithDefault(base
				.push(SocketBasedInfrastructureImpl.P_PORT), defBase
				.push(SocketBasedInfrastructureImpl.P_PORT), port);

		initialize();
	}

	protected void initialize() {

		if (server == null) {
			try {
				server = InetAddress.getLocalHost().getHostName();
			} catch (final UnknownHostException e) {
				SocketBasedInfrastructureImpl.logger
						.fatal("A local game server is specified, but no IP address is available !");
				server = "localhost";
			}
		}

		if (port == 0) {
			// use a unused port, for the case of running a game inside one process
			allocatePort();
		} else if (port < 0) {
			SocketBasedInfrastructureImpl.logger
					.fatal("Invalid port number: " + port);
		}
	}

	protected void allocatePort() {
		ServerSocket socket;
		try {
			socket = new ServerSocket(0);
			port = socket.getLocalPort();
			socket.close();
		} catch (final IOException e) {
			e.printStackTrace();
			SocketBasedInfrastructureImpl.logger
					.fatal("Failed to allocate an unused port for game server !");
		}
	}

	/**
	 * @return an instance of {@link SocketBasedCatpClientConnector}.
	 */
	public ClientConnector<CatpMessage> createClientConnector() {
		return new SocketBasedCatpClientConnector();
	}

	/**
	 * @return an instance of {@link SocketBasedCatpServerConnector}.
	 */
	public ServerConnector<CatpMessage> createServerConnector() {
		return new SocketBasedCatpServerConnector();
	}

	public void cleanUp() {
		// nothing to do.
	}

	/**
	 * @return the domain name or ip address of the catp server.
	 */
	public String getServer() {
		return server;
	}

	/**
	 * sets the domain name or ip address of the catp server.
	 * 
	 * @param server
	 */
	public void setServer(final String server) {
		this.server = server;
	}

	/**
	 * @return the port number of the catp server.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * sets the port number of the catp server. By default, it is 9090.
	 * 
	 * @param port
	 */
	public void setPort(final int port) {
		this.port = port;
	}

	public boolean isSynchronous() {
		return false;
	}

	@Override
	public String toString() {
		return server + ":" + port;
	}
}
