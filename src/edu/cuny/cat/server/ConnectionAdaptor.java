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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections15.Buffer;
import org.apache.commons.collections15.BufferUtils;
import org.apache.commons.collections15.buffer.UnboundedFifoBuffer;
import org.apache.log4j.Logger;

import edu.cuny.cat.comm.CatException;
import edu.cuny.cat.comm.CatpMessage;
import edu.cuny.cat.comm.CatpMessageErrorException;
import edu.cuny.cat.comm.CatpMessageException;
import edu.cuny.cat.comm.CatpMessageInvalidException;
import edu.cuny.cat.comm.CatpProactiveSession;
import edu.cuny.cat.comm.CatpReactiveSession;
import edu.cuny.cat.comm.CatpRequest;
import edu.cuny.cat.comm.CatpResponse;
import edu.cuny.cat.comm.Connection;
import edu.cuny.cat.comm.ConnectionException;
import edu.cuny.cat.comm.ConnectionListener;
import edu.cuny.cat.comm.ListenableConnection;
import edu.cuny.cat.comm.MessageHandler;
import edu.cuny.cat.comm.ReactiveConnection;
import edu.cuny.cat.comm.Session;
import edu.cuny.cat.comm.TimableCatpProactiveSession;
import edu.cuny.cat.core.AccountHolder;
import edu.cuny.cat.core.IllegalShoutException;
import edu.cuny.cat.core.IllegalShoutInTransactionException;
import edu.cuny.cat.core.IllegalTransactionException;
import edu.cuny.cat.core.IllegalTransactionPriceException;
import edu.cuny.cat.core.InvalidChargeException;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.cat.event.AvailableMarketsAnnouncedEvent;
import edu.cuny.cat.event.AvailableTradersAnnouncedEvent;
import edu.cuny.cat.event.ClientStateUpdatedEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.FeesAnnouncedEvent;
import edu.cuny.cat.event.GameOverEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.PrivateValueAssignedEvent;
import edu.cuny.cat.event.ProfitAnnouncedEvent;
import edu.cuny.cat.event.RegistrationEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.event.RoundClosingEvent;
import edu.cuny.cat.event.RoundOpenedEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.cat.event.ShoutPostedEvent;
import edu.cuny.cat.event.ShoutReceivedEvent;
import edu.cuny.cat.event.ShoutRejectedEvent;
import edu.cuny.cat.event.SimulationOverEvent;
import edu.cuny.cat.event.SimulationStartedEvent;
import edu.cuny.cat.event.SpecialistCheckInEvent;
import edu.cuny.cat.event.SubscriptionEvent;
import edu.cuny.cat.event.TraderCheckInEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.event.TransactionPostedEvent;
import edu.cuny.cat.event.TraderProfitEvent;
import edu.cuny.cat.registry.Registry;
import edu.cuny.cat.registry.SocialNetworkRegistry;
import edu.cuny.cat.task.EventDispatchingTaskOnServerSide;
import edu.cuny.cat.task.IncomingMessageDispatchingTask;
import edu.cuny.cat.task.OutgoingMessageDispatchingTask;
import edu.cuny.cat.valuation.ValuationPolicy;
import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.util.Galaxy;

import uk.ac.liv.cat.socialnetwork.util.PlaySound;

/**
 * Each instance of this class is created by {@link ConnectionManager} on behalf
 * of a game server to deal with requests and responses from/to a game client.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.145 $
 */
public final class ConnectionAdaptor implements AuctionEventListener,
		ConnectionListener<CatpMessage>, MessageHandler<CatpMessage>, Observer {

	protected static Logger logger = Logger.getLogger(ConnectionAdaptor.class);

	protected EventEngine eventEngine;

	protected ConnectionManager manager;

	protected GameController controller;

	protected ReactiveConnection<CatpMessage> connection;

	protected Registry registry;

	protected GameClock clock;

	protected ShoutValidator shoutValidator;

	protected TransactionValidator transactionValidator;

	protected ChargeValidator chargeValidator;

	protected TimeoutController timeController;

	protected Buffer<CatpProactiveSession> proactiveSessions;

	protected CatpReactiveSession reactiveSessions[];

	/*
	 * stores pending sessions for shouts, registration and subscription requests
	 * key is shout id, or REGISTER/SUBSCRIBE + specialist.id.
	 * 
	 * TODO: currently only shouts involve pending sessions.
	 */
	protected Map<String, ShoutFromTraderSession> pendingRequestSessions;

	protected AccountHolder client;

	protected ValuationPolicy valuer;

	protected int state;

	public static String tag = "";

	public ConnectionAdaptor(final ConnectionManager manager,
			final Connection<CatpMessage> conn) {

		eventEngine = Galaxy.getInstance().getDefaultTyped(EventEngine.class);

		this.manager = manager;

		controller = GameController.getInstance();

		clock = controller.getClock();
		shoutValidator = controller.getShoutValidator();
		transactionValidator = controller.getTransactionValidator();
		chargeValidator = controller.getChargeValidator();
		registry = controller.getRegistry();
		timeController = controller.getTimeController();

		proactiveSessions = BufferUtils
				.synchronizedBuffer(new UnboundedFifoBuffer<CatpProactiveSession>());
		pendingRequestSessions = Collections
				.synchronizedMap(new HashMap<String, ShoutFromTraderSession>());

		connection = ListenableConnection.makeReactiveConnection(conn);

		setExpectedReactiveSessions(new CatpReactiveSession[] {
				new CheckInSession(), new OracleSession("BeforeCheckIn") });

		openConnection();

		setState(ClientState.READY, ClientState.getCodeDesc(ClientState.READY)
				+ " for checking in");
	}

	private void openConnection() {
		connection.setListener(this);

		try {
			connection.open();
		} catch (final ConnectionException e) {
			e.printStackTrace();
			ConnectionAdaptor.logger.fatal(e.toString(), e);
			setState(ClientState.FATAL, ClientState.getCodeDesc(ClientState.FATAL)
					+ " in openning connection");
			return;
		}
	}

	public AccountHolder getClient() {
		return client;
	}

	public boolean isTrader() {
		return (client instanceof Trader);
	}

	public boolean isSeller() {
		return isTrader() && ((Trader) client).isSeller();
	}

	public boolean isSpecialist() {
		return (client instanceof Specialist);
	}

	public void setValuer(final ValuationPolicy valuer) {
		this.valuer = valuer;
	}

	public ValuationPolicy getValuer() {
		return valuer;
	}

	protected void processGameStarting(final GameStartingEvent event) {

		/*
		 * NOTE: any existing pending proactive session may indicate some
		 * improvement might be needed.
		 */
		clearPendingProactiveSessions();

		setExpectedReactiveSessions(new CatpReactiveSession[] { new OracleSession(
				"GameStarting") });

		final CatpRequest request = CatpRequest.createRequest(CatpMessage.OPTIONS,
				new String[] {
						CatpMessage.TYPE,
						CatpMessage.GAMESTARTING,
						CatpMessage.VALUE,
						CatpMessage.concatenate(new int[] { clock.getDayLen(),
								clock.getRoundLen() }) });
		request.setTrigger(event);

		request.setTag(ConnectionAdaptor.tag);

		final TimableCatpProactiveSession session = new GameStartingSession(request);
		startProactiveSession(session);
	}

	protected void processAvailableTradersAnnounced(
			final AvailableTradersAnnouncedEvent event) {

		final CatpRequest request = CatpRequest.createRequest(CatpMessage.POST,
				new String[] { CatpMessage.TYPE, CatpMessage.TRADER, CatpMessage.ID,
				// send list of all traders including failed one in case some that
						// failed after checkin may reconnect.
						// CatpMessage.concatenate(registry.getWorkingTraderIds()) });
						CatpMessage.concatenate(registry.getTraderIds()) });
		request.setTrigger(event);
		request.setTag(ConnectionAdaptor.tag);

		final TimableCatpProactiveSession session = new PostTraderSession(request);
		startProactiveSession(session);
	}

	protected void processAvailableMarketsAnnounced(
			final AvailableMarketsAnnouncedEvent event) {
		final CatpRequest request = CatpRequest.createRequest(CatpMessage.POST,
				new String[] { CatpMessage.TYPE, CatpMessage.SPECIALIST,
						CatpMessage.ID,
						// send list of all specialists including failed ones in case some
						// that failed after checkin may reconnect.
						// CatpMessage.concatenate(registry.getWorkingSpecialistIds()) });
						CatpMessage.concatenate(registry.getSpecialistIds()) });

		request.setTrigger(event);
		request.setTag(ConnectionAdaptor.tag);

		final PostSpecialistSession session = new PostSpecialistSession(request);
		startProactiveSession(session);

	}

	protected void processGameStarted(final GameStartedEvent event) {

		setExpectedReactiveSessions(new CatpReactiveSession[] { new OracleSession(
				"GameStarted") });

		final CatpRequest request = CatpRequest.createRequest(CatpMessage.OPTIONS,
				new String[] { CatpMessage.TYPE, CatpMessage.GAMESTARTED });
		request.setTrigger(event);

		request.setTag(ConnectionAdaptor.tag);

		final TimableCatpProactiveSession session = new GameStartedSession(request);
		startProactiveSession(session);
	}

	protected void processGameOver(final GameOverEvent event) {
		setExpectedReactiveSessions(new CatpReactiveSession[] {
				new GetTraderSession(), new GetSpecialistSession(),
				new GetFeeSession(), new GetProfitSession(),
				new OracleSession("GameOver") });

		final CatpRequest request = CatpRequest.createRequest(CatpMessage.OPTIONS,
				new String[] { CatpMessage.TYPE, CatpMessage.GAMEOVER });
		request.setTrigger(event);

		request.setTag(ConnectionAdaptor.tag);

		final TimableCatpProactiveSession session = new GameOverSession(request);
		startProactiveSession(session);
	}

	protected void processDayOpening(final DayOpeningEvent event) {

		if (isTrader()) {
			final Trader trader = (Trader) client;
			if (trader.getSpecialistId() != null) {
				ConnectionAdaptor.logger
						.error("Failed in asserting the unregistered status of trader "
								+ getClientId() + " at day " + event.getDay() + " !");
			}
		} else {
			final Specialist specialist = (Specialist) client;
			if (!specialist.getTraderMap().isEmpty()) {
				ConnectionAdaptor.logger
						.error("Failed in asserting the emptiness of registered traders at specialist: "
								+ getClientId() + " at day " + event.getDay() + " !");
			}
		}

		setExpectedReactiveSessions(new CatpReactiveSession[] {
				new GetTraderSession(), new GetSpecialistSession(),
				new GetFeeSession(), new GetProfitSession(),
				new OracleSession("DayOpening") });

		final CatpRequest request = CatpRequest.createRequest(CatpMessage.OPTIONS,
				new String[] { CatpMessage.TYPE, CatpMessage.DAYOPENING,
						CatpMessage.TIME, CatpMessage.concatenate(event.getTime()) });
		request.setTrigger(event);

		request.setTag(ConnectionAdaptor.tag);

		final TimableCatpProactiveSession session = new DayOpeningSession(request);
		startProactiveSession(session);
	}

	protected void processDayOpenedEvent(final DayOpenedEvent event) {

		// announce valid price lists from specialists
		final Specialist specialists[] = registry.getActiveSpecialists();
		FeesAnnouncedEvent faEvent = null;
		for (final Specialist specialist : specialists) {
			faEvent = new FeesAnnouncedEvent(specialist);
			faEvent.setTime(event.getTime());
			processFeesAnnounced(faEvent);
		}

		if (isTrader()) {
			if (((Trader) client).isSeller()) {
				setExpectedReactiveSessions(new CatpReactiveSession[] {
						new PostProfitSession(), new GetInformedDecisionSession(), 
						new AskFromTraderSession(), new SubscribeFromClientSession(),
						new RegisterFromTraderSession(), new GetTraderSession(),
						new GetSpecialistSession(), new GetFeeSession(),
						new GetProfitSession(), new OracleSession("DayOpened") });
			} else {
				setExpectedReactiveSessions(new CatpReactiveSession[] {
						new PostProfitSession(), new GetInformedDecisionSession(), 
						new BidFromTraderSession(), new SubscribeFromClientSession(),
						new RegisterFromTraderSession(), new GetTraderSession(),
						new GetSpecialistSession(), new GetFeeSession(),
						new GetProfitSession(), new OracleSession("DayOpened") });
			}
		} else {
			setExpectedReactiveSessions(new CatpReactiveSession[] {
					new SubscribeFromClientSession(),
					new TransactionFromSpecialistSession(), new GetTraderSession(),
					new GetSpecialistSession(), new GetFeeSession(),
					new GetProfitSession(), new OracleSession("DayOpened") });
		}

		final CatpRequest request = CatpRequest.createRequest(CatpMessage.OPTIONS,
				new String[] { CatpMessage.TYPE, CatpMessage.DAYOPENED,
						CatpMessage.TIME, CatpMessage.concatenate(event.getTime()) });
		request.setTrigger(event);

		if (isTrader()) {
			// TODO: to allow multiple different private values for multiple
			// entitlements
			final double privateValue = valuer.getValue();
			request.addHeader(CatpMessage.VALUE, String.valueOf(privateValue));
		}

		request.setTag(ConnectionAdaptor.tag);

		final TimableCatpProactiveSession session = new DayOpenedSession(request);
		startProactiveSession(session);
	}

	protected void processDayClosed(final DayClosedEvent event) {

		clearPendingRequestSessions();

		setExpectedReactiveSessions(new CatpReactiveSession[] {
				new GetTraderSession(), new GetSpecialistSession(),
				new GetFeeSession(), new GetProfitSession(),
				new OracleSession("DayClosed") });

		final String specialistIds[] = registry.getSpecialistIds();
		final double profits[] = new double[specialistIds.length];
		final int popularities[] = new int[specialistIds.length];
		for (int i = 0; i < profits.length; i++) {
			profits[i] = registry.getSpecialist(specialistIds[i]).getAccount()
					.getBalance();
			popularities[i] = registry.getSpecialist(specialistIds[i]).getTraderMap()
					.size();
		}

		final ProfitAnnouncedEvent paEvent = new ProfitAnnouncedEvent(null);
		paEvent.setTime(event.getTime());
		CatpRequest request = CatpRequest.createRequest(CatpRequest.POST,
				new String[] { CatpMessage.TYPE, CatpMessage.PROFIT, CatpMessage.ID,
						CatpMessage.concatenate(specialistIds), CatpMessage.VALUE,
						CatpMessage.concatenate(profits) });
		request.setTrigger(paEvent);

		request.setTag(ConnectionAdaptor.tag);

		TimableCatpProactiveSession session = new PostSession(request);
		startProactiveSession(session);

		request = CatpRequest.createRequest(CatpRequest.OPTIONS, new String[] {
				CatpMessage.TYPE, CatpMessage.DAYCLOSED, CatpMessage.ID,
				CatpMessage.concatenate(specialistIds), CatpMessage.VALUE,
				CatpMessage.concatenate(popularities), CatpMessage.TIME,
				CatpMessage.concatenate(event.getTime()) });
		request.setTrigger(event);

		request.setTag(ConnectionAdaptor.tag);

		session = new DayClosedSession(request);
		startProactiveSession(session);
	}

	protected void processRoundOpened(final RoundOpenedEvent event) {

		((OracleSession) reactiveSessions[reactiveSessions.length - 1])
				.setState("RoundOpened");

		final TimableCatpProactiveSession session = new RoundSession(
				CatpMessage.ROUNDOPENED, event);
		startProactiveSession(session);
	}

	protected void processRoundClosing(final RoundClosingEvent event) {

		((OracleSession) reactiveSessions[reactiveSessions.length - 1])
				.setState("RoundClosing");

		final TimableCatpProactiveSession session = new RoundSession(
				CatpMessage.ROUNDCLOSING, event);
		startProactiveSession(session);
	}

	protected void processRoundClosed(final RoundClosedEvent event) {

		((OracleSession) reactiveSessions[reactiveSessions.length - 1])
				.setState("RoundClosed");

		final TimableCatpProactiveSession session = new RoundSession(
				CatpMessage.ROUNDCLOSED, event);
		startProactiveSession(session);
	}

	protected void processRegistration(final RegistrationEvent event) {
		final CatpRequest request = CatpRequest.createRequest(CatpRequest.REGISTER,
				new String[] { CatpMessage.ID, event.getTraderId() });
		request.setTrigger(event);

		request.setTag(ConnectionAdaptor.tag);

		final TimableCatpProactiveSession session = new RegisterToSpecialistSession(
				request);
		startProactiveSession(session);
	}

	protected void processSubscription(final SubscriptionEvent event) {
		final CatpRequest request = CatpRequest.createRequest(
				CatpRequest.SUBSCRIBE, new String[] { CatpMessage.ID,
						event.getSubscriberId() });
		request.setTrigger(event);

		request.setTag(ConnectionAdaptor.tag);

		final TimableCatpProactiveSession session = new SubscribeToSpecialistSession(
				request);
		startProactiveSession(session);
	}

	protected void processFeesAnnounced(final FeesAnnouncedEvent event) {

		final CatpRequest request = CatpRequest.createRequest(CatpMessage.POST,
				new String[] { CatpMessage.TYPE, CatpMessage.FEE, CatpMessage.ID,
						event.getSpecialist().getId(), CatpMessage.VALUE,
						CatpMessage.concatenate(event.getSpecialist().getFees()) });
		event.setTime(clock.getTime());
		request.setTrigger(event);
		request.setTag(ConnectionAdaptor.tag);

		final TimableCatpProactiveSession session = new PostFeeSession(request);
		startProactiveSession(session);
	}

	/**
	 * notifies a trader that its shout is placed.
	 * 
	 * @param event
	 */
	protected void processShoutPlaced(final ShoutPlacedEvent event) {
		final Shout shout = event.getShout();

		if (!shout.getTrader().getId().equals(getClientId())) {
			ConnectionAdaptor.logger
					.fatal("Bug: placed shout has incorrect trader information !");
			return;
		}
		// successful shout made by myself
		// trader notified of the success of shout attempting to place

		if (Shout.TRACE) {
			ConnectionAdaptor.logger.info("AtS+*: " + shout);
		}

		final ShoutFromTraderSession session = pendingRequestSessions.get(shout
				.getId());

		if (session != null) {
			pendingRequestSessions.remove(shout.getId());

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK,
					new String[] { CatpMessage.ID, shout.getId(), CatpMessage.TIME,
							CatpMessage.concatenate(event.getTime()), CatpMessage.TYPE,
							CatpMessage.SHOUT });

			response.setTag(ConnectionAdaptor.tag);
			dispatchOutgoingMessage(response, session);
		} else {
			ConnectionAdaptor.logger
					.error("A pending ShoutFromTraderSession should exist to process placing !");
		}
	}

	/**
	 * This notifies a subscriber of a placed shout.
	 * 
	 * @param event
	 */
	protected void processShoutPosted(final ShoutPostedEvent event) {

		if (Shout.TRACE) {
			ConnectionAdaptor.logger.info("ASp*: " + event.getShout());
		}

		final CatpRequest request = CatpRequest.createRequest(CatpMessage.POST,
				new String[] {
						CatpMessage.TYPE,
						event.getShout().isAsk() ? CatpMessage.ASK : CatpMessage.BID,
						CatpMessage.ID,
						CatpMessage.concatenate(new String[] { event.getShout().getId(),
								event.getShout().getTrader().getId(),
								event.getShout().getSpecialist().getId() }), CatpMessage.VALUE,
						String.valueOf(event.getShout().getPrice()), CatpMessage.TIME,
						CatpMessage.concatenate(event.getTime()) });
		request.setTrigger(event);

		request.setTag(event.getDay());

		final TimableCatpProactiveSession session = new PostSession(request);
		startProactiveSession(session);
	}

	/**
	 * NOTE: this method is used to let a specialist know a shout is received
	 * towards it.
	 */
	protected void processShoutReceived(final ShoutReceivedEvent event) {

		if (isSpecialist()) {
			// notify specialist of this shout received and to be accepted or rejected

			if (Shout.TRACE) {
				ConnectionAdaptor.logger.info("AmSr*: " + event.getShout());
			}

			final CatpRequest request = CatpRequest.createRequest(event.getShout()
					.isAsk() ? CatpMessage.ASK : CatpMessage.BID, new String[] {
					CatpMessage.ID, event.getShout().getId(), CatpMessage.VALUE,
					String.valueOf(event.getShout().getPrice()), CatpMessage.TIME,
					CatpMessage.concatenate(event.getTime()) });
			request.setTrigger(event);

			request.setTag(ConnectionAdaptor.tag);

			final TimableCatpProactiveSession session = new ShoutForwardSession(
					request, event.getShout().getId());
			startProactiveSession(session);
		}
	}

	protected void processShoutRejected(final ShoutRejectedEvent event) {
		// notify the trader of shout rejected

		final Shout shout = event.getShout();

		if (shout.getState() != Shout.REJECTED) {
			ConnectionAdaptor.logger.fatal(
					"A shout rejected should be in state REJECTED !", new Exception());
			ConnectionAdaptor.logger.fatal("shout: " + shout);
			return;
		}

		if (Shout.TRACE) {
			ConnectionAdaptor.logger.info("AtSx*: " + shout);
		}

		final ShoutFromTraderSession session = pendingRequestSessions.get(shout
				.getId());

		if (session != null) {
			pendingRequestSessions.remove(shout.getId());

			final CatpResponse response = CatpResponse.createResponse(
					CatpMessage.INVALID, new String[] { CatpMessage.TYPE,
							CatpMessage.SPECIALIST, CatpMessage.TIME,
							CatpMessage.concatenate(event.getTime()) });
			response.setTag(ConnectionAdaptor.tag);
			dispatchOutgoingMessage(response, session);
		} else {
			ConnectionAdaptor.logger
					.fatal("A pending ShoutFromTraderSession should exist to process rejection !");
		}

	}

	/**
	 * notifieds traders that their shouts are matched in the transaction.
	 * 
	 * @param event
	 */
	protected void processTransactionExecuted(final TransactionExecutedEvent event) {
		// 

		// TODO: the information included in the message may be less, e.g., removing
		// the specialist info
		final CatpRequest request = CatpRequest.createRequest(
				CatpMessage.TRANSACTION, new String[] {
						CatpMessage.ID,
						CatpMessage.concatenate(new String[] {
								event.getTransaction().getId(),
								event.getTransaction().getAsk().getId(),
								event.getTransaction().getBid().getId(),
								event.getTransaction().getSpecialist().getId() }),
						CatpMessage.VALUE,
						CatpMessage.concatenate(new double[] {
								event.getTransaction().getPrice(),
								event.getTransaction().getAsk().getPrice(),
								event.getTransaction().getBid().getPrice() }),
						CatpMessage.TIME, CatpMessage.concatenate(event.getTime()) });
		request.setTag(ConnectionAdaptor.tag);

		final TimableCatpProactiveSession session = new TransactionToTraderSession(
				request);
		startProactiveSession(session);
	}

	/**
	 * notifies subscribers of transactions.
	 * 
	 * @param event
	 */
	protected void processTransactionPosted(final TransactionPostedEvent event) {
		// 

		final CatpRequest request = CatpRequest.createRequest(CatpMessage.POST,
				new String[] {
						CatpMessage.TYPE,
						CatpMessage.TRANSACTION,
						CatpMessage.ID,
						CatpMessage.concatenate(new String[] {
								event.getTransaction().getId(),
								event.getTransaction().getAsk().getId(),
								event.getTransaction().getBid().getId(),
								event.getTransaction().getSpecialist().getId() }),
						CatpMessage.VALUE,
						CatpMessage.concatenate(new double[] {
								event.getTransaction().getPrice(),
								event.getTransaction().getAsk().getPrice(),
								event.getTransaction().getBid().getPrice() }),
						CatpMessage.TIME, CatpMessage.concatenate(event.getTime()) });
		request.setTag(ConnectionAdaptor.tag);

		final TimableCatpProactiveSession session = new PostSession(request);
		startProactiveSession(session);
	}

	protected void processSimulationStarted(final SimulationStartedEvent event) {
		// do nothing
	}

	protected void processSimulationOver(final SimulationOverEvent event) {
		terminate();
		setState(ClientState.CONN_CLOSED, ClientState
				.getCodeDesc(ClientState.CONN_CLOSED));
	}

	private void terminate() {
		try {
			clearPendingProactiveSessions();
			clearPendingRequestSessions();
			connection.close();
		} catch (final ConnectionException e) {
			e.printStackTrace();
			ConnectionAdaptor.logger.fatal("Failed to close the connection !", e);
		}
	}

	/**
	 * receives notification of control messages from GameView, or messages from
	 * somewhere else, i.e. other adaptors.
	 * 
	 */

	public synchronized void eventOccurred(final AuctionEvent event) {

		// debug("eventOccurred <" + getClientId());

		if ((state == ClientState.FATAL) || (state == ClientState.CONN_CLOSED)) {
			ConnectionAdaptor.logger
					.info("Connection adaptor for " + getClientId()
							+ " is down, so disregard " + event.getClass().getSimpleName()
							+ " !");
			// debug("eventOccurred >1" + getClientId());
			return;
		}

		// always notify valuer first so that valuer gets updated first
		if (valuer != null) {
			valuer.eventOccurred(event);
		}

		if (event instanceof GameStartingEvent) {
			processGameStarting((GameStartingEvent) event);
		} else if (event instanceof GameStartedEvent) {
			processGameStarted((GameStartedEvent) event);
		} else if (event instanceof GameOverEvent) {
			processGameOver((GameOverEvent) event);
		} else if (event instanceof DayOpeningEvent) {
			processDayOpening((DayOpeningEvent) event);
		} else if (event instanceof DayOpenedEvent) {
			processDayOpenedEvent((DayOpenedEvent) event);
		} else if (event instanceof DayClosedEvent) {
			processDayClosed((DayClosedEvent) event);
		} else if (event instanceof RoundOpenedEvent) {
			processRoundOpened((RoundOpenedEvent) event);
		} else if (event instanceof RoundClosingEvent) {
			processRoundClosing((RoundClosingEvent) event);
		} else if (event instanceof RoundClosedEvent) {
			processRoundClosed((RoundClosedEvent) event);
		} else if (event instanceof RegistrationEvent) {
			processRegistration((RegistrationEvent) event);
		} else if (event instanceof SubscriptionEvent) {
			processSubscription((SubscriptionEvent) event);
		} else if (event instanceof FeesAnnouncedEvent) {
			processFeesAnnounced((FeesAnnouncedEvent) event);
		} else if (event instanceof ShoutPlacedEvent) {
			processShoutPlaced((ShoutPlacedEvent) event);
		} else if (event instanceof ShoutReceivedEvent) {
			processShoutReceived((ShoutReceivedEvent) event);
		} else if (event instanceof ShoutRejectedEvent) {
			processShoutRejected((ShoutRejectedEvent) event);
		} else if (event instanceof ShoutPostedEvent) {
			processShoutPosted((ShoutPostedEvent) event);
		} else if (event instanceof TransactionExecutedEvent) {
			processTransactionExecuted((TransactionExecutedEvent) event);
		} else if (event instanceof TransactionPostedEvent) {
			processTransactionPosted((TransactionPostedEvent) event);
		} else if (event instanceof SimulationStartedEvent) {
			processSimulationStarted((SimulationStartedEvent) event);
		} else if (event instanceof SimulationOverEvent) {
			processSimulationOver((SimulationOverEvent) event);
		} else if (event instanceof AvailableTradersAnnouncedEvent) {
			processAvailableTradersAnnounced((AvailableTradersAnnouncedEvent) event);
		} else if (event instanceof AvailableMarketsAnnouncedEvent) {
			processAvailableMarketsAnnounced((AvailableMarketsAnnouncedEvent) event);
			// } else if (event instanceof TraderCheckInEvent) {
			// // do nothing
			// } else if (event instanceof SpecialistCheckInEvent) {
			// // do nothing
			// } else if (event instanceof ClientStateUpdatedEvent) {
			// // do nothing
		} else {
			ConnectionAdaptor.logger.fatal("Invalid event to client : "
					+ event.getClass().getSimpleName());
			setState(ClientState.FATAL, event);
		}

		// debug("eventOccurred >2" + getClientId());
	}

	private void startProactiveSession(final TimableCatpProactiveSession session) {
		proactiveSessions.add(session);
		try {
			final TimeoutTask timeoutAction = timeController.monitor(this, session);
			session.setTimeoutAction(timeoutAction);
			session.sendRequest();
		} catch (final CatException e) {
			proactiveSessions.remove(session);
			session.forceOut();
			ConnectionAdaptor.logger.error("Failed to send request in " + session
					+ ": \n" + session.getRequest());
		}
	}

	private void setExpectedReactiveSessions(final CatpReactiveSession sessions[]) {
		reactiveSessions = sessions;
	}

	private void dectedAndRunReactiveSessions(final CatpRequest request,
			final CatpReactiveSession sessions[]) {

		CatpResponse response = null;
		CatpReactiveSession session = null;

		for (final CatpReactiveSession session2 : sessions) {
			try {
				session = (CatpReactiveSession) session2.clone();
				session.processRequest(request);
				if (!session.isProcessed()) {
					ConnectionAdaptor.logger.fatal("Bug: " + session + " in "
							+ getClass().getSimpleName()
							+ " processed request but didn't mark _processed_:\n" + request
							+ "\n");
				}
				return;
			} catch (final CatpMessageException e) {
				if (session.isProcessed()) {
					final String s = "Failed in processing request from " + getClientId()
							+ " in " + session + ".";
					String responseType = null;
					if (e instanceof CatpMessageInvalidException) {
						responseType = CatpMessage.INVALID;
					} else if (e instanceof CatpMessageErrorException) {
						responseType = CatpMessage.ERROR;
					}

					if (responseType != null) {
						response = CatpResponse.createResponse(responseType, new String[] {
								CatpMessage.TEXT, s + " Error:" + e.toString() });

						// use tag only when Tag is present in request.
						if (request.getTag() != null) {
							response.setTag(request.getTag());
						}

						// only send response if connection is still alive
						// TODO: to check if the checking is sufficient.
						if (!connection.isClosed()) {
							dispatchOutgoingMessage(response, session);
						}
					}

					ConnectionAdaptor.logger.error(s + " Request:\n" + request, e);

					return;
				} else {
					// do nothing

					// continue to try to process the request with the next
					// possible reactive session.
				}
			} catch (final CatException e) {
				ConnectionAdaptor.logger.fatal(e);
			} catch (final RuntimeException e) {
				e.printStackTrace();
				ConnectionAdaptor.logger.fatal(e);
			}
		}

		setState(ClientState.FATAL, ClientState.getCodeDesc(ClientState.FATAL)
				+ " in locating proper reactive session !");
	}

	public void messageArrived(final CatpMessage msg) {
		dispatchIncomingMessage(msg);
	}

	public synchronized void handleMessage(final CatpMessage msg) {
		// debug("messageReceived <" + getClientId());

		CatpResponse response = null;
		CatpRequest request = null;

		if (msg == null) {
			// message is null, indicating connection closed
			if (state != ClientState.FATAL) {
				setState(ClientState.FATAL, ClientState.getCodeDesc(ClientState.FATAL)
						+ " in message from client");
			}
		} else {
			if (state != ClientState.FATAL) {
				if ((ConnectionAdaptor.tag != null)
						&& (ConnectionAdaptor.tag.length() != 0)
						&& !ConnectionAdaptor.tag.equals(msg.getTag()) && (client != null)) {
					// simply disregard the message, but don't check when the connection
					// is just opened
					ConnectionAdaptor.logger.info("Message with wrong tag received from "
							+ client.getId() + " (right tag = " + ConnectionAdaptor.tag
							+ ") :\n" + msg);

					// debug("messageReceived >1" + getClientId());
					return;
				}
			}

			if (msg instanceof CatpResponse) {
				response = (CatpResponse) msg;

				if (proactiveSessions.isEmpty()) {
					if (state != ClientState.FATAL) {
						ConnectionAdaptor.logger.fatal("Unexpected response received from "
								+ client.getId() + ":\n" + response);
						setState(ClientState.ERROR, ClientState
								.getCodeDesc(ClientState.ERROR)
								+ " with unexpected message");
						// debug("messageReceived >2" + getClientId());
						return;
					}
				} else {
					final TimableCatpProactiveSession session = (TimableCatpProactiveSession) proactiveSessions
							.remove();
					try {
						session.processResponse(response);
					} catch (final CatpMessageException e) {
						if (state != ClientState.FATAL) {
							ConnectionAdaptor.logger.error("Failed in "
									+ session.getClass().getSimpleName()
									+ " with the response from " + client.getId() + " : \n"
									+ response, e);
							// state has been set properly, so no need to do so here
						}
					} catch (final CatException e) {
						if (state != ClientState.FATAL) {
							ConnectionAdaptor.logger.fatal(e);
							setState(ClientState.FATAL, ClientState
									.getCodeDesc(ClientState.FATAL)
									+ " in on-going " + session);
						}
					}
				}
			} else {
				request = (CatpRequest) msg;

				dectedAndRunReactiveSessions(request, reactiveSessions);
			}
		}

		// debug("messageReceived >3" + getClientId());

	}

	protected void dispatchEvent(final AuctionEvent event,
			final Collection<String> receiverIdColl) {
		manager.dispatchEvent(event, receiverIdColl, this);
	}

	protected void dispatchOutgoingMessage(CatpMessage msg,
			Session<CatpMessage> session) {
		// send outgoing messagen directly, instead of dispatching at the connection
		// manager
		// 
		try {
			session.sendMessage(msg);
		} catch (final CatException e) {
			ConnectionAdaptor.logger
					.error("Exception occurred during sending message to "
							+ getClientId());
			ConnectionAdaptor.logger.error(msg);
			e.printStackTrace();
		}
		// manager.dispatchOutgoingMessage(message, session, getClientId(), this);
	}

	protected void dispatchIncomingMessage(CatpMessage msg) {
		manager.dispatchIncomingMessage(msg, this, getClientId(), this);
	}

	/**
	 * processes timeout event from TimeController on sessions.
	 * 
	 */
	public synchronized void timeout(final TimableCatpProactiveSession session) {

		// debug("timeout <" + getClientId());

		// TODO: whether or not to make the situation worse for the following
		// exceptions?

		if (session.isCompleted()) {
			/*
			 * There are at least two cases that a timeout session turns out to have
			 * completed:
			 * 
			 * Case 1: A session receives a response after it times out but before
			 * this timeout() method is invoked.
			 * 
			 * Case 2: A session times out but got cleared out of pending proactive
			 * sessions due to a previous timeout error before this timeout() method
			 * is invoked.
			 * 
			 * On both cases, isCompleted() is true.
			 */
			ConnectionAdaptor.logger.warn(session
					+ " times out, but managed to either"
					+ " receive the response in the last minute or"
					+ " get forced out in processing an earlier error !");
		} else {
			if (!proactiveSessions.contains(session)) {
				ConnectionAdaptor.logger.error(session + " with " + getClientId()
						+ " timed out, but not found in pending proactive session list !");

			} else {
				final TimableCatpProactiveSession pendingSession = (TimableCatpProactiveSession) proactiveSessions
						.get();

				/*
				 * NOTE that it's possible that sessions scheduled later may timeout
				 * before those scheduled earlier. When this happens, the timeout
				 * session exists in proactiveSessions but not the one at the head.
				 */

				if (session == pendingSession) {
					ConnectionAdaptor.logger.error("Timeout during " + session
							+ " with client " + client.getId() + " !");
				} else {
					ConnectionAdaptor.logger
							.error("Timeout during "
									+ session
									+ " with client "
									+ client.getId()
									+ " and the timeout occurred before sessions scheduled earlier timeout !\n");
				}

				proactiveSessions.remove(session);
				session.forceOut();
			}
		}

		// debug("timeout >" + getClientId());

	}

	private void setState(final int state, final String desc) {
		// no triggering event info
		setState(state, null, desc);
	}

	private void setState(final int newState, final AuctionEvent triggeringEvent) {
		// no description info
		setState(newState, triggeringEvent, null);
	}

	private void setState(final int newState, final AuctionEvent triggeringEvent,
			final String desc) {

		// disregard any state update after this adaptor is down.
		if (state == ClientState.CONN_CLOSED) {
			return;
		}

		final int oldState = state;
		final int nextState = calculateState(newState);

		if ((nextState == ClientState.ERROR) || (nextState == ClientState.FATAL)) {
			failed = true;
		} else if (nextState == ClientState.OK) {
			failed = false;
		}

		// if new error or recovered from error, display the change of state.
		final String msg = "State of connection to " + getClientId() + ": "
				+ ClientState.getCodeDesc(oldState) + " -> "
				+ ClientState.getCodeDesc(nextState);
		if ((newState == ClientState.FATAL) || (newState == ClientState.ERROR)) {
			if (newState == ClientState.FATAL) {
				ConnectionAdaptor.logger.fatal(msg);
			} else if (newState == ClientState.ERROR) {
				ConnectionAdaptor.logger.error(msg);
			}

			new Exception(ClientState.getCodeDesc(newState) + ": " + desc)
					.printStackTrace();
		} else if ((newState == ClientState.OK) && (oldState == ClientState.ERROR)) {
			ConnectionAdaptor.logger.info(msg);
		}

		synchronized (CatpMessage.ID) {
			// as in CheckInSession, client is created first before notfiying manager
			// to update adaptors, without synchronization, the event may be fired to
			// manager while the adaptor is not identified.
			if (client != null) {
				final ClientStateUpdatedEvent event = new ClientStateUpdatedEvent(
						client, new ClientState(oldState),
						new ClientState(nextState, desc), triggeringEvent);
				event.setTime(clock.getTime());

				state = nextState;

				controller.processEventInsideServer(event);
			} else {
				state = nextState;
			}
		}

		// do cleaning up only at the first time of changing state to FATAL or
		// CONN_CLOSED
		if ((newState == ClientState.FATAL) && (oldState != ClientState.FATAL)) {
			setExpectedReactiveSessions(new CatpReactiveSession[] { new OracleSession(
					ClientState.getCodeDesc(state)) });

			// a specialist aims to clear the pending ShoutForwardSessions now instead
			// of at the end of the day so as for the trader clients to be notified
			// asap.
			if (isSpecialist()) {
				clearPendingProactiveSessions();
			} else {
				clearPendingRequestSessions();
			}

			// setting the state to CONN_CLOSED would cause the adaptor to be removed
			// completely
			setState(ClientState.CONN_CLOSED, triggeringEvent,
					"Connection closed due to fatal errors !");

		} else if (newState == ClientState.CONN_CLOSED) {
			terminate();
			setExpectedReactiveSessions(new CatpReactiveSession[] { new OracleSession(
					ClientState.getCodeDesc(state)) });
		}
	}

	protected int calculateState(final int newState) {
		if ((state == ClientState.FATAL) && (newState != ClientState.CONN_CLOSED)) {
			// do not downgrade the level of error
			return state;
		} else if (state == ClientState.CONN_CLOSED) {
			// do not change state after connection is closed.
			return state;
		} else {
			return newState;
		}
	}

	public int getState() {
		return state;
	}

	// ////////////////////////////////////////////////////////////
	// proactive sessions
	// ////////////////////////////////////////////////////////////

	/**
	 * processes Round OPENED/CLOSING/CLOSED messages that doesn't need special
	 * attention to the response
	 */
	class RoundSession extends TimableCatpProactiveSession {

		String type;

		public RoundSession(final String type, final AuctionEvent triggeringEvent) {
			super(connection, CatpRequest.createRequest(CatpMessage.OPTIONS,
					new String[] { CatpMessage.TYPE, type, CatpMessage.TIME,
							CatpMessage.concatenate(triggeringEvent.getTime()) }));
			request.setTag(ConnectionAdaptor.tag);
			request.setTrigger(triggeringEvent);

			this.type = type;
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			eventEngine.dispatchEvent(GameClock.class, new Event(this, type));

			if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)) {
				setState(ClientState.OK, (AuctionEvent) request.getTrigger());
			} else {
				setState(ClientState.ERROR, (AuctionEvent) request.getTrigger());
				throw new CatpMessageErrorException("Unexpected "
						+ response.getStatusCode() + " response received from "
						+ getClientId() + " !");
			}
		}

		public void timeout() {
			setState(ClientState.FATAL, (AuctionEvent) request.getTrigger(),
					"Timeout in " + this);
		}

		public String toString() {
			return "RoundSession " + request.getHeader(CatpMessage.TYPE);
		}

	}

	class GameStartingSession extends TimableCatpProactiveSession {

		public GameStartingSession(final CatpRequest request) {
			super(connection, request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			eventEngine.dispatchEvent(GameClock.class, new Event(this,
					CatpMessage.GAMESTARTING));

			if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)) {
				setState(ClientState.OK, (AuctionEvent) request.getTrigger());
			} else {
				setState(ClientState.ERROR, (AuctionEvent) request.getTrigger());

				throw new CatpMessageErrorException("Unexpected "
						+ response.getStatusCode()
						+ " response received to options gamestarting message!");
			}
		}

		public void timeout() {
			setState(ClientState.FATAL, (AuctionEvent) request.getTrigger(),
					"Timeout in " + this);
		}

		public String toString() {
			return "GameStartingSession";
		}
	}

	class GameStartedSession extends TimableCatpProactiveSession {

		public GameStartedSession(final CatpRequest request) {
			super(connection, request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			eventEngine.dispatchEvent(GameClock.class, new Event(this,
					CatpMessage.GAMESTARTED));

			if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)) {
				setState(ClientState.OK, (AuctionEvent) request.getTrigger());
			} else {
				setState(ClientState.ERROR, (AuctionEvent) request.getTrigger());
				throw new CatpMessageErrorException("Unexpected "
						+ response.getStatusCode()
						+ " response received to options gamestarted message!");
			}
		}

		public void timeout() {
			setState(ClientState.FATAL, (AuctionEvent) request.getTrigger(),
					"Timeout in " + this);
		}

		public String toString() {
			return "GameStartedSession";
		}
	}

	class GameOverSession extends TimableCatpProactiveSession {

		public GameOverSession(final CatpRequest request) {
			super(connection, request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			eventEngine.dispatchEvent(GameClock.class, new Event(this,
					CatpMessage.GAMEOVER));

			if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)) {
				setState(ClientState.OK, (AuctionEvent) request.getTrigger());
				setState(ClientState.READY, (AuctionEvent) request.getTrigger());
			} else {
				setState(ClientState.ERROR, (AuctionEvent) request.getTrigger());
				throw new CatpMessageErrorException("Unexpected "
						+ response.getStatusCode()
						+ " response received to options gameover message!");
			}
		}

		public void timeout() {
			setState(ClientState.FATAL, (AuctionEvent) request.getTrigger(),
					"Timeout in " + this);
		}

		public String toString() {
			return "GameOverSession";
		}
	}

	/**
	 * notifies specialists of day opened.
	 * 
	 */
	class DayOpeningSession extends TimableCatpProactiveSession {

		public DayOpeningSession(final CatpRequest request) {
			super(connection, request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			eventEngine.dispatchEvent(GameClock.class, new Event(this,
					CatpMessage.DAYOPENING));

			if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)) {

				if (isSpecialist()) {
					// for specialists, check for price list
					if (CatpMessage.FEE.equalsIgnoreCase(response
							.getHeader(CatpMessage.TYPE))) {
						final String priceList = response.getHeader(CatpMessage.VALUE);
						if ((priceList == null) || (priceList.length() == 0)) {
							setState(ClientState.ERROR, (AuctionEvent) request.getTrigger());
							eventEngine.dispatchEvent(GameClock.class, new Event(this,
									CatpMessage.FEE));
							throw new CatpMessageErrorException("Empty price list !");
						}

						final double fees[] = CatpMessage.parseDoubles(priceList);
						try {
							chargeValidator.check(getClientId(), fees);
						} catch (final InvalidChargeException e) {
							// e.printStackTrace();
							setState(ClientState.ERROR, (AuctionEvent) request.getTrigger());
							eventEngine.dispatchEvent(GameClock.class, new Event(this,
									CatpMessage.FEE));
							new PlaySound("ohno.wav").start();
							throw new CatpMessageInvalidException();
						}

						// ban the specialist for the current game day if there is a pending
						// day banning penalty
						// just ban the FeesAnnouncedEvent
						final ClientBehaviorController behaviorController = controller
								.getBehaviorController();
						if (behaviorController.getPenalty(getClientId(),
								ClientBehaviorController.DAY_BANNING_PENALTY) > 0) {
							behaviorController.penaltyExecuted(getClientId(),
									ClientBehaviorController.DAY_BANNING_PENALTY);
							ConnectionAdaptor.logger.info("Penalty "
									+ behaviorController.getPenalty(getClientId(),
											ClientBehaviorController.DAY_BANNING_PENALTY)
									+ " pending on " + getClientId() + ".\n");
							eventEngine.dispatchEvent(GameClock.class, new Event(this,
									CatpMessage.FEE));
						} else {

							final Specialist specialist = (Specialist) client;
							specialist.setFees(fees);
							final FeesAnnouncedEvent event = new FeesAnnouncedEvent(
									specialist);
							event.setTime(clock.getTime());
							controller.processEventInsideServer(event);
							eventEngine.dispatchEvent(GameClock.class, new Event(this,
									CatpMessage.FEE));
						}
					} else {
						setState(ClientState.ERROR, (AuctionEvent) request.getTrigger());
						throw new CatpMessageErrorException(
								"Header FEE expected instead of "
										+ response.getHeader(CatpMessage.TYPE)
										+ " in the response to options dayopening request !");
					}
				} else {
					// for trader, check number of entitlements
					final int entitlement = response.getIntHeader(CatpMessage.VALUE);
					if (entitlement < 0) {
						ConnectionAdaptor.logger
								.warn("Illegal negative initial entitlement of traders !");
					}

					final Trader trader = (Trader) client;
					trader.setEntitlement(entitlement);
				}
				setState(ClientState.OK, (AuctionEvent) request.getTrigger());
			} else {
				setState(ClientState.FATAL, (AuctionEvent) request.getTrigger());
				throw new CatpMessageErrorException("Unexpected "
						+ response.getStatusCode()
						+ " response received to options dayopening request !");
			}
		}

		public void timeout() {
			setState(ClientState.FATAL, (AuctionEvent) request.getTrigger(),
					"Timeout in " + this);
		}

		public String toString() {
			return "DayOpeningSession";
		}
	}

	class DayOpenedSession extends TimableCatpProactiveSession {

		public DayOpenedSession(final CatpRequest request) {
			super(connection, request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			eventEngine.dispatchEvent(GameClock.class, new Event(this,
					CatpMessage.DAYOPENED));

			if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)) {
				if (isTrader()) {
					final double privateValue = request
							.getDoubleHeader(CatpMessage.VALUE);

					// TODO: did not consider private values for multiple items
					// this may distort the supply and demand curves if not all traders
					// have the same entitlement every day.
					final PrivateValueAssignedEvent event = new PrivateValueAssignedEvent(
							getClientId(), privateValue);
					event.setTime(clock.getTime());
					controller.processEventInsideServer(event);
				}

				setState(ClientState.OK, (AuctionEvent) request.getTrigger());

			} else {
				setState(ClientState.FATAL, (AuctionEvent) request.getTrigger());

				throw new CatpMessageErrorException("Unexpected "
						+ response.getStatusCode()
						+ " response received to options dayopened request !");
			}
		}

		public void timeout() {
			setState(ClientState.FATAL, (AuctionEvent) request.getTrigger(),
					"Timeout in " + this);
		}

		public String toString() {
			return "DayOpenedSession";
		}
	}

	class DayClosedSession extends TimableCatpProactiveSession {

		public DayClosedSession(final CatpRequest request) {
			super(connection, request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			eventEngine.dispatchEvent(GameClock.class, new Event(this,
					CatpMessage.DAYCLOSED));

			if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)) {
				setState(ClientState.OK, (AuctionEvent) request.getTrigger());
			} else {
				setState(ClientState.FATAL, (AuctionEvent) request.getTrigger());

				throw new CatpMessageErrorException("Unexpected "
						+ response.getStatusCode()
						+ " response received to options dayclosed message!");
			}
		}

		public void timeout() {
			setState(ClientState.FATAL, (AuctionEvent) request.getTrigger(),
					"Timeout in " + this);
		}

		public String toString() {
			return "DayClosedSession";
		}
	}

	class PostSession extends TimableCatpProactiveSession {

		public PostSession(final CatpRequest request) {
			super(connection, request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)) {
				setState(ClientState.OK, (AuctionEvent) request.getTrigger());
			} else {
				final String msg = "Unexpected response received to post "
						+ request.getHeader(CatpMessage.TYPE) + " message:\n" + response;
				setState(ClientState.ERROR, (AuctionEvent) request.getTrigger(), msg);

				if (response.getStatusCode().equalsIgnoreCase(CatpResponse.INVALID)) {
					if (getState() != ClientState.CONN_CLOSED) {
						final String type = response.getHeader(CatpMessage.TYPE);
						if ((type != null) && type.equalsIgnoreCase(CatpMessage.WRONGTIME)) {
							/*
							 * the request arrived too late and was invalid for client to
							 * process, so just disregard the response.
							 */
						} else {
							// There may be a bug in program !

							// TODO: when a specialist times out in responding to POST Shout
							// messages, this may happen. Check this out!
							ConnectionAdaptor.logger.info("Bug: unexpected scenario in "
									+ toString());
						}
					}
				} else {
					throw new CatpMessageErrorException(msg);
				}
			}
		}

		public void timeout() {
			setState(ClientState.ERROR, (AuctionEvent) request.getTrigger(),
					"Timeout in " + this);
		}

		public String toString() {
			return "PostSession " + request.getHeader(CatpMessage.TYPE);
		}
	}

	class PostTraderSession extends PostSession {
		public PostTraderSession(final CatpRequest request) {
			super(request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			// notifies game clock
			eventEngine.dispatchEvent(GameClock.class, new Event(this,
					CatpMessage.CLIENT));

			if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)
					|| response.getStatusCode().equalsIgnoreCase(CatpResponse.INVALID)) {
				setState(
						response.getStatusCode().equalsIgnoreCase(CatpResponse.OK) ? ClientState.OK
								: ClientState.ERROR, (AuctionEvent) request.getTrigger());
			} else {
				setState(ClientState.ERROR, (AuctionEvent) request.getTrigger());
				throw new CatpMessageErrorException("Unexpected "
						+ response.getStatusCode()
						+ " response received to post trader message!");
			}

		}
	}

	class PostSpecialistSession extends PostSession {
		public PostSpecialistSession(final CatpRequest request) {
			super(request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			// notifies game clock
			eventEngine.dispatchEvent(GameClock.class, new Event(this,
					CatpMessage.CLIENT));

			if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)) {
				setState(ClientState.OK, (AuctionEvent) request.getTrigger());
			} else if (response.getStatusCode()
					.equalsIgnoreCase(CatpResponse.INVALID)) {
				setState(ClientState.ERROR, (AuctionEvent) request.getTrigger());
			} else {
				setState(ClientState.ERROR, (AuctionEvent) request.getTrigger());
				throw new CatpMessageErrorException("Unexpected "
						+ response.getStatusCode()
						+ " response received to post specialist message!");
			}
		}
	}

	class PostFeeSession extends PostSession {
		public PostFeeSession(final CatpRequest request) {
			super(request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			// notifies game clock
			eventEngine.dispatchEvent(GameClock.class, new Event(this,
					CatpMessage.FEE));
		}

		public String toString() {
			return super.toString() + "[" + request.getHeader(CatpMessage.ID) + "]";
		}
	}

	class RegisterToSpecialistSession extends TimableCatpProactiveSession {

		public RegisterToSpecialistSession(final CatpRequest request) {
			super(connection, request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			// TODO: to allow specialist to reject registration request using pending
			// session
		}

		public void timeout() {
			setState(ClientState.ERROR, (AuctionEvent) request.getTrigger(),
					"Timeout in " + this);
		}

		public String toString() {
			return "RegisterToSpecialistSession";
		}
	}

	class SubscribeToSpecialistSession extends TimableCatpProactiveSession {

		public SubscribeToSpecialistSession(final CatpRequest request) {
			super(connection, request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			// TODO: to allow specialist to reject subscription request using pending
			// sessions

		}

		public void timeout() {
			setState(ClientState.ERROR, (AuctionEvent) request.getTrigger(),
					"Timeout in " + this);
		}

		public String toString() {
			return "SubscribeToSpecialistSession";
		}
	}

	class ShoutForwardSession extends TimableCatpProactiveSession {

		String shoutId;

		public ShoutForwardSession(final CatpRequest request, final String shoutId) {
			super(connection, request);
			this.shoutId = shoutId;
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			if (isTrader()) {
				throw new CatpMessageErrorException(
						"Only specialists can possibly respond to shouts!");
			}

			final Specialist specialist = (Specialist) client;
			boolean failed = false;

			synchronized (specialist) { // shout to market

				Shout currentShout = null;
				Shout newShout = registry.getShout(shoutId);

				if (newShout == null) {
					ConnectionAdaptor.logger
							.fatal("Forwarded a non-existing shout to specialist !");
					failed = true;
				} else {
					if (newShout.getState() == Shout.PENDING) {
						// a brand new one, do nothing

						// logger.info("Forwarded new shout: " + shout);

					} else if ((newShout.getState() == Shout.PLACED)
							|| (newShout.getState() == Shout.MATCHED)) {
						// this is a shout modification request

						// NOTE: if shout.getState()==Shout.MATCHED, the specialist's
						// response must be INVALID, rejecting the modification.

						// logger.info("Forwarded modified shout: " + shout);

						if (newShout.getChild() == null) {
							ConnectionAdaptor.logger
									.fatal("Bug: either current shout shouldn't have been placed"
											+ " or its child shouldn't be null !");
							failed = true;
						} else {
							currentShout = newShout;
							newShout = newShout.getChild();
							if (newShout.getState() != Shout.PENDING) {
								ConnectionAdaptor.logger
										.fatal("Bug: shout modifying the current one should be PENDING !");
								failed = true;
							}

							if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)
									&& (currentShout.getState() != Shout.PLACED)) {
								ConnectionAdaptor.logger
										.fatal(
												"Bug: succeeded in modifying a shout that is not in state PLACED !",
												new Exception(
														"Probable bug or malicious action in specialist "
																+ getClientId() + " !"));
								ConnectionAdaptor.logger
										.fatal("current shout: " + currentShout);
								ConnectionAdaptor.logger.fatal("modification shout: "
										+ newShout);
								ConnectionAdaptor.logger.fatal("\n");
								failed = true;
							}
						}
					} else {
						ConnectionAdaptor.logger.fatal(this + ": Invalid shout state "
								+ newShout.getState());
						failed = true;
					}
					newShout.setSpecialist(specialist);
				}

				if (!failed
						&& response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)) {
					if (Shout.TRACE) {
						if (currentShout != null) {
							// shout modification
							ConnectionAdaptor.logger.info("AS-*: " + currentShout);
							ConnectionAdaptor.logger.info("AS+m: " + newShout);
						} else {
							// brand new shout
							ConnectionAdaptor.logger.info("AS+n: " + newShout);
						}
					}
					AuctionEvent event = new ShoutPlacedEvent(newShout);
					event.setTime(clock.getTime());

					// sends to registry, console, and reports
					// update the current shout
					controller.processEventInsideServer(event);

					// forward to the trader
					final HashSet<String> receiverIds = new HashSet<String>();
					receiverIds.add(newShout.getTrader().getId());
					dispatchEvent(event, receiverIds);

					// forward to all subscribers
					event = new ShoutPostedEvent(newShout);
					event.setTime(clock.getTime());

					receiverIds.clear();
					final String ids[] = registry.getSubscriberIds(client.getId());
					if (ids != null) {
						for (final String id : ids) {
							receiverIds.add(id);
						}
					}
					dispatchEvent(event, receiverIds);

					// manager.printPendingTasks();
					// logger.info("\n");

				} else {
					// shout rejected

					if (Shout.TRACE) {
						if (currentShout == null) {
							ConnectionAdaptor.logger.info("ASxn: " + newShout);
						} else {
							ConnectionAdaptor.logger.info("ASxm: " + newShout);
						}
					}

					final AuctionEvent event = new ShoutRejectedEvent(newShout);

					event.setTime(clock.getTime());

					// tell registry
					controller.processEventInsideServer(event);

					// forward to the trader only
					final HashSet<String> receiverIds = new HashSet<String>();
					receiverIds.add(newShout.getTrader().getId());
					dispatchEvent(event, receiverIds);

					if (!response.getStatusCode().equalsIgnoreCase(CatpResponse.INVALID)) {
						throw new CatpMessageErrorException("Unexpected "
								+ response.getStatusCode() + " response received !");
					}
				}
			} // synchronization on specialist
		}

		public void timeout() {
			setState(ClientState.ERROR, (AuctionEvent) request.getTrigger(),
					"Timeout in " + this);

			/*
			 * create a fake response to terminate this session nicely, so that the
			 * server will eventually notify the trader of the rejection of its shout.
			 */
			final CatpResponse response = CatpResponse
					.createResponse(CatpMessage.INVALID);
			try {
				processResponse(response);
			} catch (final CatException e) {
				ConnectionAdaptor.logger
						.error("Failed in taking a short cut for timeout during " + this
								+ " !", e);
			}
		}

		public String toString() {
			return "ShoutForwardSession[" + shoutId + "]";
		}
	}

	class TransactionToTraderSession extends TimableCatpProactiveSession {

		public TransactionToTraderSession(final CatpRequest request) {
			super(connection, request);
		}

		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			if (response.getStatusCode().equalsIgnoreCase(CatpResponse.OK)) {
				setState(ClientState.OK, (AuctionEvent) request.getTrigger());
			} else {
				setState(ClientState.ERROR, (AuctionEvent) request.getTrigger());

				throw new CatpMessageErrorException("Unexpected "
						+ response.getStatusCode()
						+ " response received from trader to transaction message!");
			}
		}

		public void timeout() {
			setState(ClientState.ERROR, (AuctionEvent) request.getTrigger(),
					"Timeout in " + this);
		}

		public String toString() {
			return "TransactionToTraderSession";
		}
	}

	// ////////////////////////////////////////////////////////////
	// reactive sessions
	// ////////////////////////////////////////////////////////////

	class CheckInSession extends CatpReactiveSession {

		public CheckInSession() {
			super(connection, CatpMessage.CHECKIN);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			String type = request.getHeader(CatpMessage.TYPE);
			if ((type == null) || (type.length() == 0)) {
				setState(ClientState.FATAL, ClientState.getCodeDesc(ClientState.FATAL)
						+ " with empty client type during CHECKIN");
				throw new CatpMessageErrorException(
						"No type of cat client specified in CHECKIN message !");
			} else {
				if (IdentityOffice.isValidClientType(type)) {

					final String desc = request.getHeader(CatpMessage.TEXT);
					final boolean isTrader = !type.toLowerCase().startsWith(
							CatpMessage.SPECIALIST.toLowerCase());

					// check for security token
					if (!controller.getSecurityManager().isAuthorizedClient(isTrader,
							type)) {
						setState(ClientState.FATAL, ClientState
								.getCodeDesc(ClientState.FATAL)
								+ " without security token during CHECKIN");
						throw new CatpMessageErrorException("Unexpected "
								+ request.getType()
								+ " request without security token received from "
								+ connection.getRemoteAddressInfo() + " !");
					} else {
						type = controller.getSecurityManager().removeToken(isTrader, type);
					}

					boolean isSeller = false;
					if (isTrader) {
						if (type.toLowerCase().startsWith(CatpMessage.SELLER.toLowerCase())) {
							isSeller = true;
						} else if (type.toLowerCase().startsWith(
								CatpMessage.BUYER.toLowerCase())) {
							isSeller = false;
						} else {
							ConnectionAdaptor.logger.error("Trader type must start with "
									+ CatpMessage.SELLER.toLowerCase() + " or "
									+ CatpMessage.BUYER.toLowerCase() + "!");
							ConnectionAdaptor.logger.error("Given trader type, " + type
									+ " cannot determine whether the trader sells or buys !");
						}
					}

					synchronized (CatpMessage.ID) {
						// avoid duplicate proposed identities passed

						// if client suggests an id, check if it is an expected specialist;
						// or if it is a failed client; or
						// otherwise view it a new client and use the suggested id if the id
						// doesn't conflict with existing client; or allocate an id based on
						// the client's type

						String clientId = request.getHeader(CatpMessage.ID);
						if (clientId != null) {
							if (registry.getExpectedSpecialist(clientId) != null) {
								// expected specialist comes in
								client = registry.getExpectedSpecialist(clientId);
								client.setDescription(desc);
							} else if (registry.getFailedClient(clientId) != null) {
								// failed client reconnects
								client = registry.getFailedClient(clientId);

								final ClientBehaviorController behaviorController = controller
										.getBehaviorController();
								if (behaviorController.getPenalty(clientId,
										ClientBehaviorController.CONNECTION_BANNING_PENALTY) > 0) {
									manager.removeBabyAdaptor(ConnectionAdaptor.this);

									// do not adjust penalty to be executed here to make the
									// client never able to check in.

									final String s = "Maximum number of reconnection allowed has been reached !";
									setState(ClientState.FATAL, s);
									throw new CatpMessageErrorException(s);
								} else {
									behaviorController.observe(clientId,
											ClientBehaviorController.RECONNECTION);
								}
							} else if ((isTrader && registry.containsTrader(clientId))
									|| (!isTrader && registry.containsSpecialist(clientId))) {
								// new client with conflicting id proposal
								clientId = null;
							} else {
								// new client with good id proposal
							}
						} else {
							// new client without id
						}

						// if new unexpected client, only proceed when game not yet started
						if ((client == null) && clock.isActive()) {
							final String s = "new client not allowed after game started (CHECKIN)";
							setState(ClientState.FATAL, s);
							throw new CatpMessageErrorException(s);
						}

						// if new client and no id proposal, allocate an id
						if ((clientId == null) || (clientId.length() == 0)) {
							clientId = manager.getIdentityOffice().createIdentity(type);
						}

						final CatpResponse response = CatpResponse.createResponse(
								CatpMessage.OK, new String[] { CatpMessage.ID, clientId });
						dispatchOutgoingMessage(response, this);

						// if new client, create client object
						if (client == null) {
							if (isTrader) {
								client = new Trader(clientId, desc, isSeller);
							} else {
								client = new Specialist(clientId, desc);
							}
						}

						AuctionEvent event = null;
						if (client instanceof Trader) {
							event = new TraderCheckInEvent((Trader) client);
						} else {
							event = new SpecialistCheckInEvent((Specialist) client);
						}

						event.setTime(clock.getTime());
						controller.processEventInsideServer(event);

						ConnectionAdaptor.logger.debug("Id assigned: " + clientId);

						manager.clientCheckIn(ConnectionAdaptor.this);

					} // end of synchronization

				} else {
					setState(ClientState.FATAL, ClientState
							.getCodeDesc(ClientState.FATAL)
							+ " with invalid client type during CHECKIN");
					throw new CatpMessageErrorException("Unexpected " + request.getType()
							+ " request received !");
				}
			}
		}

		public String toString() {
			return "CheckInSession";
		}

	}

	class RegisterFromTraderSession extends CatpReactiveSession {

		protected Shout shout;

		public RegisterFromTraderSession() {
			super(connection, CatpMessage.REGISTER);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			if (!(isTrader())) {
				// this case should be a bug
				setState(ClientState.FATAL, (AuctionEvent) request.getTrigger());
				throw new CatpMessageErrorException(reqType
						+ " request came from a non-trader: " + getClientId() + " !");
			}

			eventEngine.dispatchEvent(GameClock.class, new Event(this,
					CatpMessage.REGISTER));

			String brokerId = registry.getBrokerId(client.getId());

			CatpResponse response = null;
			if (brokerId != null) {
				response = CatpResponse.createResponse(CatpMessage.INVALID,
						new String[] { CatpMessage.TEXT,
								"Already registered with " + brokerId + "." });
				response.setTag(request.getTag());
				dispatchOutgoingMessage(response, this);
			} else {
				brokerId = request.getHeader(CatpMessage.ID);
				if ((brokerId == null) || (brokerId.length() == 0)) {
					// select not to choose any specialist

					response = CatpResponse.createResponse(CatpMessage.OK);
					response.setHeader(CatpMessage.TEXT, CatpMessage.REGISTER);
					response.setTag(request.getTag());
					dispatchOutgoingMessage(response, this);

					ConnectionAdaptor.logger.debug("no specialist selected by "
							+ client.getId());

				} else {
					// only active specialists allowed to be registered with
					final Specialist specialist = registry.getActiveSpecialist(brokerId);

					if (specialist != null) {

						// TODO: wait until the successful response from specialist to
						// acknowledge the success of the request.

						response = CatpResponse.createResponse(CatpMessage.OK);
						response.setTag(request.getTag());
						dispatchOutgoingMessage(response, this);

						final RegistrationEvent event = new RegistrationEvent(client
								.getId(), brokerId);
						event.setTime(clock.getTime());
						controller.processEventInsideServer(event);

						// forward to the specialist
						final HashSet<String> receiverIds = new HashSet<String>();
						receiverIds.add(brokerId);
						dispatchEvent(event, receiverIds);

					} else {
						throw new CatpMessageInvalidException(
								"Invalid or dead specialist ID in register request !");
					}
				}
			}
		}

		public String toString() {
			return "RegisterFromTraderSession";
		}

	}

	class SubscribeFromClientSession extends CatpReactiveSession {

		public SubscribeFromClientSession() {
			super(connection, CatpMessage.SUBSCRIBE);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String idList = request.getHeader(CatpMessage.ID);
			if ((idList == null) || (idList.length() == 0)) {
				throw new CatpMessageErrorException("Empty specialist list !");
			} else {

				final String specialistIds[] = CatpMessage.parseStrings(idList);
				boolean hasInactiveSpecialist = false;
				final String goodIds[] = new String[specialistIds.length];
				for (int i = 0; i < specialistIds.length; i++) {
					if (registry.getActiveSpecialist(specialistIds[i]) != null) {
						goodIds[i] = specialistIds[i];
					} else {
						hasInactiveSpecialist = true;
					}
				}

				// TODO: wait until the successful response from specialist to
				// acknowledge the success of requests.

				final CatpResponse response = CatpResponse.createResponse(
						CatpMessage.OK, new String[] { CatpMessage.TEXT,
								CatpMessage.SUBSCRIBE });
				if (hasInactiveSpecialist) {
					// specify those succeeded
					response.setHeader(CatpMessage.ID, CatpMessage.concatenate(goodIds));
				}
				response.setTag(request.getTag());
				dispatchOutgoingMessage(response, this);

				final int time[] = clock.getTime();
				final HashSet<String> receiverIds = new HashSet<String>();
				for (final String goodId : goodIds) {
					if (goodId != null) {
						final AuctionEvent event = new SubscriptionEvent(getClientId(),
								goodId);
						event.setTime(time);
						controller.processEventInsideServer(event);

						receiverIds.clear();
						receiverIds.add(goodId);
						dispatchEvent(event, receiverIds);
					}
				}
			}
		}

		public String toString() {
			return "SubscribeFromClientSession";
		}

	}

	class GetTraderSession extends CatpReactiveSession {
		public GetTraderSession() {
			super(connection, CatpMessage.GET, CatpMessage.TRADER);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK,
					new String[] { CatpMessage.TYPE, CatpMessage.TRADER, CatpMessage.ID,
							CatpMessage.concatenate(registry.getWorkingTraderIds()) });

			request.setTag(ConnectionAdaptor.tag);
			dispatchOutgoingMessage(response, this);
		}

		public String toString() {
			return "GetTraderSession";
		}
	}

	class GetSpecialistSession extends CatpReactiveSession {
		public GetSpecialistSession() {
			super(connection, CatpMessage.GET, CatpMessage.SPECIALIST);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK,
					new String[] { CatpMessage.TYPE, CatpMessage.SPECIALIST,
							CatpMessage.ID,
							CatpMessage.concatenate(registry.getWorkingSpecialistIds()) });

			request.setTag(ConnectionAdaptor.tag);
			dispatchOutgoingMessage(response, this);
		}

		public String toString() {
			return "GetSpecialistSession";
		}
	}

	class GetProfitSession extends CatpReactiveSession {
		public GetProfitSession() {
			super(connection, CatpMessage.GET, CatpMessage.PROFIT);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String specialistIds[] = registry.getWorkingSpecialistIds();
			final double profits[] = new double[specialistIds.length];
			for (int i = 0; i < profits.length; i++) {
				profits[i] = registry.getWorkingSpecialist(specialistIds[i])
						.getAccount().getBalance();
			}

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK,
					new String[] { CatpMessage.TYPE, CatpMessage.PROFIT, CatpMessage.ID,
							CatpMessage.concatenate(specialistIds), CatpMessage.VALUE,
							CatpMessage.concatenate(profits) });

			request.setTag(ConnectionAdaptor.tag);
			dispatchOutgoingMessage(response, this);
		}

		public String toString() {
			return "GetProfitSession";
		}
	}

	/**
	 * Newly added session class
	 * 
	 * @author guiguan
	 */
	class GetInformedDecisionSession extends CatpReactiveSession {
		public GetInformedDecisionSession() {
			super(connection, CatpMessage.GET, "MD");
		}

		/**
		 * Get Market Selection Strategy Information <code>
		 * GET CRLF
		 * Type: MD CRLF
		 * Text: strategy=MSS CRLF
		 * CRLF
		 * 
		 * OK CRLF
		 * Text: strategy=MSS, specialistid=specialist1, percentage=0.8 CRLF
		 * CRLF
		 * </code>
		 * 
		 * Get Shouting Strategy Information <code>
		 * GET CRLF
		 * Type: MD CRLF
		 * Text: strategy=SS CRLF
		 * CRLF
		 * 
		 * OK CRLF
		 * Text: strategy=SS, price=100.234234, quantity=2, percentage=0.8 CRLF
		 * CRLF
		 * </code>
		 */
		public void processRequest(final CatpRequest request)
				throws CatException {
			super.processRequest(request);

			String strategy = null;
			CatpResponse response = null;

			Pattern p = Pattern.compile("(\\w*)=(\\w*)");
			Matcher m = p.matcher(request.getHeader(CatpMessage.TEXT));

			while (m.find()) {
				if (m.group(1).equals("strategy")) {
					strategy = m.group(2);
				}
			}

			if (strategy != null) {
				if (strategy.equalsIgnoreCase("MSS")) {
					SocialNetworkRegistry snr = (SocialNetworkRegistry) registry;

					response = CatpResponse.createResponse(CatpMessage.OK,
							new String[] {
									CatpMessage.TYPE,
									"MD",
									CatpMessage.TEXT,
									String.format("strategy=MSS, %s", snr
											.getMarketSelectionStrategy()
											.queryMatrix(getClientId())) });
				} else if (strategy.equalsIgnoreCase("SS")) {
					SocialNetworkRegistry snr = (SocialNetworkRegistry) registry;

					response = CatpResponse.createResponse(CatpMessage.OK,
							new String[] {
									CatpMessage.TYPE,
									"MD",
									CatpMessage.TEXT,
									String.format("strategy=SS, %s", snr
											.getInformedShoutingMethod() //
											.queryMatrix(getClientId())) });
				}
			} else {
				response = CatpResponse.createResponse(CatpMessage.ERROR,
						new String[] { CatpMessage.TEXT,
								"Strategy must be specified!" });
			}

			request.setTag(request.getTag());
			sendMessage(response);
		}

		public String toString() {
			return "GetInformedDecisionSession";
		}
	}
	
	private int count = 0;
	/**
	 * Get Trader Profit. 
	 * 
	 * @author Martin Chapman
	 */
	class PostProfitSession extends CatpReactiveSession {
		public PostProfitSession() {
			super(connection, CatpMessage.POST, "traderProfit");
		}

		public void processRequest(final CatpRequest request)
				throws CatException {
			super.processRequest(request);

			CatpResponse response = null;
			
			final AuctionEvent event = new TraderProfitEvent(request.getHeader(CatpMessage.ID), 
															 request.getDoubleHeader(CatpMessage.VALUE), 
        									Boolean.parseBoolean(request.getHeader(CatpMessage.TEXT)));

			controller.processEventInsideServer(event);

			if (event != null) {

				SocialNetworkRegistry snr = (SocialNetworkRegistry) registry;

				response = CatpResponse.createResponse(CatpMessage.OK);
				
			} 
			else {
			
				response = CatpResponse.createResponse(CatpMessage.ERROR,
						new String[] { CatpMessage.TEXT,
								"Strategy must be specified!" });
			}

			request.setTag(request.getTag());
			sendMessage(response);
		}

		public String toString() {
			return "PostProfitSession";
		}
	}

	class GetFeeSession extends CatpReactiveSession {
		public GetFeeSession() {
			super(connection, CatpMessage.GET, CatpMessage.FEE);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String specialistId = request.getHeader(CatpMessage.ID);
			final Specialist specialist = registry.getActiveSpecialist(specialistId);
			CatpResponse response = null;
			if (specialist != null) {
				response = CatpResponse.createResponse(CatpMessage.OK, new String[] {
						CatpMessage.TYPE, CatpMessage.FEE, CatpMessage.ID, specialistId,
						CatpMessage.VALUE, CatpMessage.concatenate(specialist.getFees()) });
			} else {
				response = CatpResponse.createResponse(CatpMessage.INVALID,
						new String[] { CatpMessage.TYPE, CatpMessage.SPECIALIST,
								CatpMessage.TEXT, "Specialist doesn't exist or is inactive." });
			}

			request.setTag(ConnectionAdaptor.tag);
			dispatchOutgoingMessage(response, this);
		}

		public String toString() {
			return "GetFeeSession";
		}
	}

	/**
	 * processes new shout or modified shout from trader
	 */
	class ShoutFromTraderSession extends CatpReactiveSession {

		protected Shout shout;

		public ShoutFromTraderSession(final String reqType) {
			super(connection, reqType);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			if (!(isTrader())) {
				throw new CatpMessageInvalidException(reqType
						+ " request came from a non-trader: " + getClientId() + " !");
			}

			final Trader trader = (Trader) client;

			final String brokerId = registry.getBrokerId(client.getId());
			if (brokerId == null) {
				throw new CatpMessageInvalidException(
						"Trader must first register with a specialist before making shouts !");
			}

			final Specialist specialist = registry.getActiveSpecialist(brokerId);
			if (specialist == null) {
				throw new CatpMessageInvalidException("Specialist " + brokerId
						+ " is not active!");
			}

			synchronized (specialist) { // shout from trader

				final double price = request.getDoubleHeader(CatpMessage.VALUE);

				try {
					shoutValidator.check(trader.isSeller(), price, valuer.getValue());
				} catch (final IllegalShoutException e) {
					e.printStackTrace();
					throw new CatpMessageInvalidException(e.getMessage());
				} catch (final Exception e) {
					e.printStackTrace();
					throw new CatpMessageErrorException(e.getMessage());
				}

				CatpResponse response = null;
				String shoutId = request.getHeader(CatpMessage.ID);
				Shout currentShout = null;
				if (shoutId == null) {
					// brand new shout

					// TODO: there should be no other shouts in registry, check it!

					shoutId = manager.getIdentityOffice().createIdentity(reqType);
					shout = new Shout(shoutId, price, reqType
							.equalsIgnoreCase(CatpRequest.BID));
				} else {
					// modifying shout
					currentShout = registry.getShout(shoutId);

					if (currentShout == null) {
						throw new CatpMessageErrorException(
								"Possible bug: attempting to modify a non-existing shout !");
					} else {
						if (currentShout.getChild() != null) {
							throw new CatpMessageErrorException(
									"At most one on-going shout request is expected !");
						}

						if (currentShout.getState() == Shout.PLACED) {
							// not accepted yet, can modify
							// set the modified one as child of the current one
							shout = new Shout(shoutId, price, reqType
									.equalsIgnoreCase(CatpRequest.BID));
						} else if (currentShout.getState() == Shout.MATCHED) {
							// already accepted, reject the modification right away
							response = CatpResponse.createResponse(CatpMessage.INVALID,
									new String[] { CatpMessage.TYPE, CatpMessage.SPECIALIST,
											CatpMessage.TEXT, "Current shout accepted already !",
											CatpMessage.TIME,
											CatpMessage.concatenate(clock.getTime()) });
						} else {
							throw new CatpMessageErrorException(
									"Possible bug: shout to be modified has invalid state !");
						}
					}
				}

				if (shout != null) {
					// make this session pending, waiting for specialist's decision
					final String key = shoutId;
					final ShoutFromTraderSession session = pendingRequestSessions
							.get(key);

					if (session != null) {
						final String s = "Possible bug: no pending ShoutFromTraderSession expected !";
						new Exception(s).printStackTrace();
						throw new CatpMessageErrorException(s);
					}

					pendingRequestSessions.put(key, this);

					// need send request to specialist
					shout.setTrader((Trader) client);

					final AuctionEvent event = new ShoutReceivedEvent(shout);
					event.setTime(clock.getTime());

					// sends to registry, console, and reports
					controller.processEventInsideServer(event);

					// forward to the broker
					final HashSet<String> receivers = new HashSet<String>();
					receivers.add(brokerId);
					dispatchEvent(event, receivers);
				} else if (response == null) {
					throw new CatpMessageErrorException(
							"Possible bug: response should be ready since shout is null and is not going to be forwarded to specialist !");
				} else {
					response.setTag(request.getTag());
					dispatchOutgoingMessage(response, this);
				}
			} // synchronization on specialist
		}

		public String toString() {
			return "ShoutFromTraderSession[" + shout + "]";
		}

	}

	class BidFromTraderSession extends ShoutFromTraderSession {
		public BidFromTraderSession() {
			super(CatpMessage.BID);
		}

		public String toString() {
			return "BidFromTraderSession";
		}

	}

	class AskFromTraderSession extends ShoutFromTraderSession {
		public AskFromTraderSession() {
			super(CatpMessage.ASK);
		}

		public String toString() {
			return "AskFromTraderSession";
		}
	}

	class TransactionFromSpecialistSession extends CatpReactiveSession {
		public TransactionFromSpecialistSession() {
			super(connection, CatpMessage.TRANSACTION);
		}

		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			if (isTrader()) {
				throw new CatpMessageErrorException(reqType
						+ " request came from a trader: " + getClientId() + " !");
			}

			final Specialist specialist = (Specialist) client;
			synchronized (specialist) { // transaction from market

				CatpResponse response = null;

				final String idList = request.getHeader(CatpMessage.ID);
				String ids[] = null;
				String s = null;
				if ((idList == null) || (idList.length() == 0)) {
					s = "Empty id list in " + reqType + " request !";
					ConnectionAdaptor.logger.error(s);
					throw new CatpMessageErrorException(s);
				} else {
					ids = CatpMessage.parseStrings(idList);
					if (ids.length != 2) {
						s = "Invalid id list length in " + reqType + " request !";
						ConnectionAdaptor.logger.error(s);
						throw new CatpMessageErrorException(s);
					}
				}

				final String priceList = request.getHeader(CatpMessage.VALUE);
				double prices[] = null;
				if ((priceList == null) || (priceList.length() == 0)) {
					s = "Empty price list in " + reqType + " request !";
					ConnectionAdaptor.logger.error(s);
					throw new CatpMessageErrorException(s);
				} else {
					prices = CatpMessage.parseDoubles(priceList);
					if (prices.length != 1) {
						throw new CatpMessageErrorException("Price list in " + reqType
								+ " request should contain 1 numeric value !");
					}
				}

				final Shout ask = registry.getShout(ids[0]);
				final Shout bid = registry.getShout(ids[1]);

				try {
					transactionValidator.check(ask, bid, prices[0]);
				} catch (final IllegalTransactionException e) {
					if (e instanceof IllegalShoutInTransactionException) {
						response = CatpResponse
								.createResponse(CatpMessage.INVALID, new String[] {
										CatpMessage.TEXT, e.getMessage(), CatpMessage.TIME,
										CatpMessage.concatenate(clock.getTime()) });
					} else if (e instanceof IllegalTransactionPriceException) {
						response = CatpResponse.createResponse(CatpMessage.INVALID,
								new String[] { CatpMessage.TEXT, e.getMessage(),
										CatpMessage.TIME, CatpMessage.concatenate(clock.getTime()),
										CatpMessage.TYPE, CatpMessage.VALUE });
					} else {
						// TODO: should never reach this
						response = CatpResponse.createResponse(CatpMessage.INVALID);
					}

					response.setTag(request.getTag());
					dispatchOutgoingMessage(response, this);
					return;
				}

				if (Shout.TRACE) {
					ConnectionAdaptor.logger.info("ATa: " + ask);
					ConnectionAdaptor.logger.info("ATb: " + bid);
				}

				final String transactionId = manager.getIdentityOffice()
						.createIdentity(request.getType());

				response = CatpResponse.createResponse(CatpMessage.OK, new String[] {
						CatpMessage.ID, transactionId, CatpMessage.TIME,
						CatpMessage.concatenate(clock.getTime()) });

				response.setTag(request.getTag());
				dispatchOutgoingMessage(response, this);

				// the transaction price may be slightly outside [ask, bid] range, so
				// adjust it
				if (prices[0] > bid.getPrice()) {
					prices[0] = bid.getPrice();
				} else if (prices[0] < ask.getPrice()) {
					prices[0] = ask.getPrice();
				}

				final Transaction transaction = new Transaction(transactionId, ask,
						bid, prices[0]);
				transaction.setSpecialist((Specialist) client);

				AuctionEvent event = new TransactionExecutedEvent(transaction);
				event.setTime(clock.getTime());

				// event to registry, console, and reports
				controller.processEventInsideServer(event);

				// forward to traders involved in the transaction
				final Set<String> receiverIds = new LinkedHashSet<String>();
				receiverIds.add(ask.getTrader().getId());
				receiverIds.add(bid.getTrader().getId());
				dispatchEvent(event, receiverIds);

				// forward to the trader, and all subscribers
				event = new TransactionPostedEvent(transaction);
				event.setTime(clock.getTime());
				receiverIds.clear();
				ids = registry.getSubscriberIds(client.getId());
				if (ids != null) {
					for (final String id : ids) {
						receiverIds.add(id);
					}
				}

				dispatchEvent(event, receiverIds);

			}// synchronization on specialist
		}

		public String toString() {
			return "TransactionFromSpecialistSession";
		}
	}

	/**
	 * a session deals with all unexpected request.
	 */
	class OracleSession extends CatpReactiveSession {

		String state;

		public OracleSession(final String state) {
			super(connection, null);
			this.state = state;
		}

		public String getState() {
			return state;
		}

		public void setState(final String state) {
			this.state = state;
		}

		public void processRequest(final CatpRequest request) throws CatException {

			setProcessed(true);

			switch (ConnectionAdaptor.this.getState()) {
			case ClientState.ERROR:
			case ClientState.FATAL:
				if (!connection.isClosed()) {
					final CatpResponse response = CatpResponse.createResponse(
							CatpMessage.ERROR, new String[] { CatpMessage.TEXT,
									"Game is over. No request will be processed. Bye !" });
					response.setTag(request.getTag());
					dispatchOutgoingMessage(response, this);
				}

				break;

			default:

				if (!connection.isClosed()) {
					String responseType;
					if (request.getType().equalsIgnoreCase(CatpMessage.ASK)
							|| request.getType().equalsIgnoreCase(CatpMessage.BID)
							|| request.getType().equalsIgnoreCase(CatpMessage.TRANSACTION)) {
						responseType = CatpMessage.INVALID;
					} else {
						responseType = CatpMessage.ERROR;

						if (request.getType().equalsIgnoreCase(CatpMessage.REGISTER)) {
							ConnectionAdaptor.logger
									.error("Registration request comes at a wrong time " + state
											+ " from " + getClientId() + "\n" + request + "\n");
						}
					}

					final CatpResponse response = CatpResponse.createResponse(
							responseType, new String[] { CatpMessage.TYPE,
									CatpMessage.WRONGTIME, CatpMessage.TEXT,
									"Requested at a wrong time " + state + "." });
					response.setTag(request.getTag());
					dispatchOutgoingMessage(response, this);

					final ClientBehaviorController behaviorController = controller
							.getBehaviorController();
					behaviorController.observe(getClientId(),
							ClientBehaviorController.REQUEST_AT_WRONG_TIME);

					ConnectionAdaptor.logger.error("request comes at a wrong time from "
							+ getClient().getId() + ":\n" + request);
					showReactiveSessions();
				}
			}
		}

		public String toString() {
			return "OracleSession";
		}

	}

	public String getClientId() {
		if (client == null) {
			return "unamed client";
		} else {
			return client.getId();
		}
	}

	public ReactiveConnection<CatpMessage> getConnection() {
		return connection;
	}

	protected void showReactiveSessions() {
		if (reactiveSessions.length > 0) {
			ConnectionAdaptor.logger.info("< connection adaptor for " + getClientId()
					+ "\n");
		}
		for (final CatpReactiveSession reactiveSession : reactiveSessions) {
			ConnectionAdaptor.logger.info("\t allowed reactive session: "
					+ reactiveSession.toString());
		}

		if (reactiveSessions.length > 0) {
			ConnectionAdaptor.logger.info(">\n");
		}
	}

	protected void clearPendingRequestSessions() {
		if (!pendingRequestSessions.isEmpty()) {

			ConnectionAdaptor.logger.error(getClientId()
					+ " has pending ShoutFromTraderSessions when day is closing: "
					+ pendingRequestSessions.keySet() + " !");

			// TODO: to deal with the pending ShoutFromTraderSessions

			final Iterator<ShoutFromTraderSession> itor = pendingRequestSessions
					.values().iterator();
			ShoutFromTraderSession session = null;
			while (itor.hasNext()) {
				session = itor.next();

				// TODO: to tell in the response that the session fails to complete!
				final CatpResponse response = CatpResponse.createResponse(
						CatpMessage.INVALID, new String[] { CatpMessage.TYPE,
								CatpMessage.SPECIALIST, CatpMessage.TIME,
								CatpMessage.concatenate(clock.getTime()) });
				response.setTag(ConnectionAdaptor.tag);
				dispatchOutgoingMessage(response, session);
			}

			pendingRequestSessions.clear();
		}
	}

	protected void clearPendingProactiveSessions() {
		final CatpProactiveSession sessions[] = proactiveSessions
				.toArray(new CatpProactiveSession[0]);
		if (sessions.length > 0) {
			ConnectionAdaptor.logger
					.info("< Pending proactive sessions at server for " + getClientId()
							+ "\n");

			for (final CatpProactiveSession session : sessions) {
				ConnectionAdaptor.logger.info(" pending proactive session: " + session);
			}

			ConnectionAdaptor.logger.info(">\n");

			for (final CatpProactiveSession session : sessions) {
				session.forceOut();
				proactiveSessions.remove(session);
			}
		}

	}

	/**
	 * to observe the results of dispatching events to other clients.
	 * 
	 * @param o
	 * @param arg
	 *          the AuctionEvent attempted to dispatch
	 */
	public void update(final Observable o, final Object arg) {
		if (o instanceof EventDispatchingTaskOnServerSide) {
			// event dispatching failed

			final EventDispatchingTaskOnServerSide task = (EventDispatchingTaskOnServerSide) o;
			AuctionEvent event = task.getEvent();
			final String receiverId = (String) arg;

			if (event instanceof ShoutReceivedEvent) {
				// failed to forward shout to specialist

				final ShoutReceivedEvent srEvent = (ShoutReceivedEvent) event;

				event = new ShoutRejectedEvent(srEvent.getShout());
				event.setTime(clock.getTime());

				// tell registry
				controller.processEventInsideServer(event);

				// forward to the trader (myself) only
				final HashSet<String> receiverIds = new HashSet<String>();
				receiverIds.add(srEvent.getShout().getTrader().getId());
				dispatchEvent(event, receiverIds);

				ConnectionAdaptor.logger.warn("Adaptor for " + getClient().getId()
						+ " failed to forward shout " + srEvent.getShout().getId() + " to "
						+ receiverId + " !\n");

			} else if (event instanceof ShoutPlacedEvent) {
				// failed to notify trader or subscribers of shout placed
				// do nothing

				final ShoutPlacedEvent spEvent = (ShoutPlacedEvent) event;

				ConnectionAdaptor.logger.warn("Adaptor for " + getClient().getId()
						+ " failed to notify " + receiverId + " of the placed shout "
						+ spEvent.getShout().getId() + " !\n");

			} else if (event instanceof ShoutRejectedEvent) {
				// failed to notify trader of shout rejected
				// do nothing

				final ShoutRejectedEvent srEvent = (ShoutRejectedEvent) event;

				ConnectionAdaptor.logger.warn("Adaptor for " + getClient().getId()
						+ " failed to notify " + receiverId + " of the rejected shout "
						+ srEvent.getShout().getId() + " !\n");

			} else if (event instanceof TransactionPostedEvent) {
				// failed to notify of transaction made
				// do nothing

				final TransactionPostedEvent teEvent = (TransactionPostedEvent) event;

				ConnectionAdaptor.logger.warn("Adaptor for " + getClient().getId()
						+ " failed to notify " + receiverId + " of the transaction "
						+ teEvent.getTransaction().getId() + " !\n");

			} else if (event instanceof RegistrationEvent) {
				// failed to notify specialist of trader registration
				// do nothing

				ConnectionAdaptor.logger.warn("Adaptor for " + getClient().getId()
						+ " failed to notify " + receiverId + " of registration !\n");

			} else if (event instanceof SubscriptionEvent) {
				// failed to notify specialist of client subscription
				// do nothing

				ConnectionAdaptor.logger.warn("Adaptor for " + getClient().getId()
						+ " failed to notify " + receiverId + " of subscription !\n");
			} else {
				ConnectionAdaptor.logger.warn("Adaptor for " + getClient().getId()
						+ " failed to notify " + receiverId + " of " + event + " !\n");

			}
		} else if (o instanceof IncomingMessageDispatchingTask) {
			// TODO: check what to do with arg
			final IncomingMessageDispatchingTask task = (IncomingMessageDispatchingTask) o;
			ConnectionAdaptor.logger.warn("Adaptor for " + getClient().getId()
					+ " failed to process task " + task + " regarding message from "
					+ task.getClientId() + ":\n" + task.getMessage() + " !\n");
		} else if (o instanceof OutgoingMessageDispatchingTask) {
			final OutgoingMessageDispatchingTask task = (OutgoingMessageDispatchingTask) o;
			ConnectionAdaptor.logger.warn("Adaptor for " + getClient().getId()
					+ " failed to process task " + task + " regarding message to "
					+ task.getClientId() + ":\n" + task.getMessage() + " !\n");
		} else {
			ConnectionAdaptor.logger.error("Unexpected observable " + o
					+ " with argument " + arg + " !\n");
		}
	}

	/**
	 * for debug only.
	 */
	protected boolean failed = false;

	protected void debug(final String s) {
		if (failed) {
			ConnectionAdaptor.logger.info("DEBUG: " + s);
		}
	}
}
