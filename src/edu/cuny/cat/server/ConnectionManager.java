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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Observer;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import edu.cuny.cat.comm.CatException;
import edu.cuny.cat.comm.CatpInfrastructure;
import edu.cuny.cat.comm.CatpMessage;
import edu.cuny.cat.comm.Connection;
import edu.cuny.cat.comm.ConnectionException;
import edu.cuny.cat.comm.MessageHandler;
import edu.cuny.cat.comm.ServerConnector;
import edu.cuny.cat.comm.Session;
import edu.cuny.cat.core.AccountHolder;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.cat.event.ClientStateUpdatedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.GameOverEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.task.Dispatcher;
import edu.cuny.cat.task.DispatchingTask;
import edu.cuny.cat.task.EventDispatchingTask;
import edu.cuny.cat.task.EventDispatchingTaskOnServerSide;
import edu.cuny.cat.task.IncomingMessageDispatchingTask;
import edu.cuny.cat.task.MessageDispatchingTask;
import edu.cuny.cat.task.OutgoingMessageDispatchingTask;
import edu.cuny.cat.task.PriorityAsynchronousDispatcher;
import edu.cuny.cat.task.PrioritySynchronousDispatcher;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.random.Uniform;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Utils;

/**
 * A class, as a thread, accepts connection requests, creates
 * {@link ConnectionAdaptor}s, and manage them, including event passing, etc..
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.64 $
 */
public final class ConnectionManager extends Thread implements
		AuctionEventListener {

	static Logger logger = Logger.getLogger(ConnectionManager.class);

	protected CatpInfrastructure infrast;

	protected GameController controller;

	protected ServerConnector<CatpMessage> serverConnector;

	protected boolean stopAcceptingConnection;

	protected IdentityOffice identityOffice;

	/**
	 * normal connections
	 */
	protected SortedMap<String, ConnectionAdaptor> adaptors;

	/**
	 * connections with clients that have yet to check in.
	 */
	protected Set<ConnectionAdaptor> babyAdaptors;

	/**
	 * a separate thread to process event-dispatching tasks
	 */
	protected Dispatcher dispatcher;

	/**
	 * used to randomize event dispatching order
	 */
	protected Uniform uniform;

	public ConnectionManager() {
		controller = GameController.getInstance();

		infrast = Galaxy.getInstance().getDefaultTyped(CatpInfrastructure.class);
		serverConnector = infrast.createServerConnector();

		adaptors = Collections
				.synchronizedSortedMap(new TreeMap<String, ConnectionAdaptor>());
		babyAdaptors = Collections
				.synchronizedSet(new HashSet<ConnectionAdaptor>());

		identityOffice = new IdentityOffice();

		uniform = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	public IdentityOffice getIdentityOffice() {
		return identityOffice;
	}

	/**
	 * terminiates the connections and the event dispatcher.
	 */
	public void terminate() {
		stopAcceptingConnection();
		dispatcher.terminate();

		// TODO: remove all adaptors

		babyAdaptors.clear();
		adaptors.clear();

		serverConnector = null;
		controller = null;
		infrast = null;
	}

	@Override
	public void run() {

		Connection<CatpMessage> conn;
		ConnectionAdaptor adaptor;

		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		if (infrast.isSynchronous()) {
			dispatcher = new PrioritySynchronousDispatcher(
					new EventBeforeMessageComparator());
		} else {
			dispatcher = new PriorityAsynchronousDispatcher(
					new EventBeforeMessageComparator());
		}

		ConnectionManager.logger.debug("server listening ...");

		while (true) {
			try {
				conn = serverConnector.accept();

				adaptor = new ConnectionAdaptor(this, conn);
				babyAdaptors.add(adaptor);

			} catch (final CatException e) {
				if (!stopAcceptingConnection) {
					e.printStackTrace();
				}

				break;
			}
		}
	}

	protected void stopAcceptingConnection() {
		stopAcceptingConnection = true;
		try {
			serverConnector.close();
		} catch (final ConnectionException e) {
			ConnectionManager.logger.error(e);
		}
	}

	public void removeBabyAdaptor(final ConnectionAdaptor adaptor) {
		babyAdaptors.remove(adaptor);
	}

	public void removeAdaptor(final String clientId) {
		adaptors.remove(clientId);
	}

	public ConnectionAdaptor getAdaptor(final String clientId) {
		ConnectionAdaptor adaptor = null;
		if (adaptors.containsKey(clientId)) {
			adaptor = adaptors.get(clientId);
		}

		return adaptor;
	}

	/**
	 * processes the connection adaptor over which a client checked in. This is
	 * processed here instead of through ClientStateUpdatedEvent because the
	 * adaptor has no reference before the client's name is allocated.
	 * 
	 * @param adaptor
	 *          the connection adaptor for the client that just checked in.
	 */
	public synchronized void clientCheckIn(final ConnectionAdaptor adaptor) {
		adaptors.put(adaptor.getClientId(), adaptor);
		babyAdaptors.remove(adaptor);

		if (adaptor.isTrader()) {
			/* TODO: different types of adaptor for traders and specialists */
			adaptor.setValuer(controller.getValuerFactory().createValuer(
					adaptor.isSeller()));

			// show info only after specialists begin to check in to avoid too much
			// log
			if (controller.getRegistry().getNumOfWorkingSpecialists() > 0) {
				ConnectionManager.logger
						.info("Server checked in " + adaptor.getClientId() + " ("
								+ adaptor.getConnection().getRemoteAddressInfo() + ").\n\n"
								+ Utils.indent(controller.getRegistry().getClientStatInfo())
								+ "\n");
			}
		} else {
			ConnectionManager.logger.info("Server checked in "
					+ adaptor.getClientId() + " ("
					+ adaptor.getConnection().getRemoteAddressInfo() + ").\n\n"
					+ Utils.indent(controller.getRegistry().getClientStatInfo()) + "\n");
		}

	}

	// /////////////////////////////////////////////////////

	/**
	 * process event without forwarding to connection adaptors
	 * 
	 * @param event
	 */
	public void eventOccurred(final AuctionEvent event) {
		processEventLocally(event);
	}

	public void processEventLocally(final AuctionEvent event) {

		/* update the tag for all the connection adaptors */
		if ((event instanceof GameStartingEvent)
				|| (event instanceof DayOpeningEvent)
				|| (event instanceof GameOverEvent)) {
			ConnectionAdaptor.tag = String.valueOf(event.getDay());

			/* if synchronous infrastructure is used, no need to wait for new clients */
			if (event instanceof GameStartingEvent) {
				if (infrast.isSynchronous() && !stopAcceptingConnection) {
					stopAcceptingConnection();
				}
			}
		}

		ConnectionAdaptor adaptor = null;
		if (event instanceof ClientStateUpdatedEvent) {

			final ClientStateUpdatedEvent csuEvent = (ClientStateUpdatedEvent) event;
			final AccountHolder client = csuEvent.getClient();

			adaptor = adaptors.get(client.getId());
			if (adaptor == null) {
				// it's possible that state changed at the adaptor before the adaptor is
				// identified by the manager (still in babyAdaptors), so do not consider
				// this as an error
				ConnectionManager.logger.debug("Adaptor for " + client.getId()
						+ " doesn't exist during processing client state change event:");
				ConnectionManager.logger.debug(event.toString());
				return;
			}

			switch (csuEvent.getCurrentState().getCode()) {

			case ClientState.CONN_CLOSED:

				removeAdaptor(client.getId());

				/*
				 * this may be normal when the simulation terminates, or problematic
				 * during a game
				 */
				ConnectionManager.logger
						.debug("Connection adaptor for " + client.getId()
								+ " removed due to connection closed !\n\n"
								+ Utils.indent(controller.getRegistry().getClientStatInfo())
								+ "\n");

				break;

			case ClientState.FATAL:
			case ClientState.ERROR:

				/* TODO: need do anything in other cases? */

			}
		}
	}

	/**
	 * dispatches an event to all clients
	 * 
	 * @param event
	 */
	public void dispatchEvent(final AuctionEvent event) {
		dispatchEvent(event, adaptors.keySet());
	}

	/**
	 * dispatches an event to specified client receivers.
	 * 
	 * This method is called by some connection adaptor to notify other adaptors.
	 * 
	 * @param event
	 * @param receiverIdColl
	 */
	public void dispatchEvent(final AuctionEvent event,
			final Collection<String> receiverIdColl) {
		dispatchEvent(event, receiverIdColl, null);
	}

	public void dispatchEvent(final AuctionEvent event,
			final Collection<String> receiverIdColl, final Observer observer) {

		/* consider resource pool of dispatching tasks in the future */
		final EventDispatchingTaskOnServerSide task = new EventDispatchingTaskOnServerSide(
				adaptors, event, randomize(receiverIdColl.toArray(new String[0])));

		if (observer != null) {
			task.addObserver(observer);
		}

		dispatcher.processTask(task);
	}

	/**
	 * aims to enqueue all messages outgoing to clients and get them sent in
	 * order.
	 * 
	 * NOTE: This is not used currently and may be deleted in the future.
	 * 
	 * @param msg
	 * @param session
	 * @param clientId
	 * @param observer
	 */
	public void dispatchOutgoingMessage(final CatpMessage msg,
			final Session<CatpMessage> session, final String clientId,
			final Observer observer) {

		final OutgoingMessageDispatchingTask task = new OutgoingMessageDispatchingTask(
				msg, session, clientId);

		if (observer != null) {
			task.addObserver(observer);
		}

		dispatcher.processTask(task);
	}

	/**
	 * aims to enqueue messages incoming from clients and get them process in
	 * order along with events.
	 * 
	 * @param msg
	 * @param handler
	 * @param clientId
	 * @param observer
	 */
	public void dispatchIncomingMessage(final CatpMessage msg,
			final MessageHandler<CatpMessage> handler, final String clientId,
			final Observer observer) {

		final IncomingMessageDispatchingTask task = new IncomingMessageDispatchingTask(
				msg, handler, clientId);

		if (observer != null) {
			task.addObserver(observer);
		}

		dispatcher.processTask(task);
	}

	/**
	 * 
	 */
	public void processDispatchingTasks() {
		dispatcher.process();
	}

	/**
	 * prints all the pending tasks enqueued, for debugging purpose only.
	 */
	public void printPendingTasks() {
		final DispatchingTask tasks[] = dispatcher.getTasks();
		Arrays.sort(tasks, new EventBeforeMessageComparator());
		ConnectionManager.logger.info("Pending tasks (" + tasks.length
				+ ")\n=============");
		for (int i = 0; i < tasks.length; i++) {
			ConnectionManager.logger.info(i + ". " + tasks[i]);
		}
	}

	/**
	 * randomize the order of objects in an array.
	 * 
	 * @param <T>
	 *          the type of elements in the array.
	 * 
	 * @param array
	 *          the array
	 * @return the randomized array, which is actually the same array passed in.
	 */
	public synchronized <T> T[] randomize(final T array[]) {
		T temp;
		int index;
		for (int i = array.length - 1; i > 0; i--) {
			index = uniform.nextIntFromTo(0, i);
			temp = array[i];
			array[i] = array[index];
			array[index] = temp;
		}
		return array;
	}

	class EventBeforeMessageComparator implements Comparator<DispatchingTask> {

		public int compare(DispatchingTask t1, DispatchingTask t2) {
			if ((t1 instanceof EventDispatchingTask)
					&& (t2 instanceof MessageDispatchingTask)) {
				return -1;
			} else if ((t1 instanceof MessageDispatchingTask)
					&& (t2 instanceof EventDispatchingTask)) {
				return 1;
			} else if (((t1 instanceof EventDispatchingTask) && (t2 instanceof EventDispatchingTask))
					|| ((t1 instanceof MessageDispatchingTask) && (t2 instanceof MessageDispatchingTask))) {
				if (t1.tid < t2.tid) {
					return -1;
				} else if (t1.tid > t2.tid) {
					return 1;
				} else {
					return 0;
				}
			} else {
				ConnectionManager.logger.error("Invalid task type: " + t1 + " ? " + t2);
				Utils.printStackTraces();
				Utils.fatalError(t1 + " " + t2);
				return 0;
			}
		}
	}
}
