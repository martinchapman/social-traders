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

package edu.cuny.cat;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.collections15.Buffer;
import org.apache.commons.collections15.BufferUtils;
import org.apache.commons.collections15.buffer.UnboundedFifoBuffer;
import org.apache.log4j.Logger;

import edu.cuny.cat.comm.CatException;
import edu.cuny.cat.comm.CatpInfrastructure;
import edu.cuny.cat.comm.CatpMessage;
import edu.cuny.cat.comm.CatpMessageErrorException;
import edu.cuny.cat.comm.CatpMessageException;
import edu.cuny.cat.comm.CatpMessageInvalidException;
import edu.cuny.cat.comm.CatpProactiveSession;
import edu.cuny.cat.comm.CatpReactiveSession;
import edu.cuny.cat.comm.CatpRequest;
import edu.cuny.cat.comm.CatpResponse;
import edu.cuny.cat.comm.ClientConnector;
import edu.cuny.cat.comm.Connection;
import edu.cuny.cat.comm.ConnectionException;
import edu.cuny.cat.comm.ConnectionListener;
import edu.cuny.cat.comm.ListenableConnection;
import edu.cuny.cat.comm.ReactiveConnection;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.cat.event.AvailableMarketsAnnouncedEvent;
import edu.cuny.cat.event.AvailableTradersAnnouncedEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.FeesAnnouncedEvent;
import edu.cuny.cat.event.GameOverEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.IdAssignedEvent;
import edu.cuny.cat.event.ProfitAnnouncedEvent;
import edu.cuny.cat.event.RegisteredTradersAnnouncedEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.event.RoundClosingEvent;
import edu.cuny.cat.event.RoundOpenedEvent;
import edu.cuny.cat.event.ShoutPostedEvent;
import edu.cuny.cat.event.SubscriptionEvent;
import edu.cuny.cat.event.TransactionPostedEvent;
import edu.cuny.cat.stat.ClientDynamicsReport;
import edu.cuny.cat.stat.CombiGameReport;
import edu.cuny.cat.stat.GameReport;
import edu.cuny.cat.task.Dispatcher;
import edu.cuny.cat.task.EventDispatchingTaskOnClientSide;
import edu.cuny.cat.task.SynchronousDispatcher;
import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.random.Uniform;
import edu.cuny.util.Galaxy;
import edu.cuny.util.ParamClassLoadException;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * A generic class featuring the common behavior of market (specialist) clients
 * and trader clients.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.type</tt><br>
 * <font size=-1>string beginning with <tt>SELLER</tt>, <tt>BUYER</tt>, or
 * <tt>SEPECIALIST</tt></font></td>
 * <td valign=top>the type of client</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.id</tt><br>
 * <font size=-1>string</font></td>
 * <td valign=top>the id the client wants to be assigned (optional)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.conntrialmax</tt><br>
 * <font size=-1>int &gt;=1 (<code>1000</code> by default)</font></td>
 * <td valign=top>the maximum number of attempts to connect to server</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.logging</tt><br>
 * <font size=-1>boolean (<code>false</code> by default)</font></td>
 * <td valign=top>whether to generate logging info</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.report</tt><br>
 * <font size=-1>class, inherits {@link GameReport}</font></td>
 * <td valign=top>a report collecting information on activiites on this market</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>game_client</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.72 $
 * 
 */

public abstract class GameClient implements Parameterizable, Runnable,
		ConnectionListener<CatpMessage>, AuctionEventListener, Observer {

	static Logger logger = Logger.getLogger(GameClient.class);

	public static final String P_CONNECTION_TRIAL_MAX = "conntrialmax";

	public static final String P_TYPE = "type";

	public static final String P_ID = "id";

	public static final String P_LOGGING = "logging";

	public static final String P_REPORT = "report";

	public static final String P_DEF_BASE = "game_client";

	protected ClientConnector<CatpMessage> clientConnector;

	protected ReactiveConnection<CatpMessage> connection;

	/**
	 * the maximal number of connection attempts to make
	 */
	protected int connTrialMax = 1000;

	/**
	 * receives and processes event-dispatching tasks.
	 */
	protected Dispatcher dispatcher;

	protected LinkedList<AuctionEventListener> eventListeners;

	/**
	 * self-initiated on-going sessions on current trading day.
	 */
	protected Buffer<CatpProactiveSession> proactiveSessions;

	/**
	 * sessions that are possibly initiated by the server at the moment.
	 */
	protected CatpReactiveSession reactiveSessions[];

	/**
	 * the type of this client.
	 */
	protected String type;

	/**
	 * the id of this client.
	 */
	protected String clientId;

	protected ClientRegistry registry;

	/**
	 * data report.
	 */
	protected GameReport report;

	/**
	 * the current catp tag that is used to validate the timestamp of messages.
	 */
	protected String tag = "init";

	protected boolean logging = false;

	protected CatpInfrastructure infrast;

	protected EventEngine eventEngine;

	protected GlobalPRNG prng;

	/*****************************************************************************
	 * 
	 * constructors and setup
	 * 
	 ****************************************************************************/

	public GameClient() {
		eventEngine = Galaxy.getInstance().getTyped(Game.P_CAT, EventEngine.class);
		prng = Galaxy.getInstance().getTyped(Game.P_CAT, GlobalPRNG.class);
		infrast = Galaxy.getInstance().getTyped(Game.P_CAT,
				CatpInfrastructure.class);

		clientConnector = infrast.createClientConnector();

		dispatcher = new SynchronousDispatcher();
		eventListeners = new LinkedList<AuctionEventListener>();

		proactiveSessions = BufferUtils
				.synchronizedBuffer(new UnboundedFifoBuffer<CatpProactiveSession>());

		registry = createRegistry();

		addAuctionEventListener(this);
		addAuctionEventListener(registry);
	}

	protected abstract ClientRegistry createRegistry();

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(GameClient.P_DEF_BASE);

		connTrialMax = parameters.getIntWithDefault(base
				.push(GameClient.P_CONNECTION_TRIAL_MAX), defBase
				.push(GameClient.P_CONNECTION_TRIAL_MAX), connTrialMax);

		type = parameters.getStringWithDefault(base.push(GameClient.P_TYPE), null,
				type);

		clientId = parameters.getStringWithDefault(base.push(GameClient.P_ID),
				null, null);

		logging = parameters.getBoolean(base.push(GameClient.P_LOGGING), null,
				false);

		try {
			report = parameters.getInstanceForParameter(base
					.push(GameClient.P_REPORT), defBase.push(GameClient.P_REPORT),
					GameReport.class);
			addAuctionEventListener(report);
		} catch (final ParamClassLoadException e) {
			report = null;
		}

		if ((report != null) && (report instanceof Parameterizable)) {
			((Parameterizable) report).setup(parameters, base
					.push(GameClient.P_REPORT));
		}

	}

	protected void cleanUpGamely() {
		clearPendingProactiveSessions();
	}

	protected void cleanUpDaily() {
		clearPendingProactiveSessions();
	}

	public String getId() {
		return clientId;
	}

	public void setId(final String clientId) {
		this.clientId = clientId;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	/**
	 * Add a new market data report.
	 * 
	 * @param newReport
	 *          The new report to add.
	 */
	public void addReport(final GameReport newReport) {

		if (report == null) {
			// no report available
			setReport(newReport);
		} else {
			if (!(report instanceof CombiGameReport)) {
				// current report is a primitive report, create a combi report
				final GameReport oldReport = report;
				setReport(new CombiGameReport());
				((CombiGameReport) report).addReport(oldReport);
			}

			((CombiGameReport) report).addReport(newReport);
		}
	}

	public void setReport(final GameReport newReport) {
		removeAuctionEventListener(report);
		report = newReport;
		removeAuctionEventListener(newReport);
		addAuctionEventListener(report);
	}

	public GameReport getReport() {
		return report;
	}

	/*****************************************************************************
	 * 
	 * connection related
	 * 
	 ****************************************************************************/

	protected void connectToServer() {
		int i = connTrialMax;

		loginfo(clientId + " (proposed) connecting to " + infrast + " ... ");

		Connection<CatpMessage> conn = null;
		while ((i--) >= 0) {
			try {
				conn = clientConnector.connect();
				break;
			} catch (final ConnectionException e) {
				try {
					Thread.sleep(200);
				} catch (final InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (final Exception e) {
				GameClient.logger.error(e.toString());
			}
		}

		if (conn == null) {
			GameClient.logger.error("Failed to connect to server after "
					+ connTrialMax + " attempts!");
			return;
		} else {
			connection = ListenableConnection.makeReactiveConnection(conn);
			try {
				connection.setListener(this);
				connection.open();
			} catch (final CatException e) {
				e.printStackTrace();
				GameClient.logger.fatal(e.toString());
				return;
			}
		}

		loginfo(clientId + " (proposed) connected.");
	}

	protected void close() {
		reactiveSessions = new CatpReactiveSession[] {};
		proactiveSessions.clear();

		try {
			connection.close();

			loginfo(clientId + " closed connection.");

		} catch (final ConnectionException e) {
			e.printStackTrace();
			GameClient.logger.fatal("Error in closing connection. ", e);
		}
	}

	public void run() {

		connectToServer();

		if (connection == null) {
			return;
		}

		final CatpRequest request = CatpRequest.createRequest(CatpMessage.CHECKIN,
				new String[] { CatpMessage.TYPE, type, CatpMessage.VERSION,
						CatpMessage.CURRENT_VERSION });
		if (clientId != null) {
			request.addHeader(CatpMessage.ID, clientId);
		}

		final CatpProactiveSession session = new CheckInSession(request);
		startProactiveSession(session);
	}

	/*****************************************************************************
	 * 
	 * session related
	 * 
	 ****************************************************************************/

	protected void startProactiveSession(final CatpProactiveSession session) {
		proactiveSessions.add(session);
		try {
			session.sendRequest();
		} catch (final CatException e) {
			proactiveSessions.remove(session);
			GameClient.logger.error("Failed to send request: \n"
					+ session.getRequest());
		}
	}

	protected void setExpectedReactiveSession(final CatpReactiveSession session) {
		setExpectedReactiveSessions(new CatpReactiveSession[] { session });
	}

	protected void setExpectedReactiveSessions(
			final CatpReactiveSession sessions[]) {
		reactiveSessions = sessions;
	}

	protected void dectedAndRunReactiveSessions(final CatpRequest request,
			final CatpReactiveSession sessions[]) {

		CatpResponse response = null;
		CatpReactiveSession session = null;
		for (final CatpReactiveSession session2 : sessions) {
			try {
				session = (CatpReactiveSession) session2.clone();
				session.processRequest(request);
				return;
			} catch (final CatpMessageException e) {
				if (session.isProcessed()) {
					final String s = "Failed in processing request from server in "
							+ session.getClass().getSimpleName() + ".";
					try {
						if (e instanceof CatpMessageInvalidException) {
							response = CatpResponse
									.createResponse(CatpMessage.INVALID, new String[] {
											CatpMessage.TEXT, s + " Error:" + e.toString() });
							response.setTag(request.getTag());
							connection.sendMessage(response);
						} else if (e instanceof CatpMessageErrorException) {
							response = CatpResponse
									.createResponse(CatpMessage.ERROR, new String[] {
											CatpMessage.TEXT, s + " Error:" + e.toString() });
							response.setTag(request.getTag());
							connection.sendMessage(response);
						}
						GameClient.logger.error(s + " Request:\n" + request, e);
					} catch (final CatException e1) {
						e1.printStackTrace();
						GameClient.logger.error(s
								+ "\n Failed to send error response as well.", e1);
					}
					close();
					return;
				}
			} catch (final CatException e) {
				e.printStackTrace();
				GameClient.logger.fatal(e);
			} catch (final Exception e) {
				e.printStackTrace();
				GameClient.logger.fatal(e);
			}
		}
	}

	protected void clearPendingProactiveSessions() {
		final CatpProactiveSession sessions[] = proactiveSessions
				.toArray(new CatpProactiveSession[0]);
		if (sessions.length > 0) {
			GameClient.logger.info("< Pending proactive sessions at "
					+ getClass().getSimpleName() + " named " + getId() + "\n");
			for (final CatpProactiveSession session : sessions) {
				GameClient.logger.info(" pending proactive session: " + session);
			}
			GameClient.logger.info(">\n");

			for (final CatpProactiveSession session : sessions) {
				session.forceOut();
				proactiveSessions.remove(session);
			}

		}
	}

	protected class CheckInSession extends CatpProactiveSession {

		public CheckInSession(final CatpRequest request) {
			super(connection, request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			// tells the local server, if applicable, that I'm in !
			eventEngine.dispatchEvent(GameClient.class, new Event(this,
					CatpMessage.CHECKIN));

			if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)) {
				final String id_assigned = response.getHeader(CatpMessage.ID);
				if ((id_assigned == null) || (id_assigned.length() == 0)) {
					/* proposed id passed by the server */
					if (clientId == null) {
						/* if no proposed id, an id from server is expected. */
						throw new CatpMessageErrorException("Client ID expected !");
					}
				} else {
					/* use the id assigned by the server */
					clientId = id_assigned;
				}

				setExpectedReactiveSessions(new CatpReactiveSession[] {
						new SyncSession(), new GameStartingSession(),
						new OracleSession("CheckedIn") });

				dispatchEvent(new IdAssignedEvent(clientId));

				reportDynamics("checked in");

				loginfo(clientId + " checked in.");

			} else {
				throw new CatpMessageErrorException("Unexpected "
						+ response.getStatusCode() + " response received !");
			}
		}

		public String toString() {
			return "CheckInSession";
		}
	}

	protected class SubscribeToSpecialistSession extends CatpProactiveSession {

		private final String specialistIds[];

		public SubscribeToSpecialistSession(final String specialistIds[]) {
			super(connection);
			this.specialistIds = specialistIds;

			setRequest(CatpRequest.createRequest(CatpMessage.SUBSCRIBE, new String[] {
					CatpMessage.ID, CatpMessage.concatenate(specialistIds) }));

			getRequest().setTag(tag);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)) {
				for (final String specialistId : specialistIds) {
					dispatchEvent(new SubscriptionEvent(getId(), specialistId));
				}
			} else {
				/*
				 * no longer throw exception, simply overlook the failure.
				 * 
				 * this could be caused by the failure of a specialist to be subscribed.
				 */
				final CatpMessageErrorException e = new CatpMessageErrorException(
						"Unexpected " + response.getStatusCode() + " response received !");
				e.printStackTrace();
			}
		}
	}

	protected class GameStartingSession extends CatpReactiveSession {

		public GameStartingSession() {
			super(connection, CatpMessage.OPTIONS, CatpMessage.GAMESTARTING);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			printGameInfo();

			tag = request.getTag();

			setExpectedReactiveSessions(new CatpReactiveSession[] {
					new PostTraderSession(), new PostSpecialistSession(),
					new GameStartedSession(), new OracleSession("GameStarting") });

			int[] durations = null;
			final GameStartingEvent event = new GameStartingEvent();
			try {
				durations = CatpMessage.parseIntegers(request
						.getHeader(CatpMessage.VALUE));
				if ((durations == null) || (durations.length != 2)) {
					GameClient.logger
							.error("2 ints expected as day length and round length info in OPTIONS GameStarting request !");
				} else {
					event.setDayLen(durations[0]);
					event.setRoundLen(durations[1]);
				}
			} catch (final CatException e) {
				GameClient.logger
						.error(
								"Failed to parse out day length and round length info in OPTIONS GameStarting request !",
								e);
			}

			addEventDispatchingTask(event);

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();

		}
	}

	protected class GameStartedSession extends CatpReactiveSession {

		public GameStartedSession() {
			super(connection, CatpMessage.OPTIONS, CatpMessage.GAMESTARTED);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			setExpectedReactiveSessions(new CatpReactiveSession[] {
					getDayOpeningSessionInstance(), new OracleSession("GameStarted") });

			addEventDispatchingTask(new GameStartedEvent());

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	protected class GameOverSession extends CatpReactiveSession {

		public GameOverSession() {
			super(connection, CatpMessage.OPTIONS, CatpMessage.GAMEOVER);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			tag = request.getTag();

			cleanUpGamely();

			setExpectedReactiveSessions(new CatpReactiveSession[] {
					new GameStartingSession(), new OracleSession("GameOver") });

			addEventDispatchingTask(new GameOverEvent());

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	protected abstract CatpReactiveSession getDayOpeningSessionInstance();

	protected abstract CatpReactiveSession getDayOpenedSessionInstance();

	protected class DayClosedSession extends CatpReactiveSession {
		public DayClosedSession() {
			super(connection, CatpMessage.OPTIONS, CatpMessage.DAYCLOSED);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String idList = request.getHeader(CatpMessage.ID);
			if (idList == null) {
				throw new CatpMessageErrorException("Invalid specialist id list in "
						+ reqType + " " + typeHeader + " request !");
			} else {
				final String ids[] = CatpMessage.parseStrings(idList);
				final String popularityList = request.getHeader(CatpMessage.VALUE);
				final int popularities[] = CatpMessage.parseIntegers(popularityList);
				if (ids.length != popularities.length) {
					throw new CatpMessageErrorException(
							"Inconsistent popularity list in " + reqType + " " + typeHeader
									+ " request !");
				}

				Specialist specialist = null;
				for (final String id2 : ids) {
					specialist = registry.getSpecialist(id2);
					if (specialist == null) {
						GameClient.logger
								.debug("Unknown specialist " + id2 + " in " + reqType + " "
										+ typeHeader + " request to " + clientId + " !");
					}
				}

				cleanUpDaily();

				setExpectedReactiveSessions(new CatpReactiveSession[] {
						new GameOverSession(), getDayOpeningSessionInstance(),
						new OracleSession("DayClosed") });

				for (int i = 0; i < ids.length; i++) {
					specialist = registry.getSpecialist(ids[i]);
					if (specialist != null) {
						addEventDispatchingTask(new RegisteredTradersAnnouncedEvent(
								specialist, popularities[i]));
					}
				}

				final DayClosedEvent event = new DayClosedEvent();
				event.setTime(CatpMessage.parseIntegers(request
						.getHeader(CatpMessage.TIME)));
				addEventDispatchingTask(event);

				final CatpResponse response = CatpResponse
						.createResponse(CatpMessage.OK);
				response.setTag(request.getTag());
				sendMessage(response);

				processEventDispatchingTasks();

				if (logging) {
					registry.printStatus();
				}
			}
		}
	}

	protected class RoundOpenedSession extends CatpReactiveSession {
		public RoundOpenedSession() {
			super(connection, CatpMessage.OPTIONS, CatpMessage.ROUNDOPENED);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final int time[] = CatpMessage.parseIntegers(request
					.getHeader(CatpMessage.TIME));
			printRoundInfo(time[1]);

			final RoundOpenedEvent event = new RoundOpenedEvent();
			event.setTime(time);
			addEventDispatchingTask(event);

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	protected class RoundClosingSession extends CatpReactiveSession {
		public RoundClosingSession() {
			super(connection, CatpMessage.OPTIONS, CatpMessage.ROUNDCLOSING);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final RoundClosingEvent event = new RoundClosingEvent();
			event.setTime(CatpMessage.parseIntegers(request
					.getHeader(CatpMessage.TIME)));
			addEventDispatchingTask(event);

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	protected class RoundClosedSession extends CatpReactiveSession {
		public RoundClosedSession() {
			super(connection, CatpMessage.OPTIONS, CatpMessage.ROUNDCLOSED);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final RoundClosedEvent event = new RoundClosedEvent();
			event.setTime(CatpMessage.parseIntegers(request
					.getHeader(CatpMessage.TIME)));
			addEventDispatchingTask(event);

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	protected class PostSession extends CatpReactiveSession {
		public PostSession(final String typeHeader) {
			super(connection, CatpMessage.POST, typeHeader);
		}
	}

	protected class PostFeeSession extends PostSession {
		public PostFeeSession() {
			super(CatpMessage.FEE);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String marketId = request.getHeader(CatpMessage.ID);
			Specialist specialist = null;
			double fees[] = null;
			if ((marketId == null) || (marketId.length() == 0)) {
				throw new CatpMessageErrorException("Empty market id in " + reqType
						+ " " + typeHeader + " request !");
			} else if (registry.getSpecialist(marketId) == null) {
				GameClient.logger.error(marketId + " is not in " + clientId
						+ "'s specialist list:");
				GameClient.logger.error(registry.getSpecialistIds());
				GameClient.logger.error("fees charged by " + marketId
						+ " are disregarded !\n");

				/* not a severe problem, do not throw an exception any more */
				// throw new CatpMessageInvalidException("Invalid market id in " +
				// reqType
				// + " " + typeHeader + " request !");
			} else {
				final String priceList = request.getHeader(CatpMessage.VALUE);
				fees = CatpMessage.parseDoubles(priceList);
				if ((fees == null) || (fees.length != 5)) {
					throw new CatpMessageErrorException("Invalid price list in "
							+ reqType + " " + typeHeader + " request !");
				}

				specialist = registry.getSpecialist(marketId);
			}

			/* forward the event only when the specialist is known */
			if (specialist != null) {
				specialist.setFees(fees);
				final FeesAnnouncedEvent event = new FeesAnnouncedEvent(specialist);
				addEventDispatchingTask(event);
			}

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	protected class PostClientListSession extends PostSession {

		protected String idList;

		public PostClientListSession(final String typeHeader) {
			super(typeHeader);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			idList = request.getHeader(CatpMessage.ID);
			if (idList == null) {
				throw new CatpMessageErrorException("Valid ID header expected in "
						+ reqType + " " + typeHeader + " request !");
			}
		}
	}

	protected class PostTraderSession extends PostClientListSession {
		public PostTraderSession() {
			super(CatpMessage.TRADER);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String traderIds[] = CatpMessage.parseStrings(idList);
			final List<Trader> traderList = new LinkedList<Trader>();
			Trader trader = null;
			for (final String traderId : traderIds) {
				trader = registry.getTrader(traderId);
				if (trader == null) {
					trader = registry.addTrader(traderId, null, traderId.toLowerCase()
							.startsWith(CatpMessage.SELLER.toLowerCase()));
				}
				traderList.add(trader);
			}
			final AvailableTradersAnnouncedEvent event = new AvailableTradersAnnouncedEvent(
					traderList);
			addEventDispatchingTask(event);

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	protected class PostSpecialistSession extends PostClientListSession {
		public PostSpecialistSession() {
			super(CatpMessage.SPECIALIST);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String specialistIds[] = CatpMessage.parseStrings(idList);

			// NOTE: it's crucial to use a data structure that has predictable
			// iterating order to obtain reproducibility
			final List<Specialist> specialistList = new LinkedList<Specialist>();
			Specialist specialist = null;
			for (final String specialistId : specialistIds) {
				specialist = registry.getSpecialist(specialistId);
				if (specialist == null) {
					specialist = registry.addSpecialist(specialistId);
				}

				specialistList.add(specialist);
			}
			final AvailableMarketsAnnouncedEvent event = new AvailableMarketsAnnouncedEvent(
					specialistList);
			addEventDispatchingTask(event);

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	protected class PostProfitSession extends PostSession {
		public PostProfitSession() {
			super(CatpMessage.PROFIT);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String idList = request.getHeader(CatpMessage.ID);
			if (idList == null) {
				throw new CatpMessageErrorException("Null specialist id list in "
						+ reqType + " " + typeHeader + " request !");
			} else {
				final String ids[] = CatpMessage.parseStrings(idList);
				final String profitList = request.getHeader(CatpMessage.VALUE);
				final double profits[] = CatpMessage.parseDoubles(profitList);
				if (profits.length != ids.length) {
					throw new CatpMessageErrorException("Inconsistent profit list in "
							+ reqType + " " + typeHeader + " request !");
				}

				for (int i = 0; i < ids.length; i++) {
					Specialist specialist = registry.getSpecialist(ids[i]);
					if (specialist == null) {
						GameClient.logger
								.info("Unknown specialist " + ids[i] + " in " + reqType + " "
										+ typeHeader + " request to " + clientId + " !");
						specialist = registry.addSpecialist(ids[i]);
					}
					specialist.getAccount().setBalance(profits[i]);
				}
			}

			final Iterator<Specialist> iterator = registry.getSpecialists()
					.iterator();
			while (iterator.hasNext()) {
				addEventDispatchingTask(new ProfitAnnouncedEvent(iterator.next()));
			}

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	protected class PostShoutSession extends PostSession {
		public PostShoutSession(final String typeHeader) {
			super(typeHeader);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String idList = request.getHeader(CatpMessage.ID);
			final String ids[] = CatpMessage.parseStrings(idList);
			if ((ids == null) || (ids.length != 3)) {
				throw new CatpMessageErrorException("Invalid id list in " + reqType
						+ " " + typeHeader + " request !");
			} else {

				try {
					final double price = request.getDoubleHeader(CatpMessage.VALUE);

					final Shout shout = new Shout(ids[0], 1, price, typeHeader
							.equals(CatpMessage.BID));
					shout.setState(Shout.PLACED);

					Trader trader = registry.getTrader(ids[1]);
					if (trader == null) {
						GameClient.logger.error(shout.getId()
								+ " came from unknown trader " + ids[1]);
						// record this unknown trader
						trader = registry.addTrader(ids[1], null, shout.isAsk());
					}
					shout.setTrader(trader);

					Specialist specialist = registry.getSpecialist(ids[2]);
					if (specialist == null) {
						GameClient.logger.error(shout.getId()
								+ " placed at an unknown specialist " + ids[2]);
						// record this unknown specialist
						specialist = registry.addSpecialist(ids[2]);
					}
					shout.setSpecialist(specialist);

					// trader client and market client behave differently here
					// postShoutReceived(shout);

					final Shout currentShout = registry.getShout(shout.getId());

					if (currentShout != null) {
						if ((currentShout.getTrader() != null)
								&& (shout.getTrader() != currentShout.getTrader())) {
							GameClient.logger
									.fatal("Posted shout already existing in local registry !"
											+ " The two shouts are unexpectedly from different traders !");
							GameClient.logger.fatal("shout: " + shout);
							GameClient.logger.fatal("currentShout: " + currentShout);
						}

						if (shout.getSpecialist() != currentShout.getSpecialist()) {
							GameClient.logger
									.fatal("Posted shout already existing in local registry !"
											+ " The two shouts are unexpectedly associated with different specialists !");
							GameClient.logger.fatal("shout: " + shout);
							GameClient.logger.fatal("currentShout: " + currentShout);
						}

						if (currentShout.isMatched()) {
							// this should never be reached !
							GameClient.logger
									.fatal("Posted shout came after its existing record in the local registry is set MATCHED at "
											+ clientId + " !");
							GameClient.logger.error("shout: " + shout);
							GameClient.logger.error("currentShout: " + currentShout);
						}
					}

					// logger.info("\t ----- Shouts ------ " + shouts.keySet());

					if (Shout.TRACE) {
						GameClient.logger.info("\t CSp: " + shout);
					}

					final AuctionEvent event = new ShoutPostedEvent(shout);
					final int time[] = CatpMessage.parseIntegers(request
							.getHeader(CatpMessage.TIME));
					event.setTime(time);
					addEventDispatchingTask(event);

					final CatpResponse response = CatpResponse
							.createResponse(CatpMessage.OK);
					response.setTag(request.getTag());
					sendMessage(response);

				} catch (final CatException e) {
					e.printStackTrace();
					GameClient.logger.error(e);
					final CatpResponse response = CatpResponse.createResponse(
							CatpMessage.ERROR, new String[] {
									CatpMessage.TEXT,
									"Failed to obtain shout price in POST " + typeHeader
											+ " message !" });
					response.setTag(request.getTag());
					sendMessage(response);
				}

				processEventDispatchingTasks();
			}
		}
	}

	// /**
	// * This method should be implemented by {@link TraderClient} and
	// * {@link MarketClient} respectively to check the validity of the posted
	// * shout.
	// *
	// * @param shout
	// * the posted shout.
	// */
	// protected abstract void postShoutReceived(Shout shout);

	protected class PostAskSession extends PostShoutSession {
		public PostAskSession() {
			super(CatpMessage.ASK);
		}
	}

	protected class PostBidSession extends PostShoutSession {
		public PostBidSession() {
			super(CatpMessage.BID);
		}
	}

	protected class PostTransactionSession extends PostSession {
		public PostTransactionSession() {
			super(CatpMessage.TRANSACTION);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String idList = request.getHeader(CatpMessage.ID);
			final String ids[] = CatpMessage.parseStrings(idList);
			if ((ids == null) || (ids.length != 4)) {
				throw new CatpMessageErrorException("Invalid id list in " + reqType
						+ " " + typeHeader + " request !");
			} else {
				try {
					final String priceList = request.getHeader(CatpMessage.VALUE);
					final double prices[] = CatpMessage.parseDoubles(priceList);
					if ((prices == null) || (prices.length != 3)) {
						throw new CatpMessageErrorException("Invalid price list in "
								+ reqType + " " + typeHeader + " request !");
					} else {

						final int quantity = 1;

						final int time[] = CatpMessage.parseIntegers(request
								.getHeader(CatpMessage.TIME));

						Specialist specialist = registry.getSpecialist(ids[3]);
						if (specialist == null) {
							GameClient.logger.error("Transaction " + ids[0]
									+ " made at an unknown specialist " + ids[3] + " !");
							// record this unknown specialist
							specialist = registry.addSpecialist(ids[3]);
						}

						final Shout ask = createPostedMatchedShout(ids[1], quantity,
								prices[1], false, specialist);
						final Shout bid = createPostedMatchedShout(ids[2], quantity,
								prices[2], true, specialist);

						final Transaction transaction = new Transaction(ids[0], ask, bid,
								prices[0]);
						transaction.setSpecialist(specialist);

						// trader client and market client behave differently here
						postTransactionReceived(transaction);

						AuctionEvent event = null;
						event = new TransactionPostedEvent(transaction);
						event.setTime(time);
						addEventDispatchingTask(event);

						final CatpResponse response = CatpResponse
								.createResponse(CatpMessage.OK);
						response.setTag(request.getTag());
						sendMessage(response);
					}
				} catch (final CatException e) {
					e.printStackTrace();
					GameClient.logger.error(e);
					final CatpResponse response = CatpResponse
							.createResponse(
									CatpMessage.ERROR,
									new String[] { CatpMessage.TEXT,
											"Failed to obtain transaction price in POST TRANSACTION message !" });
					response.setTag(request.getTag());
					sendMessage(response);
				}

				processEventDispatchingTasks();
			}
		}

		protected Shout createPostedMatchedShout(final String shoutId,
				final int quantity, final double price, final boolean isBid,
				Specialist specialist) {

			final Shout matchedShout = GameClient.createMatchedShoutSimple(shoutId,
					quantity, price, isBid, specialist);
			final Shout recordedShout = registry.getShout(shoutId);

			if (recordedShout == null) {
				// no record of matched shout, error
				GameClient.logger.fatal(clientId
						+ " does not have record of posted transacted shout !");
				GameClient.logger.fatal("matchedShout: " + matchedShout);
			} else {
				// know this shout before
				checkAndUpdateMatchedShout(matchedShout, recordedShout);
			}

			return matchedShout;
		}
	}

	/**
	 * creates a shout with a matched state.
	 * 
	 * @param shoutId
	 * @param quantity
	 * @param price
	 * @param isBid
	 * @param specialist
	 * @return the matched shout.
	 */
	protected static Shout createMatchedShoutSimple(final String shoutId,
			final int quantity, final double price, final boolean isBid,
			Specialist specialist) {
		final Shout matchedShout = new Shout(shoutId, quantity, price, isBid);
		matchedShout.setSpecialist(specialist);
		matchedShout.setState(Shout.MATCHED);

		return matchedShout;
	}

	/**
	 * checks the information in a matched shout against the existing record.
	 * 
	 * @param matchedShout
	 *          the matched shout
	 * @param recordedShout
	 *          the existing record of the matched shout, which is from the local
	 *          registry
	 */
	protected void checkAndUpdateMatchedShout(Shout matchedShout,
			Shout recordedShout) {
		if (recordedShout.getSpecialist() == null) {
			// every shout should have a specialist set
			GameClient.logger
					.error("Null specialist in the previous record of the shout in a transaction !");
			GameClient.logger.error("recordedShout: " + recordedShout);
		} else if (recordedShout.getSpecialist() != matchedShout.getSpecialist()) {
			GameClient.logger
					.error("Existing shout should have been placed at specialist "
							+ matchedShout.getSpecialist().getId()
							+ " that made the transaction !");
			GameClient.logger.error("recordedShout: " + recordedShout);
		}

		if (recordedShout.getPrice() != matchedShout.getPrice()) {
			// modified shout not received, so local price not updated

			// show error message only, do not try to correct anything
			GameClient.logger
					.error("Existing record of matched shout has a different price from "
							+ matchedShout.getPrice() + " in the transaction posted to "
							+ clientId + " !");
			GameClient.logger.error("recordedShout: " + recordedShout);
		}

		// use the trader info in the existing shout
		matchedShout.setTrader(recordedShout.getTrader());
	}

	/**
	 * This method should be implemented by {@link TraderClient} and
	 * {@link MarketClient} respectively to check the validity of the posted
	 * transaction.
	 * 
	 * @param transaction
	 *          the posted transaction
	 */
	protected abstract void postTransactionReceived(Transaction transaction);

	/**
	 * For a failed client to reconnect in and synchronize with the game.
	 */
	protected class SyncSession extends CatpReactiveSession {

		public SyncSession() {
			super(connection, null);
		}

		public void processRequest(final CatpRequest request) throws CatException {

			setProcessed(true);

			if (request == null) {
				throw new CatpMessageErrorException(
						"Empty request received while synchronization.");
			} else if (CatpMessage.OPTIONS.equalsIgnoreCase(request.getType())) {

				if (CatpMessage.GAMESTARTING.equalsIgnoreCase(request
						.getHeader(CatpMessage.TYPE))) {

					/* this is a fresh start, pass on to GameStartingSession */

					setProcessed(false);

					throw new CatpMessageErrorException(
							"Simply pass the request to the following GameStartingSession.");

				} else if (CatpMessage.DAYCLOSED.equalsIgnoreCase(request
						.getHeader(CatpMessage.TYPE))) {

					/*
					 * reconnect and restart, sends events to do necessary initialization
					 * and sync on day closed
					 */

					dispatchEvent(new GameStartingEvent());
					dispatchEvent(new GameStartedEvent());

					// do what POST SPECIALIST does based on info in OPTIONS DAYCLOSED
					final String idList = request.getHeader(CatpMessage.ID);
					String specialistIds[] = new String[0];
					if (idList != null) {
						specialistIds = CatpMessage.parseStrings(idList);
					}

					for (final String specialistId : specialistIds) {
						if (registry.getSpecialist(specialistId) != null) {
							/*
							 * possibly the original specialist list are available, so do not
							 * overwrite
							 */
						} else {
							registry.addSpecialist(specialistId);
						}
					}

					/* TODO: use more ways to get latest specialist list */
					final AvailableMarketsAnnouncedEvent event = new AvailableMarketsAnnouncedEvent(
							registry.getSpecialists());
					dispatchEvent(event);

					setExpectedReactiveSessions(new CatpReactiveSession[] {
							new GameOverSession(), getDayOpeningSessionInstance(),
							new OracleSession("DayClosed") });

				} /* end of day closed processing */
			} /* end of options request processing */

			/* always respond with OK */
			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);
		}
	}

	/**
	 * a session deals with all unexpected request.
	 */
	protected class OracleSession extends CatpReactiveSession {

		String state;

		public OracleSession(final String state) {
			super(connection, null);
			this.state = state;
		}

		public void processRequest(final CatpRequest request) throws CatException {

			setProcessed(true);

			if (!connection.isClosed()) {
				String responseType;
				if (request.getType().equalsIgnoreCase(CatpMessage.ASK)
						|| request.getType().equalsIgnoreCase(CatpMessage.BID)
						|| request.getType().equalsIgnoreCase(CatpMessage.POST)) {
					responseType = CatpMessage.INVALID;
				} else {
					responseType = CatpMessage.ERROR;
				}
				GameClient.logger.error(clientId
						+ " failed in processing request following " + state + ": \n"
						+ request);
				showSessions(reactiveSessions);

				final CatpResponse response = CatpResponse.createResponse(responseType,
						new String[] { CatpMessage.TYPE, CatpMessage.WRONGTIME,
								CatpMessage.TEXT, "Requested at a wrong time " + state + "." });

				if (request.getTag() == null) {
					GameClient.logger.error("No tag found in request from server: \n"
							+ request);
				} else {
					response.setTag(request.getTag());
				}

				sendMessage(response);
			}
		}

		public String getState() {
			return state;
		}

		public String toString() {
			return getClass().getSimpleName() + "(" + state + ")";
		}
	}

	/*****************************************************************************
	 * 
	 * auction listener related
	 * 
	 ****************************************************************************/

	public void addAuctionEventListener(final AuctionEventListener listener) {
		if (!eventListeners.contains(listener)) {
			eventListeners.add(listener);
		} else {
			GameClient.logger.error("Attempts to add duplicate event listerners to "
					+ clientId + " !");
		}
	}

	public void removeAuctionEventListener(final AuctionEventListener listener) {
		if (eventListeners != null) {
			eventListeners.remove(listener);
		} else {
			GameClient.logger.warn("Failed in removing event listeners to "
					+ clientId + " !");
		}
	}

	protected void dispatchEvent(final AuctionEvent event) {
		addEventDispatchingTask(event);
		processEventDispatchingTasks();
	}

	protected void addEventDispatchingTask(final AuctionEvent event) {
		/* consider resource pool of dispatching tasks in the future */
		final EventDispatchingTaskOnClientSide task = new EventDispatchingTaskOnClientSide(
				eventListeners, event);
		task.addObserver(this);
		dispatcher.addTask(task);
	}

	protected void processEventDispatchingTasks() {
		dispatcher.process();
	}

	public void update(final Observable o, final Object arg) {
		if (o instanceof EventDispatchingTaskOnClientSide) {
			// event dispatching failed

			final EventDispatchingTaskOnClientSide task = (EventDispatchingTaskOnClientSide) o;
			final AuctionEvent event = task.getEvent();
			final AuctionEventListener listener = (AuctionEventListener) arg;

			// failed to dispatch event to listener
			// TODO: to deal with aftermath?

			GameClient.logger.error(clientId + " failed in dispatching " + event
					+ " to " + listener + " !");

		}
	}

	/*****************************************************************************
	 * 
	 * message processing related
	 * 
	 ****************************************************************************/

	public synchronized void messageArrived(final CatpMessage msg) {
		CatpResponse response = null;
		CatpRequest request = null;

		if (msg == null) {

			if (!proactiveSessions.isEmpty()) {
				// catp connection problem occurred
				GameClient.logger.error("Error in " + clientId
						+ " getting message from connection with server !");
			}

			close();

		} else {

			if (msg instanceof CatpResponse) {

				response = (CatpResponse) msg;

				if (proactiveSessions.isEmpty()) {
					GameClient.logger
							.error("Unexpected response received from server :\n" + response);
					close();
				} else {
					final CatpProactiveSession session = proactiveSessions.remove();
					try {
						session.processResponse(response);
						return;
					} catch (final CatpMessageException e) {
						e.printStackTrace();
						GameClient.logger.error("Failed in "
								+ session.getClass().getSimpleName()
								+ " with the response from server : \n" + response, e);
					} catch (final CatException e) {
						e.printStackTrace();
						GameClient.logger.fatal(e);
					} catch (final Exception e) {
						e.printStackTrace();
						GameClient.logger.fatal("Exception while processing response: \n"
								+ response, e);
					}

					close();
				}
			} else {
				request = (CatpRequest) msg;
				dectedAndRunReactiveSessions(request, reactiveSessions);
			}
		}
	}

	/*****************************************************************************
	 * 
	 * others
	 * 
	 ****************************************************************************/

	public String toString() {
		return getClass().getSimpleName();
	}

	/*****************************************************************************
	 * 
	 * debugging
	 * 
	 ****************************************************************************/

	protected void reportDynamics(final String info) {
		reportDynamics(clientId, info);
	}

	public void reportDynamics(final String clientId, final String info) {
		final Event te = new Event(clientId);
		te.setValue(ClientDynamicsReport.ID, clientId);
		te.setValue(ClientDynamicsReport.INFO, info);
		eventEngine.dispatchEvent(ClientDynamicsReport.class, te);
	}

	protected void showSessions(final Object sessions[]) {
		if (sessions.length > 0) {
			GameClient.logger.info("< " + getClass().getSimpleName() + ": "
					+ clientId + "\n");
		}
		for (final Object session : sessions) {
			GameClient.logger.info("\t session: " + session.toString());
		}

		if (sessions.length > 0) {
			GameClient.logger.info(">\n");
		}
	}

	protected void printGameInfo() {
		loginfo("Game\n");
	}

	protected void printDayInfo(final int day) {
		loginfo("\n  Game day " + day + "\n");
	}

	protected void printRoundInfo(final int round) {
		loginfo("    Game round " + round + "\n");
	}

	protected void loginfo(final String msg) {
		if (logging) {
			GameClient.logger.info(msg);
		}
	}

	/*****************************************************************************
	 * 
	 * methods for testing the impact of session timeout
	 * 
	 ****************************************************************************/

	/*
	 * tells whether a timeout has occurred or not.
	 */
	boolean tried = false;

	protected void testMarketTimeout() {
		if (type.equalsIgnoreCase(CatpMessage.SPECIALIST)
				&& clientId.equalsIgnoreCase("CH")) {
			testTimeout();
		}
	}

	protected void testTraderTimeout() {
		if (clientId.equalsIgnoreCase("buyerZIC_19")) {
			testTimeout();
		}
	}

	protected void testTimeout() {
		final Uniform uniform = new Uniform(0, 1, prng.getEngine());
		if ((uniform.nextDouble() < 1) && !tried) {
			synchronized (uniform) {
				try {
					tried = true;
					GameClient.logger.info("Triggering timeout ...\n");

					// the time to wait should be no shorter than the length of session
					// timeout in parameter file
					uniform.wait(10000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
