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
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import edu.cuny.cat.comm.CatException;
import edu.cuny.cat.comm.CatpMessage;
import edu.cuny.cat.comm.CatpMessageErrorException;
import edu.cuny.cat.comm.CatpProactiveSession;
import edu.cuny.cat.comm.CatpReactiveSession;
import edu.cuny.cat.comm.CatpRequest;
import edu.cuny.cat.comm.CatpResponse;
import edu.cuny.cat.comm.Message;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.PrivateValueAssignedEvent;
import edu.cuny.cat.event.RegistrationEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.event.RoundClosingEvent;
import edu.cuny.cat.event.RoundOpenedEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.cat.event.ShoutRejectedEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.task.EventDispatchingTaskOnClientSide;
import edu.cuny.cat.trader.TradingAgent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * <p>
 * Main class of a trader client, used to launch a single such client. To start
 * a jcat competition simulation with game server and multiple clients, the
 * {@link Game} class should be used and in this case, TraderClient is involved
 * but not as a main class.
 * </p>
 * 
 * <p>
 * A trader client creates a {@link TradingAgent} to choose markets and
 * determine prices to offer.
 * </p>
 * 
 * @author Kai Cai
 * @version $Revision: 1.88 $
 * 
 */

public class TraderClient extends GameClient implements Observer {

	static Logger logger = Logger.getLogger(TraderClient.class);

	/**
	 * the trading agent that bids and chooses markets for this client.
	 */
	protected TradingAgent agent;

	/**
	 * this client as a {@link edu.cuny.cat.core.Trader}.
	 */
	protected Trader trader = null;

	/**
	 * the specialist this trader registers with on the current day
	 */
	protected Specialist registeredSpecialist = null;

	/**
	 * currently placed or pending shout if null, no such shout.
	 */
	protected Shout currentShout;

	public TraderClient() {
		agent = new TradingAgent();
		addAuctionEventListener(agent);
	}

	@Override
	public ClientRegistry createRegistry() {
		return new TraderRegistry();
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		agent.setup(parameters, base);
		agent.initialize();
		agent.addObserver(this);

		if ((type == null) || (type.length() == 0)) {
			if (agent.isSeller()) {
				type = CatpMessage.SELLER;
			} else {
				type = CatpMessage.BUYER;
			}
		}
	}

	public TradingAgent getAgent() {
		return agent;
	}

	@Override
	public void update(final Observable source, final Object arg) {
		if (arg == null) {
			// no active specialist selected, register with none
			final RegisterSession session = new RegisterSession("");
			startProactiveSession(session);
		} else if (arg instanceof Specialist) {
			// register with a specialist
			final Specialist specialist = (Specialist) arg;
			final RegisterSession session = new RegisterSession(specialist.getId());
			startProactiveSession(session);
		} else if (arg instanceof Shout) {
			/**
			 * make a shout:
			 * 
			 * currentShout: the current placed (but not matched) shout, or pending
			 * (to be rejected or placed)
			 * 
			 */

			final Shout shout = (Shout) arg;

			if (currentShout == null) {
				// no standing or pending shout
				currentShout = shout;
			} else {
				if (currentShout.getState() == Shout.PENDING) {
					// there is already a pending shout, just disregard this one
					return;
				} else if (currentShout.getState() == Shout.PLACED) {
					if (currentShout.getId() == null) {
						TraderClient.logger
								.fatal("Trader's current shout should have a non-empty id !");
						return;
					} else {
						// shout is an attempt to modify the current standing shout
						shout.setId(currentShout.getId());
					}

					if (currentShout.getChild() != null) {
						// another modification request is on-going, quit
						return;
					} else {
						// NOTE: adds the newly modified shout as child of the placed shout
						currentShout.setChild(shout);
					}
				} else {
					TraderClient.logger.fatal(clientId
							+ "'s current shout, if not null, should be in state PLACED !");
					TraderClient.logger.fatal("currentShout: " + currentShout);
					TraderClient.logger.fatal("shout: " + shout);
					registry.printStatus();
				}
			}

			shout.setState(Shout.PENDING);

			// logger.info("--> " + shout);

			final CatpRequest request = CatpRequest.createRequest(
					trader.isSeller() ? CatpMessage.ASK : CatpMessage.BID, new String[] {
							CatpMessage.VALUE, String.valueOf(shout.getPrice()) });
			if (shout.getId() != null) {
				request.setHeader(CatpMessage.ID, shout.getId());
			}
			request.setTag(tag);

			reportDynamics("attempt to " + (trader.isSeller() ? "ask" : "bid")
					+ " at " + shout.getPrice());

			final ShoutSession session = new ShoutSession(request);
			startProactiveSession(session);
		} else if (arg instanceof String[]) {
			// subscribe for information from a set of specialists
			final String specialistIds[] = (String[]) arg;
			final SubscribeToSpecialistSession session = new SubscribeToSpecialistSession(
					specialistIds);
			startProactiveSession(session);
		} else if ((source instanceof EventDispatchingTaskOnClientSide)
				|| (arg == this)) {
			TraderClient.logger.error("Error occurred in dispatching event "
					+ ((EventDispatchingTaskOnClientSide) source).getEvent());
		} else {
			TraderClient.logger.error("Invalid type of argurment from Observable: "
					+ arg.getClass().getSimpleName());
		}
	}

	/*****************************************************************************
	 * 
	 * sessions
	 * 
	 ****************************************************************************/

	class RegisterSession extends CatpProactiveSession {

		private final String specialistId;

		public RegisterSession(final String specialistId) {
			super(connection);
			this.specialistId = specialistId;
			setRequest(CatpRequest.createRequest(CatpMessage.REGISTER, new String[] {
					CatpMessage.ID, specialistId }));
			getRequest().setTag(tag);
		}

		@Override
		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			if (response.getStatusCode().equalsIgnoreCase(CatpMessage.OK)) {
				if ((specialistId != null) && (specialistId.length() != 0)) {
					dispatchEvent(new RegistrationEvent(clientId, specialistId));
				}
			} else {
				TraderClient.logger.error("Failed to register with request: \n"
						+ request);

				/*
				 * do not throw exception any more, instead give up the current trading
				 * day.
				 * 
				 * this could be caused by the failure of the selected specialist.
				 */
				// throw new CatpMessageErrorException("Unexpected "
				// + response.getStatusCode() + " response received !");
				final CatpMessageErrorException e = new CatpMessageErrorException(
						"Unexpected response received from server:\n" + response);
				e.printStackTrace();
			}
		}
	}

	class ShoutSession extends CatpProactiveSession {

		public ShoutSession(final CatpRequest request) {
			super(connection, request);
		}

		@Override
		public void processResponse(final CatpResponse response)
				throws CatException {
			super.processResponse(response);

			int time[] = null;
			try {
				time = Message.parseIntegers(response.getHeader(CatpMessage.TIME));
			} catch (final Exception e) {
				TraderClient.logger
						.fatal("Failed to obtain time from response received at "
								+ clientId + " : \n" + response);
				return;

				/* do not throw Error now to make traders more comfortable */
				// throw new CatpMessageErrorException(
				// "Failed to obtain time information !");
			}

			Shout shout = currentShout;

			if (currentShout == null) {
				// there SHOULD be a pending shout; if not nothing can be done except
				// return
				TraderClient.logger
						.fatal("Current shout is null while processing response in ShoutSession at "
								+ getId() + " !");
				return;
			} else if (currentShout.getChild() == null) {
				// brand new shout, do nothing
				if (currentShout.getState() != Shout.PENDING) {
					TraderClient.logger
							.fatal("Current shout should be PENDING instead of "
									+ currentShout.getStateDescription() + " !");
					return;
				}
			} else {
				// modified shout
				shout = currentShout.getChild();
				if (currentShout.getState() != Shout.PLACED) {
					// currentShout should be a placed shout and its child is modifying
					// currentShout
					TraderClient.logger
							.fatal("Bug: shout modified must be a PLACED one !\n"
									+ currentShout);
					return;
				}
			}

			shout.setTrader(trader);
			shout.setSpecialist(registeredSpecialist);

			if (response.getStatusCode().equalsIgnoreCase(CatpMessage.OK)) {
				// successful in placing a new one or modifying an old one
				shout.setState(Shout.PLACED);
				if (currentShout != shout) {
					// modifying shout

					if (Shout.TRACE) {
						TraderClient.logger.info("\t TS-: " + currentShout);
						TraderClient.logger.info("\t TSm: " + shout);
					}

					// the child shout, the modifying shout, becomes the current one
					currentShout = shout;

				} else {
					// brand new shout

					final String shoutId = response.getHeader(CatpMessage.ID);
					if ((shoutId == null) || (shoutId.length() == 0)) {
						// no Id assigned, return immediately and disregard the current
						// shout
						TraderClient.logger
								.fatal("Empty shout ID in OK response to ASK/BID request !");
						currentShout = null;
						return;
					} else {
						shout.setId(shoutId);

						if (Shout.TRACE) {
							TraderClient.logger.info("\t TS+: " + shout);
						}
					}
				}

				final ShoutPlacedEvent spEvent = new ShoutPlacedEvent(shout);
				spEvent.setTime(time);
				dispatchEvent(spEvent);

			} else {
				// failed in placing a new one or modifying an old one
				shout.setState(Shout.REJECTED);

				if (shout == currentShout) {
					// new one
					currentShout = null;
				} else {
					// modification fails, remove the pending child
					currentShout.setChild(null);
				}

				final ShoutRejectedEvent sjEvent = new ShoutRejectedEvent(shout);
				sjEvent.setTime(time);

				if (response.getStatusCode().equalsIgnoreCase(CatpMessage.INVALID)) {
					final String type = response.getHeader(CatpMessage.TYPE);

					if (type != null) {
						if (type.equalsIgnoreCase(CatpMessage.WRONGTIME)) {
							// do nothing
						} else if (type.equalsIgnoreCase(CatpMessage.SPECIALIST)) {
							// rejected by the specialist
							TraderClient.logger.debug("\t shout rejected by specialist .");
						} else {
							TraderClient.logger.error("Possible bug : request \n" + request
									+ "response: \n" + response);
						}
					} else {
						TraderClient.logger.error("Possible bug : request \n" + request
								+ "response: \n" + response);
					}

					if (Shout.TRACE) {
						TraderClient.logger.info("\t TSx: " + shout);
					}
					dispatchEvent(sjEvent);

				} else {

					dispatchEvent(sjEvent);

					throw new CatpMessageErrorException("Unexpected "
							+ response.getStatusCode() + " response received !");
				}
			}
		}

		@Override
		public String toString() {
			return "ShoutSession";
		}

	}

	@Override
	protected CatpReactiveSession getDayOpeningSessionInstance() {
		return new DayOpeningSession();
	}

	class DayOpeningSession extends CatpReactiveSession {
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
					new String[] { CatpMessage.VALUE,
							String.valueOf(agent.getInitialTradeEntitlement()) });
			response.setTag(request.getTag());
			sendMessage(response);

			processEventDispatchingTasks();
		}
	}

	@Override
	protected CatpReactiveSession getDayOpenedSessionInstance() {
		return new DayOpenedSession();
	}

	class DayOpenedSession extends CatpReactiveSession {
		public DayOpenedSession() {
			super(connection, CatpMessage.OPTIONS, CatpMessage.DAYOPENED);
		}

		@Override
		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			try {
				// TODO: currently, same private value for every of possible multiple
				// entitlements
				final double privateValue = request.getDoubleHeader(CatpMessage.VALUE);

				setExpectedReactiveSessions(new CatpReactiveSession[] {
						new RoundOpenedSession(), new TransactionSession(),
						new PostAskSession(), new PostBidSession(),
						new PostTransactionSession(), new OracleSession("DayOpened") });

				final DayOpenedEvent event = new DayOpenedEvent();
				event.setTime(Message
						.parseIntegers(request.getHeader(CatpMessage.TIME)));
				addEventDispatchingTask(event);

				addEventDispatchingTask(new PrivateValueAssignedEvent(clientId,
						privateValue));

				reportDynamics("private value: " + privateValue);

				final CatpResponse response = CatpResponse
						.createResponse(CatpMessage.OK);
				response.setTag(request.getTag());
				sendMessage(response);

				processEventDispatchingTasks();

			} catch (final CatException e) {
				e.printStackTrace();
				TraderClient.logger.error(e);
				sendMessage(CatpResponse
						.createResponse(CatpMessage.ERROR, new String[] { CatpMessage.TEXT,
								"Failed to obtain private value in OPTIONS DAYOPENED message" }));
			}
		}
	}

	protected class TransactionSession extends CatpReactiveSession {
		public TransactionSession() {
			super(connection, CatpMessage.TRANSACTION);
		}

		@Override
		public void processRequest(final CatpRequest request) throws CatException {
			super.processRequest(request);

			final String idList = request.getHeader(CatpMessage.ID);
			final String ids[] = Message.parseStrings(idList);
			if ((ids == null) || (ids.length != 4)) {
				throw new CatpMessageErrorException("Invalid id list in " + reqType
						+ " " + typeHeader + " request !");
			} else {
				try {
					final String priceList = request.getHeader(CatpMessage.VALUE);
					final double prices[] = Message.parseDoubles(priceList);
					if ((prices == null) || (prices.length != 3)) {
						throw new CatpMessageErrorException("Invalid price list in "
								+ reqType + " " + typeHeader + " request !");
					} else {

						final int quantity = 1;

						final int time[] = Message.parseIntegers(request
								.getHeader(CatpMessage.TIME));

						Specialist specialist = registry.getSpecialist(ids[3]);
						if (specialist == null) {
							GameClient.logger.error("Transaction " + ids[0]
									+ " made at an unknown specialist " + ids[3] + " !");
							// record this unknown specialist
							specialist = registry.addSpecialist(ids[3]);
						}

						if (specialist != registeredSpecialist) {
							TraderClient.logger.fatal(clientId
									+ " was unexpectedly notified of a transaction made by "
									+ specialist.getId() + " !");
							TraderClient.logger.fatal(clientId + " registered with "
									+ registeredSpecialist.getId() + " !");
						}

						Shout ask = registry.getShout(ids[1]);
						if (ask == null) {
							// the trader does not receive posted shout
							ask = createMatchedShout(ids[1], quantity, prices[1], false,
									specialist);
						}

						Shout bid = registry.getShout(ids[2]);
						if (bid == null) {
							bid = createMatchedShout(ids[2], quantity, prices[2], true,
									specialist);
						}

						if (Shout.TRACE) {
							TraderClient.logger.info("\t TTe: " + ask);
							TraderClient.logger.info("\t TTe: " + bid);
						}

						final Transaction transaction = new Transaction(ids[0], ask, bid,
								prices[0]);
						transaction.setSpecialist(specialist);

						if (currentShout != null) {
							if ((trader.isSeller() && ask.getId()
									.equals(currentShout.getId()))
									|| (!trader.isSeller() && ids[2].equals(currentShout.getId()))) {

								// set currentShout null, or its child, a modification attempt
								// which will (should) be rejected later on

								// NOTE that the modifying shout, when currentShout.getChild()
								// is not
								// null, will be rejected as a new shout in ShoutSession !
								currentShout = currentShout.getChild();
							} else {
								// no existing shout found, which is unexpected
								TraderClient.logger
										.fatal("Executed transaction does not involve the current shout of the notified trader, "
												+ clientId + " !");
								TraderClient.logger.fatal("currentShout: " + currentShout);
							}
						} else {
							// no existing shout found, which is unexpected
							TraderClient.logger
									.fatal("Trader, "
											+ clientId
											+ " does not have a standing shout matching the executed transaction !");
							TraderClient.logger.fatal("currentShout: " + currentShout);
						}

						AuctionEvent event = null;
						event = new TransactionExecutedEvent(transaction);
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
											"Failed to obtain transaction price in TRANSACTION message !" });
					response.setTag(request.getTag());
					sendMessage(response);
				}

				processEventDispatchingTasks();
			}
		}

		protected Shout createMatchedShout(final String shoutId,
				final int quantity, final double price, final boolean isBid,
				Specialist specialist) {

			// always create a new shout to avoid possible confusion
			final Shout matchedShout = GameClient.createMatchedShoutSimple(shoutId,
					quantity, price, isBid, specialist);
			final Shout recordedShout = registry.getShout(shoutId);

			if (recordedShout == null) {
				// unknown shout, this is possible only for a trader involved in the
				// transaction and the shout is from the other side, but there is a
				// little possibility that a client disconnected and reconnected in
				// within a day !

				// it is also possible that in multi-threading mode, a trader's
				// request of subscription came late and the trader misses some shouts.

				if ((isBid && !trader.isSeller()) || (!isBid && trader.isSeller())) {
					// a shout made by me, should know about it before
					matchedShout.setTrader(trader);
					TraderClient.logger.error("Trader " + clientId
							+ " does not know the matched shout of its own !");
					TraderClient.logger.error("matchedShout: " + matchedShout);
				}
			} else {
				// know this shout before
				checkAndUpdateMatchedShout(matchedShout, recordedShout);

				if ((isBid && !trader.isSeller()) || (!isBid && trader.isSeller())) {
					// a shout made by me, the trader should be me
					if (matchedShout.getTrader() != trader) {
						TraderClient.logger
								.error("The trader info in the matched shout does not match the identity of "
										+ clientId + " !");
					}
					TraderClient.logger.error("matchedShout: " + matchedShout);
				}
			}

			return matchedShout;
		}
	}

	@Override
	protected void postTransactionReceived(Transaction transaction) {
		final Shout ask = transaction.getAsk();
		final Shout bid = transaction.getBid();

		if (Shout.TRACE) {
			TraderClient.logger.info("\t TTp: " + ask);
			TraderClient.logger.info("\t TTp: " + bid);
		}

		final Transaction currentTransaction = registry.getTransaction(transaction
				.getId());
		if (currentTransaction == null) {
			// know nothing about the transaction
			if ((ask.getTrader() == trader) || (bid.getTrader() == trader)) {
				// one of the matched shouts from this trader, so should know the
				// transaction before
				TraderClient.logger
						.error(clientId
								+ " was involved in the posted transaction but does not have previous record !");
				TraderClient.logger.error("transaction: " + transaction);
				TraderClient.logger.error("currentTransaction: " + currentTransaction);
			}
		} else {
			if ((ask.getTrader() == trader) || (bid.getTrader() == trader)) {
				// one of the matched shouts from this trader, so know the transaction
				// before
			} else {
				TraderClient.logger
						.error("Unexpected duplicate tranaction received at trader "
								+ clientId + " !");
				TraderClient.logger.error("transaction: " + transaction);
				TraderClient.logger.error("currentTransaction: " + currentTransaction);
			}
		}
	}

	public void eventOccurred(final AuctionEvent event) {

		if (event instanceof GameStartedEvent) {
			if (trader == null) {
				trader = registry.getTrader(clientId);
				if (trader == null) {
					TraderClient.logger
							.error("No information available in the local registry regarding trader "
									+ clientId + " itself !");
					trader = registry.addTrader(clientId, null, clientId
							.startsWith(CatpMessage.ASK.toLowerCase()));
				}
			}
		} else if (event instanceof RegistrationEvent) {
			final RegistrationEvent rEvent = (RegistrationEvent) event;
			registeredSpecialist = registry.getSpecialist(rEvent.getSpecialistId());
			if (registeredSpecialist == null) {
				TraderClient.logger
						.error("No information available in the local registry regarding specialist "
								+ rEvent.getSpecialistId() + " this trader registers with !");
				registeredSpecialist = registry.addSpecialist(rEvent.getSpecialistId());
			}
		} else if (event instanceof DayClosedEvent) {

			currentShout = null;
			registeredSpecialist = null;

		} else if (event instanceof RoundOpenedEvent) {

			setExpectedReactiveSessions(new CatpReactiveSession[] {
					new RoundClosingSession(), new TransactionSession(),
					new PostAskSession(), new PostBidSession(),
					new PostTransactionSession(), new OracleSession("RoundOpened") });

		} else if (event instanceof RoundClosingEvent) {

			setExpectedReactiveSessions(new CatpReactiveSession[] {
					new RoundClosedSession(), new TransactionSession(),
					new PostAskSession(), new PostBidSession(),
					new PostTransactionSession(), new OracleSession("RoundClosing") });

		} else if (event instanceof RoundClosedEvent) {

			setExpectedReactiveSessions(new CatpReactiveSession[] {
					new RoundOpenedSession(), new DayClosedSession(),
					new TransactionSession(), new PostAskSession(), new PostBidSession(),
					new PostTransactionSession(), new PostProfitSession(),
					new OracleSession("RoundClosed") });
		}
	}

	/**
	 * for starting trader clients alone, separating from market clients and
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

			final Collection<? extends TraderClient> traders = Game.createTraders();
			Game.startTraders(traders);

		} catch (final Exception e) {
			e.printStackTrace();
			Game.cleanupObjectRegistry();
			System.exit(1);
		}
	}
}
