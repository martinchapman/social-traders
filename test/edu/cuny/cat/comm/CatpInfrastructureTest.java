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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import edu.cuny.cat.MyTestCase;
import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.util.Galaxy;
import edu.cuny.util.SyncTask;

/**
 * @author Jinzhong Niu
 * @version $Revision: 1.1 $
 */

public class CatpInfrastructureTest extends MyTestCase {

	static Logger logger = Logger.getLogger(CatpInfrastructureTest.class);

	EventEngine eventEngine;

	public CatpInfrastructureTest(final String name) {
		super(name);

		eventEngine = Galaxy.getInstance().getDefaultTyped(EventEngine.class);
	}

	public void testSocketBased() {
		System.out.println("\n>>>>>>>>>\t " + "testSocketBased() \n");

		final SocketBasedInfrastructureImpl infrast = new SocketBasedInfrastructureImpl();
		infrast.initialize();
		checkMessagePassing(infrast);
	}

	public void testQueueBased() {
		System.out.println("\n>>>>>>>>>\t " + "testQueueBased() \n");

		final QueueBasedInfrastructureImpl infrast = new QueueBasedInfrastructureImpl();
		checkMessagePassing(infrast);
	}

	public void testCallBased() {
		System.out.println("\n>>>>>>>>>\t " + "testCallBased() \n");

		final CallBasedInfrastructureImpl infrast = new CallBasedInfrastructureImpl();
		checkMessagePassing(infrast);
	}

	public void checkMessagePassing(final CatpInfrastructure infrast) {

		Galaxy.getInstance().put(Galaxy.getInstance().getDefaultSystem(),
				CatpInfrastructure.class, infrast);

		final ServerThread server = new ServerThread(infrast);
		final ClientThread client = new ClientThread(infrast);

		server.start();
		client.start();

		try {
			Thread.sleep(100);
			while (server.isAlive() || client.isAlive()) {
				Thread.sleep(100);
				System.out
						.println(" Still waiting for connection-setup threads to finish !");
				System.out.flush();
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		Assert.assertTrue(server.getConnection() != null);
		Assert.assertTrue(client.getConnection() != null);

		try {
			final ReactiveConnection<CatpMessage> sConn = server.getConnection();
			sConn.setListener(server);
			sConn.open();
			final ReactiveConnection<CatpMessage> cConn = client.getConnection();
			cConn.setListener(client);
			cConn.open();

			final SyncTask syncTask = new SyncTask(getClass(), 500);
			syncTask.setTag(server.getClass());

			syncTask.setCount(1);
			CatpMessage msg = CatpRequest.createRequest(CatpMessage.ASK);

			server.setExpectedMessage(msg);
			cConn.sendMessage(msg);

			System.out.println("Message sent from client to server.");
			System.out.flush();

			syncTask.sync();
			if (syncTask.getCount() > 0) {
				Assert.assertTrue(false);
			}

			syncTask.setTag(client.getClass());
			syncTask.setCount(1);

			msg = CatpResponse.createResponse(CatpMessage.OK);
			client.setExpectedMessage(msg);
			sConn.sendMessage(msg);

			System.out.println("Message sent from server to client.");
			System.out.flush();

			syncTask.sync();
			if (syncTask.getCount() > 0) {
				Assert.assertTrue(false);
			}

			sConn.setListener(null);
			cConn.setListener(null);

			server.close();
			client.close();
			syncTask.terminate();

			Assert.assertTrue(true);

		} catch (final CatException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	private class ServerThread extends Thread implements
			ConnectionListener<CatpMessage> {

		private final ServerConnector<CatpMessage> serverConnector;

		private ReactiveConnection<CatpMessage> connection;

		private CatpMessage msg;

		public ServerThread(final Infrastructure<CatpMessage> infrast) {
			serverConnector = infrast.createServerConnector();
		}

		@Override
		public void run() {
			try {

				System.out.println("Waiting for connection ...");
				System.out.flush();

				connection = ListenableConnection
						.makeReactiveConnection(serverConnector.accept());

				System.out.println("Connection setup.");
				System.out.flush();

			} catch (final CatException e) {
				e.printStackTrace();
			}
		}

		public ReactiveConnection<CatpMessage> getConnection() {
			return connection;
		}

		public void setExpectedMessage(final CatpMessage msg) {
			this.msg = msg;
		}

		public void messageArrived(final CatpMessage msg) {
			Assert.assertTrue(this.msg.getStartLine().equals(msg.getStartLine()));
			eventEngine.dispatchEvent(CatpInfrastructureTest.class, new Event(this,
					getClass()));
			System.out.println("Message received from client to server.");
		}

		public void close() {
			try {
				serverConnector.close();
				connection.close();
			} catch (final CatException e) {
				e.printStackTrace();
			}
		}
	}

	private class ClientThread extends Thread implements
			ConnectionListener<CatpMessage> {

		private final ClientConnector<CatpMessage> clientConnector;

		private ReactiveConnection<CatpMessage> connection;

		private CatpMessage msg;

		public ClientThread(final Infrastructure<CatpMessage> infrast) {
			clientConnector = infrast.createClientConnector();
		}

		@Override
		public void run() {
			while (true) {
				try {

					System.out.println("Connecting to server ...");

					connection = ListenableConnection
							.makeReactiveConnection(clientConnector.connect());

					System.out.println("Connection request accepted.");

					break;

				} catch (final CatpServerUnavailableException e) {
					try {
						System.out.println("Server not listening. Try to connect again.");
						Thread.sleep(100);
					} catch (final InterruptedException e1) {
						e1.printStackTrace();
					}
				} catch (final CatException e) {
					e.printStackTrace();
					Assert.assertTrue(false);
				}
			}
		}

		public ReactiveConnection<CatpMessage> getConnection() {
			return connection;
		}

		public void setExpectedMessage(final CatpMessage msg) {
			this.msg = msg;
		}

		public void messageArrived(final CatpMessage msg) {
			Assert.assertTrue(this.msg.getStartLine().equals(msg.getStartLine()));
			eventEngine.dispatchEvent(CatpInfrastructureTest.class, new Event(this,
					getClass()));
			System.out.println("Message received from server to client.");
		}

		public void close() {
			try {
				connection.close();
			} catch (final CatException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(CatpInfrastructureTest.suite());
	}

	public static Test suite() {
		return new TestSuite(CatpInfrastructureTest.class);
	}
}