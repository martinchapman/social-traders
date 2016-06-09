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

package edu.cuny.cat.server;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cuny.cat.comm.ConnectionException;
import edu.cuny.cat.comm.ConnectionListener;
import edu.cuny.cat.comm.ListenableConnection;
import edu.cuny.cat.comm.Message;
import edu.cuny.cat.comm.ReactiveConnection;
import edu.cuny.cat.comm.TelnetConnection;
import edu.cuny.cat.comm.TelnetMessage;
import edu.cuny.cat.comm.TelnetServerConnector;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.SimulationOverEvent;
import edu.cuny.cat.registry.Registry;
import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * A console that enables to control the game through telnet.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.port</tt><br>
 * <font size=-1>int (default: 9091)</font></td>
 * <td valign=top>(the port number the telnet console of the cat game server
 * will be listening to)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.code</tt><br>
 * <font size=-1>string (default: cat)</tt></font></td>
 * <td valign=top>(the security code required to log in through telnet to
 * control the game)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>telnet</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public class TelnetConsole implements Console, Parameterizable, Runnable {

	static Logger logger = Logger.getLogger(TelnetConsole.class);

	public static String P_DEF_BASE = "telnet";

	public static String P_PORT = "port";

	public static String P_CODE = "code";

	public static int DEFAULT_PORT = 9091;

	public static String DEFAULT_CODE = "cat";

	protected int port = TelnetConsole.DEFAULT_PORT;

	protected String code = TelnetConsole.DEFAULT_CODE;

	protected TelnetServerConnector serverConnector;

	protected boolean stopAcceptingConnection = false;

	protected Thread thread;

	protected Set<TelnetConnectionAdaptor> adaptors;

	protected ClockStatus clockStatus;

	public TelnetConsole() {
		adaptors = new HashSet<TelnetConnectionAdaptor>();
		clockStatus = new ClockStatus();
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(TelnetConsole.P_DEF_BASE);
		port = parameters.getIntWithDefault(base.push(TelnetConsole.P_PORT),
				defBase.push(TelnetConsole.P_PORT), TelnetConsole.DEFAULT_PORT);
		code = parameters.getStringWithDefault(base.push(TelnetConsole.P_CODE),
				defBase.push(TelnetConsole.P_CODE), TelnetConsole.DEFAULT_CODE);
	}

	public String getCode() {
		return code;
	}

	public void start() {
		serverConnector = new TelnetServerConnector(port);
		thread = new Thread(this);
		thread.start();
	}

	public void stop() {
		stopAcceptingConnection = true;

		try {
			serverConnector.close();
		} catch (final ConnectionException e) {
			TelnetConsole.logger.error(e);
		}
		serverConnector = null;
		thread = null;

		// disposes all existing connections.
		final Iterator<TelnetConnectionAdaptor> itor = adaptors.iterator();
		while (itor.hasNext()) {
			final TelnetConnectionAdaptor adaptor = itor.next();
			dispose(adaptor);
		}
	}

	public boolean isInteractive() {
		return true;
	}

	public void run() {

		while (true) {
			try {
				final TelnetConnection conn = serverConnector.accept();

				// TODO: to manage these connections
				adaptors.add(new TelnetConnectionAdaptor(conn, this, clockStatus));
			} catch (final Exception e) {
				if (!stopAcceptingConnection) {
					e.printStackTrace();
				}

				break;
			}
		}

	}

	/**
	 * disposes the {@link edu.cuny.cat.comm.TelnetConnection} the adaptor works
	 * for.
	 * 
	 * @param adaptor
	 */
	public void dispose(final TelnetConnectionAdaptor adaptor) {
		adaptors.remove(adaptor);
		adaptor.terminate();
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof SimulationOverEvent) {
			clockStatus.endClock();
		}
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();
		s += " port:" + port + " code: *****";

		return s;
	}

	/**
	 * processes a {@link edu.cuny.cat.comm.TelnetConnection} from a remote user
	 * that monitors the game.
	 * 
	 */
	public static class TelnetConnectionAdaptor implements
			ConnectionListener<TelnetMessage> {

		static Logger logger = Logger.getLogger(TelnetConnectionAdaptor.class);

		protected EventEngine eventEngine;

		public static char ESC = (char) 27;

		public static char LN = '\n';

		public static char CR = '\r';

		public static String CRLN = "\r\n";

		public static String CRLNLN = "\r\n\n";

		protected ReactiveConnection<TelnetMessage> connection;

		protected TelnetConsole controller;

		protected Registry registry;

		protected ClockStatus clockStatus;

		protected boolean authenticated;

		public TelnetConnectionAdaptor(final TelnetConnection conn,
				final TelnetConsole controller, final ClockStatus clockStatus) {

			eventEngine = Galaxy.getInstance().getDefaultTyped(EventEngine.class);

			registry = GameController.getInstance().getRegistry();

			authenticated = false;

			connection = ListenableConnection.makeReactiveConnection(conn);

			this.controller = controller;
			this.clockStatus = clockStatus;

			openConnection();

			sendMessage("Welcome to CAT server !" + TelnetConnectionAdaptor.CRLNLN);
			prompt();

		}

		private void openConnection() {
			connection.setListener(this);

			try {
				connection.open();
			} catch (final ConnectionException e) {
				e.printStackTrace();
				TelnetConnectionAdaptor.logger.fatal(e.toString(), e);
				return;
			}
		}

		private void terminate() {
			try {
				connection.close();
				connection.setListener(null);
			} catch (final ConnectionException e) {
				e.printStackTrace();
				TelnetConnectionAdaptor.logger.fatal(
						"Failed to close the connection !", e);
			}
		}

		protected void sendMessage(final String msg) {
			final TelnetMessage telnetMsg = new TelnetMessage();
			telnetMsg.setContent(msg);

			try {
				connection.sendMessage(telnetMsg);
				return;
			} catch (final Exception e) {
				e.printStackTrace();
			}

			TelnetConnectionAdaptor.logger.error("Failed in sending message !");
		}

		public void messageArrived(final TelnetMessage msg) {
			if (msg == null) {
				controller.dispose(this);
			} else {
				final String command = msg.getContent();

				if (authenticated) {
					if (command.equalsIgnoreCase("start")) {
						sendMessage(" clock starting ..." + TelnetConnectionAdaptor.CRLN);
						if (clockStatus.startClock()) {
							eventEngine.dispatchEvent(GameClock.class, new Event(this,
									EventBasedClockController.START));
							sendMessage(" clock started." + TelnetConnectionAdaptor.CRLNLN);
						} else {
							sendMessage(" failed to start clock !"
									+ TelnetConnectionAdaptor.CRLNLN);
						}
					} else if (command.equalsIgnoreCase("pause")) {
						sendMessage(" clock pausing ..." + TelnetConnectionAdaptor.CRLN);
						if (clockStatus.pauseClock()) {
							eventEngine.dispatchEvent(GameClock.class, new Event(this,
									EventBasedClockController.PAUSE));
							sendMessage(" clock paused." + TelnetConnectionAdaptor.CRLNLN);
						} else {
							sendMessage(" failed to pause clock !"
									+ TelnetConnectionAdaptor.CRLNLN);
						}
					} else if (command.equalsIgnoreCase("resume")) {
						sendMessage(" clock resuming ..." + TelnetConnectionAdaptor.CRLN);
						if (clockStatus.resumeClock()) {
							eventEngine.dispatchEvent(GameClock.class, new Event(this,
									EventBasedClockController.RESUME));
							sendMessage(" clock resumed." + TelnetConnectionAdaptor.CRLNLN);
						} else {
							sendMessage(" failed to resume clock !"
									+ TelnetConnectionAdaptor.CRLNLN);
						}
					} else if (command.equalsIgnoreCase("quit")) {
						sendMessage(" quitting game ..." + TelnetConnectionAdaptor.CRLNLN);
						if (clockStatus.endClock()) {
							controller.dispose(this);
							eventEngine.dispatchEvent(GameClock.class, new Event(this,
									EventBasedClockController.QUIT));
							return;
						} else {
							// should never fail to quit
							sendMessage(" failed to quit game !"
									+ TelnetConnectionAdaptor.CRLNLN);
						}
					} else if (command.equalsIgnoreCase("exit")) {
						sendMessage(" Good Bye !" + TelnetConnectionAdaptor.CRLNLN);
						controller.dispose(this);
						return;
					} else if (command.equalsIgnoreCase("clients")) {
						sendMessage(registry.getClientStatInfo()
								+ TelnetConnectionAdaptor.CRLNLN);
					} else if (command.equalsIgnoreCase("traders")) {
						sendMessage(Message.concatenate(registry.getTraderIds(),
								TelnetConnectionAdaptor.CRLN)
								+ TelnetConnectionAdaptor.CRLNLN);
					} else if (command.equalsIgnoreCase("markets")) {
						sendMessage(Message.concatenate(registry.getSpecialistIds(),
								TelnetConnectionAdaptor.CRLN)
								+ TelnetConnectionAdaptor.CRLNLN);
					} else if (command.equalsIgnoreCase("time")) {
						sendMessage(GameController.getInstance().getClock().getTimeText()
								+ TelnetConnectionAdaptor.CRLNLN);
					} else {
						sendMessage(" Unrecognized command: " + command
								+ TelnetConnectionAdaptor.CRLNLN);
					}
				} else {
					if (command.equals(controller.getCode())) {
						authenticated = true;
						sendMessage(" You are authenticated."
								+ TelnetConnectionAdaptor.CRLNLN);
					} else {
						sendMessage(" Invalid code !" + TelnetConnectionAdaptor.CRLNLN);
					}
				}

				prompt();
			}
		}

		protected void prompt() {
			if (authenticated) {
				sendMessage("\r" + stylize("CAT> ", Color.GREEN));
			} else {
				sendMessage("\r" + stylize("Security code: ", Color.GREEN));
			}
		}

		protected String stylize(final String s, final Color c) {
			String prefix = "";
			if (c == Color.BLACK) {
				prefix = "[30m";
			} else if (c == Color.RED) {
				prefix = "[31m";
			} else if (c == Color.GREEN) {
				prefix = "[32m";
			}

			return TelnetConnectionAdaptor.ESC + prefix + TelnetConnectionAdaptor.ESC
					+ "[1m" + s + TelnetConnectionAdaptor.ESC + "[0m";
		}
	}

	/**
	 * records the status of the {@link GameClock}.
	 * 
	 * @author Jinzhong Niu
	 */
	public static class ClockStatus {

		protected boolean started = false;

		protected boolean paused = false;

		protected boolean ended = false;

		protected boolean isStartable() {
			return !started;
		}

		protected boolean isPauseable() {
			return started && !paused && !ended;
		}

		protected boolean isResumable() {
			return started && paused && !ended;
		}

		protected boolean isEndable() {
			return true;
		}

		public synchronized boolean startClock() {
			if (isStartable()) {
				started = true;
				return true;
			} else {
				return false;
			}
		}

		public synchronized boolean pauseClock() {
			if (isPauseable()) {
				paused = true;
				return true;
			} else {
				return false;
			}
		}

		public synchronized boolean resumeClock() {
			if (isResumable()) {
				paused = false;
				return true;
			} else {
				return false;
			}
		}

		public synchronized boolean endClock() {
			if (isEndable()) {
				ended = true;
				return true;
			} else {
				return false;
			}
		}
	}
}
