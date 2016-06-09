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

import java.util.Collection;

import org.apache.log4j.Logger;

import edu.cuny.cat.comm.CatException;
import edu.cuny.cat.comm.CatpMessage;
import edu.cuny.cat.comm.CatpMessageErrorException;
import edu.cuny.cat.comm.CatpProactiveSession;
import edu.cuny.cat.comm.CatpReactiveSession;
import edu.cuny.cat.comm.CatpRequest;
import edu.cuny.cat.comm.CatpResponse;
import edu.cuny.cat.comm.Message;
import edu.cuny.cat.core.IllegalShoutException;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.RegistrationEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.event.RoundClosingEvent;
import edu.cuny.cat.event.RoundOpenedEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.cat.event.SubscriptionEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.event.TransactionRejectedEvent;
import edu.cuny.cat.market.Auctioneer;
import edu.cuny.cat.market.GenericDoubleAuctioneer;
import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.event.EventListener;
import edu.cuny.util.Galaxy;
import edu.cuny.util.ParamClassLoadException;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * Main class of a market (specialist) client, used to launch a single such
 * client. To start a jcat competition simulation with game server and multiple
 * clients, the {@link Game} class should be used and in this case, MarketClient
 * is involved but not as a main class.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.auctioneer</tt><br>
 * <font size=-1>class, inherits {@link Auctioneer}</font></td>
 * <td valign=top>the auctioneer regulating the market</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>market_client</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.86 $
 * 
 */

public class MarketClient extends GameClient implements EventListener {

	protected Auctioneer auctioneer = null;

	/*
	 * parameter-related
	 */
	public static final String P_DEF_BASE = "market_client";

	public static final String P_AUCTIONEER = "auctioneer";

	// TODO: move to auctioneer later on?
	protected boolean isPlayTime;

	/**
	 * this client as {@link edu.cuny.cat.core.Specialist}
	 */
	protected Specialist specialist;

	static Logger logger = Logger.getLogger(MarketClient.class);

	public MarketClient() {
		type = CatpMessage.SPECIALIST;
	}

	@Override
	public ClientRegistry createRegistry() {
		return new MarketRegistry();
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(MarketClient.P_DEF_BASE);

		try {
			auctioneer = parameters.getInstanceForParameter(base
					.push(MarketClient.P_AUCTIONEER), defBase
					.push(MarketClient.P_AUCTIONEER), Auctioneer.class);
		} catch (final ParamClassLoadException e) {
			auctioneer = new GenericDoubleAuctioneer();
		}

		try {
			((Parameterizable) auctioneer).setup(parameters, base
					.push(MarketClient.P_AUCTIONEER));

			addAuctionEventListener(auctioneer);

			if (auctioneer instanceof GenericDoubleAuctioneer) {
				((GenericDoubleAuctioneer) auctioneer)
						.setRegistry((MarketRegistry) registry);
			}

			// listen for requested transactions and subscribees from auctioneer
			Galaxy.getInstance().getDefaultTyped(EventEngine.class).checkIn(
					auctioneer, this);
		} catch (final ParamClassLoadException e) {
			MarketClient.logger
					.error("Failed to load parameters for auctioneer !", e);
		}
	}

	public Auctioneer getAuctioneer() {
		return auctioneer;
	}

	public void setAuctioneer(final Auctioneer auctioneer) {
		this.auctioneer = auctioneer;
	}

	protected class TransactionProactiveSession extends CatpProactiveSession {

		protected Transaction transaction;

		public TransactionProactiveSession(final CatpRequest request,
				final Transaction transaction) {
			super(connection, request);
			this.transaction = transaction;
		}

		@Override
		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			AuctionEvent event = null;

			if (response.getStatusCode().equalsIgnoreCase(CatpMessage.OK)) {
				final String transactionId = response.getHeader(CatpMessage.ID);
				if ((transactionId == null) || (transactionId.length() == 0)) {
					throw new CatpMessageErrorException(
							"Invalid transaction ID received !");
				} else {
					transaction.setId(transactionId);

					final Transaction currentTransaction = registry
							.getTransaction(transaction.getId());
					if (currentTransaction != null) {
						MarketClient.logger.error("Unexpected transaction info found in "
								+ getClass().getSimpleName() + " !");
					}

					event = new TransactionExecutedEvent(transaction);
					final int time[] = Message.parseIntegers(response
							.getHeader(CatpMessage.TIME));
					event.setTime(time);
					dispatchEvent(event);
					MarketClient.logger
							.debug("Transaction executed and transaction id received: "
									+ transactionId);

					if (Shout.TRACE) {
						MarketClient.logger.info("\t MTy: " + transaction.getAsk());
						MarketClient.logger.info("\t MTy: " + transaction.getBid());
					}
				}
			} else if (response.getStatusCode().equalsIgnoreCase(CatpMessage.INVALID)) {
				event = new TransactionRejectedEvent(transaction);

				// TODO: TIME header may not be used in this case.
				//
				// int time[] = CatpMessage.parseIntegers(response
				// .getHeader(CatpMessage.TIME));
				// event.setTime(time);

				dispatchEvent(event);

				if (CatpMessage.WRONGTIME.equalsIgnoreCase(response
						.getHeader(CatpMessage.TYPE))) {
					// TODO: more WRONGTIME responses to transaction requests occur
					// when a game is started long after the program is launched.
					// check it later.
				} else {
					MarketClient.logger.info("Transaction request from " + getId()
							+ " rejected :\n" + request + "\nResponse from server:\n"
							+ response);
				}

				if (Shout.TRACE) {
					MarketClient.logger.info("\n");
					MarketClient.logger.info("\t MTx: " + transaction.getAsk().getId());
					MarketClient.logger.info("\t MTx: " + transaction.getBid().getId());
					MarketClient.logger.info("\n");
				}

			} else {
				throw new CatpMessageErrorException("Unexpected "
						+ response.getStatusCode()
						+ " response received to transaction: \n" + response + "\n"
						+ request);
			}
		}
	}

	@Override
	protected CatpReactiveSession getDayOpeningSessionInstance() {
		return new DayOpeningSession();
	}

	protected class DayOpeningSession extends CatpReactiveSession {
		public DayOpeningSession() {
			super(connection, CatpMessage.OPTIONS, CatpMessage.DAYOPENING);
		}

		@Override
		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final int time[] = Message.parseIntegers(request
					.getHeader(CatpMessage.TIME));
			printDayInfo(time[0]);

			tag = request.getTag();

			setExpectedReactiveSessions(new CatpReactiveSession[] {
					new PostFeeSession(), new DayOpenedSession(),
					new OracleSession("DayOpening") });

			final DayOpeningEvent event = new DayOpeningEvent();
			event.setTime(time);
			addEventDispatchingTask(event);

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK,
					new String[] { CatpMessage.TYPE, CatpMessage.FEE, CatpMessage.VALUE,
							Message.concatenate(auctioneer.getChargingPolicy().getFees()) });
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	@Override
	protected CatpReactiveSession getDayOpenedSessionInstance() {
		return new DayOpenedSession();
	}

	protected class DayOpenedSession extends CatpReactiveSession {
		public DayOpenedSession() {
			super(connection, CatpMessage.OPTIONS, CatpMessage.DAYOPENED);
		}

		@Override
		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			setExpectedReactiveSessions(new CatpReactiveSession[] {
					new RegisterSession(), new SubscribeFromClientSession(),
					new RoundOpenedSession(), new PostAskSession(), new PostBidSession(),
					new PostTransactionSession(), new OracleSession("DayOpened") });

			final DayOpenedEvent event = new DayOpenedEvent();
			event.setTime(Message.parseIntegers(request.getHeader(CatpMessage.TIME)));
			addEventDispatchingTask(event);

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	protected class RegisterSession extends CatpReactiveSession {
		public RegisterSession() {
			super(connection, CatpMessage.REGISTER);
		}

		@Override
		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String traderId = request.getHeader(CatpMessage.ID);
			if ((traderId == null) || (traderId.length() == 0)) {
				throw new CatpMessageErrorException("Invalid trader id in " + reqType
						+ " request !");
			}

			addEventDispatchingTask(new RegistrationEvent(traderId, auctioneer
					.getName()));

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	/**
	 * TODO: currently always return OK, may reject in the future
	 */
	protected class SubscribeFromClientSession extends CatpReactiveSession {
		public SubscribeFromClientSession() {
			super(connection, CatpMessage.SUBSCRIBE);
		}

		@Override
		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String traderId = request.getHeader(CatpMessage.ID);
			if ((traderId == null) || (traderId.length() == 0)) {
				throw new CatpMessageErrorException("Invalid trader id in " + reqType
						+ " request !");
			}

			addEventDispatchingTask(new SubscriptionEvent(traderId, auctioneer
					.getName()));

			final CatpResponse response = CatpResponse.createResponse(CatpMessage.OK);
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	protected class ShoutSession extends CatpReactiveSession {

		protected Shout shout;

		public ShoutSession(final String reqType) {
			super(connection, reqType);
		}

		@Override
		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String shoutId = request.getHeader(CatpMessage.ID);
			if ((shoutId == null) || (shoutId.length() == 0)) {
				throw new CatpMessageErrorException("Invalid shout id in " + reqType
						+ " request !");
			} else {
				final double price = request.getDoubleHeader(CatpMessage.VALUE);

				Shout currentShout = null;
				currentShout = auctioneer.getShout(shoutId);
				if (currentShout != null) {
					if (currentShout.getState() == Shout.PENDING) {
						final CatpResponse response = CatpResponse
								.createResponse(
										CatpMessage.INVALID,
										new String[] { CatpMessage.TEXT,
												"The shout to be modified is being involved in a transaction !" });
						response.setTag(request.getTag());
						sendMessage(response);
						return;
					} else if (currentShout.getState() == Shout.MATCHED) {
						final CatpResponse response = CatpResponse
								.createResponse(
										CatpMessage.INVALID,
										new String[] { CatpMessage.TEXT,
												"The shout to be modified is already matched in a transaction !" });
						response.setTag(request.getTag());
						sendMessage(response);
						return;
					} else if (currentShout.getState() == Shout.PLACED) {
						shout = new Shout(shoutId, price, reqType
								.equalsIgnoreCase(CatpMessage.BID));
					} else {
						throw new CatpMessageErrorException("Bug: invalid state "
								+ currentShout.getStateDescription()
								+ " of shout at specialist !");
					}
				} else {
					shout = new Shout(shoutId, price, reqType
							.equalsIgnoreCase(CatpMessage.BID));
				}

				try {
					// add the new one first, if successful, then remove the old one
					auctioneer.newShout(shout);
					if (currentShout != null) {
						auctioneer.removeShout(currentShout);
						if (Shout.TRACE) {
							MarketClient.logger.info("\t MS-: " + currentShout);
							MarketClient.logger.info("\t MSm: " + shout);
						}
					} else {
						if (Shout.TRACE) {
							MarketClient.logger.info("\t MS+: " + shout);
						}
					}
				} catch (final IllegalShoutException e) {
					if (Shout.TRACE) {
						if (currentShout != null) {
							MarketClient.logger.info("\t MSxm: " + shout);
						} else {
							MarketClient.logger.info("\t MSxn: " + shout);
						}
					}
					// e.printStackTrace();
					MarketClient.logger.debug(e);
					final CatpResponse response = CatpResponse
							.createResponse(CatpMessage.INVALID);
					response.setTag(request.getTag());
					sendMessage(response);
					return;
				} catch (final RuntimeException e) {
					MarketClient.logger
							.error("RuntimeException occurred during processing new shouts !");
					e.printStackTrace();
					final CatpResponse response = CatpResponse
							.createResponse(CatpMessage.INVALID);
					response.setTag(request.getTag());
					sendMessage(response);
					return;
				}

				shout.setState(Shout.PLACED);

				if (specialist == null) {
					specialist = registry.getSpecialist(clientId);
					if (specialist == null) {
						MarketClient.logger
								.fatal("No information available in the local registry regarding specialist "
										+ clientId + " itself !");
					}
				}
				shout.setSpecialist(specialist);

				final ShoutPlacedEvent event = new ShoutPlacedEvent(shout);
				final int time[] = Message.parseIntegers(request
						.getHeader(CatpMessage.TIME));
				event.setTime(time);
				addEventDispatchingTask(event);

				final CatpResponse response = CatpResponse
						.createResponse(CatpMessage.OK);
				response.setTag(request.getTag());
				sendMessage(response);

				// must fire the event after sending response to make sure the server
				// gets response before other messages triggered by the placed shout
				processEventDispatchingTasks();
			}
		}
	}

	protected class BidSession extends ShoutSession {
		public BidSession() {
			super(CatpMessage.BID);
		}
	}

	protected class AskSession extends ShoutSession {
		public AskSession() {
			super(CatpMessage.ASK);
		}
	}

	@Override
	protected void postTransactionReceived(Transaction transaction) {

		final Shout ask = transaction.getAsk();
		final Shout bid = transaction.getBid();

		if (Shout.TRACE) {
			MarketClient.logger.info("\t MTp: " + ask);
			MarketClient.logger.info("\t MTp: " + bid);
		}

		final Transaction currentTransaction = registry.getTransaction(transaction
				.getId());
		if (currentTransaction == null) {
			// no previous info about the transaction
			if (transaction.getSpecialist().getId().equals(clientId)) {
				// the transaction was made at this specialist
				GameClient.logger
						.error(clientId
								+ " is supposed to have information about the posted transaction already !");
				MarketClient.logger.error("transaction: " + transaction);
			}
		} else {
			// know the transaction before, the client must be the specialist
			// that makes the transaction

			if (!transaction.getSpecialist().getId().equals(clientId)) {
				GameClient.logger
						.error(clientId + " received duplicate transactions !");
				MarketClient.logger.error("transaction: " + transaction);
				MarketClient.logger.error("currentTransaction: " + currentTransaction);
			}

			if (!transaction.getAsk().equals(currentTransaction.getAsk())
					|| !transaction.getBid().equals(currentTransaction.getBid())) {
				GameClient.logger
						.error("Transaction posted disagrees with the existing one !");
				MarketClient.logger.error("transaction: " + transaction);
				MarketClient.logger.error("currentTransaction: " + currentTransaction);
			}
		}
	}

	/**
	 * for starting market clients alone, separating from trader clients and
	 * server
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {

		System.out.println(Game.getBanner());

		try {
			Game.setupObjectRegistry();

			Game.setupPreferences(null, args);

			Game.makeSureUnsynchronousInfrastructure();

			final Collection<? extends MarketClient> markets = Game.createMarkets();
			Game.startMarkets(markets);

		} catch (final Exception e) {
			e.printStackTrace();
			Game.cleanupObjectRegistry();
			System.exit(1);
		}
	}

	/**
	 * The {@link edu.cuny.event.Event}-based dispatching mechanism is used for
	 * requesting a transaction and subscription for infomration from specialists.
	 * 
	 * @param event
	 */
	public void eventOccurred(final Event event) {
		final Object obj = event.getUserObject();

		if (obj instanceof Transaction) {
			final Transaction transaction = (Transaction) obj;

			final Specialist specialist = registry.getSpecialist(clientId);
			if (specialist == null) {
				MarketClient.logger.error("Specialist " + clientId
						+ " failed to obtain reference to itself in the local registry !");
			} else {
				transaction.setSpecialist(specialist);
			}

			if (isPlayTime) {
				final String idList = Message.concatenate(new String[] {
						transaction.getAsk().getId(), transaction.getBid().getId() });
				final CatpRequest request = CatpRequest.createRequest(
						CatpMessage.TRANSACTION, new String[] { CatpMessage.ID, idList,
								CatpMessage.VALUE, String.valueOf(transaction.getPrice()) });
				request.setTag(tag);

				if (Shout.TRACE) {
					MarketClient.logger.info("\t MTa: " + transaction.getAsk());
					MarketClient.logger.info("\t MTb: " + transaction.getBid());
				}

				final CatpProactiveSession session = new TransactionProactiveSession(
						request, transaction);
				startProactiveSession(session);
			} else {
				dispatchEvent(new TransactionRejectedEvent(transaction));
			}
		} else if (obj instanceof String[]) {
			// subscribe for info from the specified markets
			final String specialistIds[] = (String[]) obj;
			if ((specialistIds != null) && (specialistIds.length > 0)) {
				final SubscribeToSpecialistSession session = new SubscribeToSpecialistSession(
						specialistIds);
				startProactiveSession(session);
			}
		}
	}

	public void eventOccurred(final AuctionEvent event) {

		if (event instanceof GameStartedEvent) {
			if (specialist == null) {
				specialist = registry.getSpecialist(clientId);
				if (specialist == null) {
					TraderClient.logger
							.error("No information available in the local registry regarding specialist "
									+ clientId + " itself !");
					specialist = registry.addSpecialist(clientId);
				}
			}
		} else if (event instanceof RoundOpenedEvent) {

			isPlayTime = true;

			setExpectedReactiveSessions(new CatpReactiveSession[] {
					new RegisterSession(), new SubscribeFromClientSession(),
					new AskSession(), new BidSession(), new RoundClosingSession(),
					new PostAskSession(), new PostBidSession(),
					new PostTransactionSession(), new OracleSession("RoundOpened") });

		} else if (event instanceof RoundClosingEvent) {

			setExpectedReactiveSessions(new CatpReactiveSession[] {
					new RegisterSession(), new SubscribeFromClientSession(),
					new AskSession(), new BidSession(), new RoundClosedSession(),
					new PostAskSession(), new PostBidSession(),
					new PostTransactionSession(), new OracleSession("RoundClosing") });

		} else if (event instanceof RoundClosedEvent) {

			isPlayTime = false;

			setExpectedReactiveSessions(new CatpReactiveSession[] {
					new RegisterSession(), new SubscribeFromClientSession(),
					new RoundOpenedSession(), new DayClosedSession(),
					new PostAskSession(), new PostBidSession(),
					new PostTransactionSession(), new PostProfitSession(),
					new OracleSession("RoundClosed") });
		}
	}
}
