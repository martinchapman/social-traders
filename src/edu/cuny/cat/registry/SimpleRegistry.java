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

package edu.cuny.cat.registry;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Account;
import edu.cuny.cat.core.AccountHolder;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.ClientStateUpdatedEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.FeesAnnouncedEvent;
import edu.cuny.cat.event.FundTransferEvent;
import edu.cuny.cat.event.GameOverEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.PrivateValueAssignedEvent;
import edu.cuny.cat.event.RegistrationEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.event.RoundClosingEvent;
import edu.cuny.cat.event.RoundOpenedEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.cat.event.ShoutReceivedEvent;
import edu.cuny.cat.event.ShoutRejectedEvent;
import edu.cuny.cat.event.SimulationOverEvent;
import edu.cuny.cat.event.SimulationStartedEvent;
import edu.cuny.cat.event.SpecialistCheckInEvent;
import edu.cuny.cat.event.SubscriptionEvent;
import edu.cuny.cat.event.TraderCheckInEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.server.ClientState;
import edu.cuny.cat.server.GameClock;
import edu.cuny.cat.server.GameController;

/**
 * Implements {@link Registry} by logging game information simply into internal
 * data structures, in contrast to some external permanent storage.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.72 $
 */
public class SimpleRegistry implements Registry {

	private static Logger logger = Logger.getLogger(SimpleRegistry.class);

	// working traders
	protected Map<String, Trader> workingTraders;

	// working specialists
	protected Map<String, Specialist> workingSpecialists;

	// failed traders
	protected Map<String, Trader> failedTraders;

	// failed specialists
	protected Map<String, Specialist> failedSpecialists;

	// expected specialists
	protected Map<String, Specialist> expectedSpecialists;

	// working specialists that announced fees
	protected Map<String, Specialist> activeSpecialists;

	protected Map<String, Shout> shouts;

	protected Map<String, Transaction> transactions;

	protected Map<String, Set<String>> subscriptions;

	protected GameController controller;

	protected GameClock clock;

	final static DecimalFormat Formatter = new DecimalFormat(
			"+#########0.00;-#########.00");

	public SimpleRegistry() {
		workingTraders = Collections.synchronizedMap(new HashMap<String, Trader>());
		workingSpecialists = Collections
				.synchronizedMap(new HashMap<String, Specialist>());
		failedTraders = Collections.synchronizedMap(new HashMap<String, Trader>());
		failedSpecialists = Collections
				.synchronizedMap(new HashMap<String, Specialist>());
		expectedSpecialists = Collections
				.synchronizedMap(new HashMap<String, Specialist>());
		activeSpecialists = Collections
				.synchronizedMap(new HashMap<String, Specialist>());
		shouts = Collections.synchronizedMap(new HashMap<String, Shout>());
		transactions = Collections
				.synchronizedMap(new HashMap<String, Transaction>());
		subscriptions = Collections
				.synchronizedMap(new HashMap<String, Set<String>>());

		controller = GameController.getInstance();
		clock = controller.getClock();
	}

	private <T extends AccountHolder> boolean moveClient(final T client,
			final Map<String, T> oldMap, final Map<String, T> newMap) {
		final boolean flag = oldMap.containsKey(client.getId())
				&& !newMap.containsKey(client.getId());
		oldMap.remove(client.getId());
		newMap.put(client.getId(), client);
		return flag;
	}

	public void addExpectedSpecialist(final Specialist specialist) {
		expectedSpecialists.put(specialist.getId(), specialist);
	}

	protected void welcomeExpectedSpecialist(final Specialist specialist) {
		if (specialist == null) {
			return;
		} else {
			moveClient(specialist, expectedSpecialists, workingSpecialists);
		}
	}

	protected void addFailedClient(final AccountHolder client) {

		boolean successful = false;

		if (client == null) {
			return;
		} else if (client instanceof Trader) {
			successful = moveClient((Trader) client, workingTraders, failedTraders);
		} else if (client instanceof Specialist) {
			successful = moveClient((Specialist) client, workingSpecialists,
					failedSpecialists);
		}

		if (successful) {
			SimpleRegistry.logger.info("failed client " + client.getId()
					+ " removed from working client lists in registry !\n");
		}
	}

	protected void restoreFailedClient(final AccountHolder client) {

		boolean successful = false;

		if (client == null) {
			return;
		} else if (client instanceof Trader) {
			successful = moveClient((Trader) client, failedTraders, workingTraders);
		} else if (client instanceof Specialist) {
			successful = moveClient((Specialist) client, failedSpecialists,
					workingSpecialists);
		}

		if (successful) {
			SimpleRegistry.logger.info("failed client " + client.getId()
					+ " reconnected in and marked working in registry !");
		}
	}

	/**
	 * 
	 * @param payer
	 *          the party to pay money for buying goods or service
	 * @param payee
	 *          the party to receive money for selling goods or service
	 * @param type
	 *          the type of the monetary transaction
	 * @param amount
	 *          the amount to be paid for one unit of goods or service
	 * @param quantity
	 *          units of goods or times of service
	 */
	protected synchronized void transferFund(final AccountHolder payer,
			final AccountHolder payee, final String type, final double amount,
			final int quantity) {

		if ((payer == null) || (payee == null)) {
			SimpleRegistry.logger
					.fatal("Null payer or payee during fund transfer in "
							+ getClass().getSimpleName() + ".");
			return;
		}

		double transferAmount = 0;
		synchronized (payer.getAccount()) {
			synchronized (payee.getAccount()) {

				// adjust assets credit for buyer
				if (type == Account.GOODS) {
					// TODO: to consider the quantity in this transaction

					// the buyer obtains the goods from seller, so add its value to the
					// balance to avoid overdraft

					final Trader trader = (Trader) payer;
					trader.getAccount().receiveFund(Account.ASSETS, "",
							trader.getPrivateValue() * quantity);
				}

				transferAmount = payer.getAccount().payFundAvailable(type,
						payee.getId(), amount * quantity);

				if (transferAmount < 0) {
					SimpleRegistry.logger
							.error("Bug: amount of fund to transfer should NOT be negative !");
				} else {
					payee.getAccount().receiveFund(type, payer.getId(), transferAmount);
					// logger.info(payer.getId() + " -> " + payee.getId() + " for " + type
					// + " @ " + Formatter.format(transferAmount) + " / "
					// + Formatter.format(amount));
				}

				// adjust assets credit for seller
				if (type == Account.GOODS) {
					// TODO: to consider the quantity in this transaction

					// the seller ships goods to buyer and lost the value of assets

					final Trader trader = (Trader) payee;
					trader.getAccount().payFundAvailable(Account.ASSETS, "",
							trader.getPrivateValue() * quantity);
				}
			}
		}

		final FundTransferEvent event = new FundTransferEvent(payer, payee, type,
				transferAmount);
		event.setTime(clock.getTime());
		controller.processEventInsideServer(event);

		if (transferAmount < amount) {
			SimpleRegistry.logger.info(payer.getId() + " paid " + payee.getId() + " "
					+ SimpleRegistry.Formatter.format(transferAmount)
					+ " instead of requested " + SimpleRegistry.Formatter.format(amount)
					+ " as " + type + " charge.\n");
		}
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof TraderCheckInEvent) {
			processTraderCheckIn((TraderCheckInEvent) event);
		} else if (event instanceof SpecialistCheckInEvent) {
			processSpecialistCheckIn((SpecialistCheckInEvent) event);
		} else if (event instanceof ShoutReceivedEvent) {
			processShoutReceived((ShoutReceivedEvent) event);
		} else if (event instanceof ShoutRejectedEvent) {
			processShoutRejected((ShoutRejectedEvent) event);
		} else if (event instanceof ShoutPlacedEvent) {
			processShoutPlaced((ShoutPlacedEvent) event);
		} else if (event instanceof TransactionExecutedEvent) {
			processTransactionExecuted((TransactionExecutedEvent) event);
		} else if (event instanceof FeesAnnouncedEvent) {
			processFeesAnnounced((FeesAnnouncedEvent) event);
		} else if (event instanceof SubscriptionEvent) {
			processSubscription((SubscriptionEvent) event);
		} else if (event instanceof PrivateValueAssignedEvent) {
			processPrivateValueAssigned((PrivateValueAssignedEvent) event);
		} else if (event instanceof RegistrationEvent) {
			processRegistration((RegistrationEvent) event);
		} else if (event instanceof GameStartingEvent) {
			processGameStarting((GameStartingEvent) event);
		} else if (event instanceof GameStartedEvent) {
			processGameStarted((GameStartedEvent) event);
		} else if (event instanceof GameOverEvent) {
			processGameOver((GameOverEvent) event);
		} else if (event instanceof DayOpeningEvent) {
			processDayOpening((DayOpeningEvent) event);
		} else if (event instanceof DayOpenedEvent) {
			processDayOpened((DayOpenedEvent) event);
		} else if (event instanceof DayClosedEvent) {
			processDayClosed((DayClosedEvent) event);
		} else if (event instanceof RoundOpenedEvent) {
			processRoundOpened((RoundOpenedEvent) event);
		} else if (event instanceof RoundClosingEvent) {
			processRoundClosing((RoundClosingEvent) event);
		} else if (event instanceof RoundClosedEvent) {
			processRoundClosed((RoundClosedEvent) event);
		} else if (event instanceof SimulationStartedEvent) {
			processSimulationStarted((SimulationStartedEvent) event);
		} else if (event instanceof SimulationOverEvent) {
			processSimulationOver((SimulationOverEvent) event);
		} else if (event instanceof ClientStateUpdatedEvent) {
			processClientStatusUpdated((ClientStateUpdatedEvent) event);
		} else if (event instanceof FundTransferEvent) {
			processFundTransfer((FundTransferEvent) event);
		} else {
			SimpleRegistry.logger
					.error("has yet to be implemented in SimpleRegistry : "
							+ event.getClass().getSimpleName());
		}
	}

	protected void processFundTransfer(final FundTransferEvent event) {
	}

	protected void processRoundClosing(final RoundClosingEvent event) {
	}

	protected void processRoundClosed(final RoundClosedEvent event) {
	}

	protected void processDayClosed(final DayClosedEvent event) {
		// TODO: to adjust the account balance of traders based on their inventory
	}

	protected void processRoundOpened(final RoundOpenedEvent event) {
	}

	protected void processDayOpening(final DayOpeningEvent event) {

		activeSpecialists.clear();

		final Trader traders[] = getTraders();
		for (final Trader trader : traders) {
			trader.setSpecialistId(null);
		}

		final Specialist specialists[] = getSpecialists();
		for (final Specialist specialist : specialists) {
			specialist.clearTraders();
			specialist.setRegistrationFee(Double.NaN);
			specialist.setInformationFee(Double.NaN);
			specialist.setShoutFee(Double.NaN);
			specialist.setTransactionFee(Double.NaN);
			specialist.setProfitFee(Double.NaN);
		}

		shouts.clear();
		transactions.clear();
		subscriptions.clear();
	}

	protected void processDayOpened(final DayOpenedEvent event) {
	}

	protected void processGameOver(final GameOverEvent event) {
	}

	protected void processGameStarting(final GameStartingEvent event) {
		// reset traders and specialists' cumulativeProfits
		final Trader traders[] = getTraders();
		for (final Trader trader : traders) {
			trader.getAccount().reset();
		}

		final Specialist specialists[] = getSpecialists();
		for (final Specialist specialist : specialists) {
			specialist.getAccount().reset();
		}

	}

	protected void processGameStarted(final GameStartedEvent event) {
	}

	protected void processPrivateValueAssigned(
			final PrivateValueAssignedEvent event) {
		final Trader trader = getTrader(event.getTraderId());

		if (trader == null) {
			SimpleRegistry.logger
					.fatal("Nonexisting trader in processPrivateValueAssigned() !");
			return;
		} else {
			trader.setPrivateValue(event.getPrivateValue());

			// initialize the balance of the account every day
			// TODO: at the end of each day, the value of unexchanged goods should be
			// deducted
			trader.getAccount().setBalance(
					event.getPrivateValue() * trader.getEntitlement());
		}
	}

	protected void processSubscription(final SubscriptionEvent event) {
		Set<String> subscribers = subscriptions.get(event.getSpecialistId());
		if (subscribers == null) {
			subscribers = Collections.synchronizedSet(new HashSet<String>());
			subscriptions.put(event.getSpecialistId(), subscribers);
		}

		if (!subscribers.contains(event.getSubscriberId())) {
			subscribers.add(event.getSubscriberId());

			final Specialist specialist = getSpecialist(event.getSpecialistId());
			final double informationFee = specialist.getInformationFee();
			if (informationFee != 0) {
				AccountHolder client = null;
				if (containsSpecialist(event.getSubscriberId())) {
					client = getSpecialist(event.getSubscriberId());
				} else if (containsTrader(event.getSubscriberId())) {
					client = getTrader(event.getSubscriberId());
				}

				transferFund(client, specialist, Account.INFORMATION_FEE,
						informationFee, 1);
			}
		}
	}

	protected void processTraderCheckIn(final TraderCheckInEvent event) {
		// trader.setInitialTradeEntitlement(event.getInitialTradeEntitlement());
		final Trader trader = event.getTrader();
		if (getFailedClient(trader.getId()) != null) {
			// failed client reconnects in
			restoreFailedClient(trader);
		} else {
			workingTraders.put(trader.getId(), trader);
		}
	}

	protected void processSpecialistCheckIn(final SpecialistCheckInEvent event) {
		final Specialist specialist = event.getSpecialist();
		if (getExpectedSpecialist(specialist.getId()) != null) {
			// expected client connects in
			welcomeExpectedSpecialist(specialist);
		} else if (getFailedClient(specialist.getId()) != null) {
			// failed client reconnects in
			restoreFailedClient(specialist);
		} else {
			// new client
			workingSpecialists.put(specialist.getId(), specialist);
		}
	}

	/**
	 * TODO: shout related event processing changes shout directly, which however
	 * may have negative effects on processing in game reports etc. as Kai pointed
	 * out
	 */
	protected void processShoutPlaced(final ShoutPlacedEvent event) {

		final Shout shout = getShout(event.getShout().getId());

		if (shout == event.getShout()) {
			// new shout

			if (event.getShout().getState() == Shout.PENDING) {
				event.getShout().setState(Shout.PLACED);

				if (Shout.TRACE) {
					SimpleRegistry.logger.info("RS+n: " + event.getShout());
				}

				final Specialist specialist = event.getShout().getSpecialist();
				final double shoutFee = specialist.getShoutFee();

				if (shoutFee != 0) {
					final Trader trader = event.getShout().getTrader();
					transferFund(trader, specialist, Account.SHOUT_FEE, shoutFee, 1);
				}
			} else {
				SimpleRegistry.logger.fatal(
						"Bug: attempting to place a shout that is not in state PENDING!",
						new Exception());
			}
		} else {
			// modified shout

			if (shout.getState() != Shout.PLACED) {
				SimpleRegistry.logger
						.fatal("Attempting to modify a shout that is NOT in the state of PLACED !");
				return;
			} else if ((shout.getChild() == null)
					|| (shout.getChild() != event.getShout())) {
				SimpleRegistry.logger.fatal(
						"Attempting to modify a non-existing shout !", new Exception());
			} else {
				event.getShout().setState(Shout.PLACED);

				if (Shout.TRACE) {
					SimpleRegistry.logger.info("RS-*: " + shout);
				}
				shout.setPrice(event.getShout().getPrice());
				shout.setQuantity(event.getShout().getQuantity());
				shout.setChild(null);
				if (Shout.TRACE) {
					SimpleRegistry.logger.info("RS+m: " + event.getShout());
				}
			}
		}
	}

	protected void processShoutRejected(final ShoutRejectedEvent event) {

		final Shout shout = getShout(event.getShout().getId());

		if (shout == event.getShout()) {
			// rejected a new shout

			if (shout.getState() == Shout.PENDING) {
				shout.setState(Shout.REJECTED);
				if (Shout.TRACE) {
					SimpleRegistry.logger.info("RSxn: " + shout);
				}
			} else {
				SimpleRegistry.logger.fatal(
						"Attempted to reject a new shout that is NOT pending !",
						new Exception());
			}
		} else {
			// rejected a modified shout

			if (shout.getChild() != null) {
				if (shout.getChild() != event.getShout()) {
					SimpleRegistry.logger.fatal(
							"attempted to reject a non-existing shout !", new Exception());
				} else {
					if (event.getShout().getState() == Shout.PENDING) {
						event.getShout().setState(Shout.REJECTED);
						shout.setChild(null);
						if (Shout.TRACE) {
							SimpleRegistry.logger.info("RSxm: " + shout);
						}
					} else {
						SimpleRegistry.logger.fatal(
								"Attempted to reject a modifying shout that is NOT pending !",
								new Exception());
					}
				}
			} else {
				SimpleRegistry.logger
						.fatal(
								"Bug: attempting to reject a modifying shout that seems non-existing!",
								new Exception());
			}
		}
	}

	/*
	 * brand new shouts: add into the shouts structure
	 * 
	 * modified shouts: set as the child of the standing shout
	 */
	protected void processShoutReceived(final ShoutReceivedEvent event) {

		event.getShout().setState(Shout.PENDING);

		final Shout shout = getShout(event.getShout().getId());
		if (shout != null) {
			// modifying shout
			if (shout.getState() != Shout.PLACED) {
				SimpleRegistry.logger
						.fatal(
								"Bug: received a shout modifying another that is not in state PLACED!",
								new Exception());
			} else {
				shout.setChild(event.getShout());
				event.getShout().setParent(shout);
				if (Shout.TRACE) {
					SimpleRegistry.logger.info("RSrm: " + event.getShout());
				}
			}
		} else {
			// brand new shout
			shouts.put(event.getShout().getId(), event.getShout());
			if (Shout.TRACE) {
				SimpleRegistry.logger.info("RSrn: " + event.getShout());
			}
		}
	}

	protected void processTransactionExecuted(final TransactionExecutedEvent event) {
		final Transaction transaction = event.getTransaction();
		final Shout ask = transaction.getAsk();
		final Shout bid = transaction.getBid();
		if ((ask.getState() != Shout.PLACED) || (bid.getState() != Shout.PLACED)) {
			SimpleRegistry.logger
					.fatal(
							"Bug: attempting to trader between two shouts that are not yet in state PLACED !",
							new Exception());
			SimpleRegistry.logger.fatal("Ask: " + transaction.getAsk());
			SimpleRegistry.logger.fatal("Bid: " + transaction.getBid());
			return;
		} else {
			ask.setState(Shout.MATCHED);
			bid.setState(Shout.MATCHED);

			if (Shout.TRACE) {
				if (ask.getChild() == null) {
					SimpleRegistry.logger.info("RTo: " + ask);
				} else {
					SimpleRegistry.logger.info("RTm: " + ask);
				}

				if (bid.getChild() == null) {
					SimpleRegistry.logger.info("RTo: " + bid);
				} else {
					SimpleRegistry.logger.info("RTm: " + bid);
				}
			}

			transactions.put(transaction.getId(), transaction);

			final Specialist specialist = transaction.getSpecialist();
			final Trader seller = ask.getTrader();
			final Trader buyer = bid.getTrader();
			final double transactionFee = specialist.getTransactionFee();
			final double profitFee = specialist.getProfitFee();

			transferFund(buyer, seller, Account.GOODS, transaction.getPrice(),
					transaction.getQuantity());

			if (transactionFee != 0) {
				transferFund(buyer, specialist, Account.TRANSACTION_FEE,
						transactionFee, 1);
				transferFund(seller, specialist, Account.TRANSACTION_FEE,
						transactionFee, 1);
			}

			if (profitFee != 0) {
				// logger.info(CatpMessage.TRANSACTION + " : " + transaction.getId());
				// logger.info(seller.getId() + " : " + seller.getPrivateValue() + " ->
				// "
				// + transaction.getAsk().getPrice() + " -> " + transaction.getPrice());
				// logger.info(buyer.getId() + " : " + buyer.getPrivateValue() + " -> "
				// + transaction.getBid().getPrice() + " -> " + transaction.getPrice());
				// logger.info("\n");

				transferFund(buyer, specialist, Account.PROFIT_FEE,
						(bid.getPrice() - transaction.getPrice()) * profitFee, transaction
								.getQuantity());

				transferFund(seller, specialist, Account.PROFIT_FEE, (transaction
						.getPrice() - ask.getPrice())
						* profitFee, transaction.getQuantity());
			}
		}
	}

	protected void processFeesAnnounced(final FeesAnnouncedEvent event) {
		activeSpecialists.put(event.getSpecialist().getId(), event.getSpecialist());
	}

	protected void processRegistration(final RegistrationEvent event) {

		final Trader trader = workingTraders.get(event.getTraderId());
		final Specialist specialist = getSpecialist(event.getSpecialistId());
		if (trader != null) {
			trader.setSpecialistId(event.getSpecialistId());
			if (specialist != null) {
				specialist.registerTrader(trader);
			} else {
				SimpleRegistry.logger.fatal("Attempting to register "
						+ event.getTraderId() + " to non-existing specialist "
						+ event.getSpecialistId() + " !");
				return;
			}
		} else {
			SimpleRegistry.logger.fatal("Attempting to register non-existing trader "
					+ event.getTraderId() + " to " + event.getSpecialistId() + " !");
			return;
		}

		final double registrationFee = specialist.getRegistrationFee();

		if (registrationFee != 0) {
			transferFund(trader, specialist, Account.REGISTRATION_FEE,
					registrationFee, 1);
		}
	}

	protected void processSimulationStarted(final SimulationStartedEvent event) {
	}

	protected void processSimulationOver(final SimulationOverEvent event) {
	}

	protected void processClientStatusUpdated(final ClientStateUpdatedEvent event) {
		if (event.getCurrentState().getCode() == ClientState.FATAL) {
			addFailedClient(event.getClient());
		} else {
			// TODO: disregard other cases?
		}
	}

	// //////////////////////////////////////////////////////

	public String[] getWorkingTraderIds() {
		final String list[] = workingTraders.keySet().toArray(new String[0]);
		Arrays.sort(list);
		return list;
	}

	public String[] getWorkingSpecialistIds() {
		final String list[] = workingSpecialists.keySet().toArray(new String[0]);
		Arrays.sort(list);
		return list;
	}

	public Specialist getWorkingSpecialist(final String specialistId) {
		return workingSpecialists.get(specialistId);
	}

	public Trader getWorkingTrader(final String traderId) {
		return workingTraders.get(traderId);
	}

	public Specialist[] getWorkingSpecialists() {
		final Specialist list[] = new Specialist[workingSpecialists.size()];
		final Object keys[] = workingSpecialists.keySet().toArray();
		Arrays.sort(keys);
		for (int i = 0; i < list.length; i++) {
			list[i] = workingSpecialists.get(keys[i]);
		}

		return list;
	}

	public Specialist[] getActiveSpecialists() {
		final Specialist list[] = new Specialist[activeSpecialists.size()];
		final Object keys[] = activeSpecialists.keySet().toArray();
		Arrays.sort(keys);
		for (int i = 0; i < list.length; i++) {
			list[i] = activeSpecialists.get(keys[i]);
		}

		return list;
	}

	public Specialist getActiveSpecialist(final String specialistId) {
		return activeSpecialists.get(specialistId);
	}

	public Trader[] getWorkingTraders() {
		final Trader list[] = new Trader[workingTraders.size()];
		final Object keys[] = workingTraders.keySet().toArray();
		Arrays.sort(keys);
		for (int i = 0; i < list.length; i++) {
			list[i] = workingTraders.get(keys[i]);
		}

		return list;
	}

	public AccountHolder getFailedClient(final String id) {
		if (failedSpecialists.containsKey(id)) {
			return failedSpecialists.get(id);
		} else if (failedTraders.containsKey(id)) {
			return failedTraders.get(id);
		} else {
			return null;
		}
	}

	public Specialist getExpectedSpecialist(final String id) {
		if (expectedSpecialists.containsKey(id)) {
			return expectedSpecialists.get(id);
		} else {
			return null;
		}
	}

	public Shout getShout(final String shoutId) {
		return shouts.get(shoutId);
	}

	public Shout[] getShouts() {
		final Shout list[] = new Shout[shouts.size()];
		final Object keys[] = shouts.keySet().toArray();
		Arrays.sort(keys);
		for (int i = 0; i < list.length; i++) {
			list[i] = shouts.get(keys[i]);
		}

		return list;
	}

	public Transaction getTransaction(final String transactionId) {
		return transactions.get(transactionId);
	}

	public Transaction[] getTransactions() {
		final Transaction list[] = new Transaction[transactions.size()];
		final Object keys[] = transactions.keySet().toArray();
		Arrays.sort(keys);
		for (int i = 0; i < list.length; i++) {
			list[i] = transactions.get(keys[i]);
		}

		return list;
	}

	public String getBrokerId(final String traderId) {
		final Trader trader = getTrader(traderId);
		if (trader != null) {
			return trader.getSpecialistId();
		} else {
			SimpleRegistry.logger
					.fatal("Attempting to query on non-existing trader for registration !");
			return null;
		}
	}

	public boolean containsWorkingSpecialist(final String specialistId) {
		return workingSpecialists.containsKey(specialistId);
	}

	public boolean containsWorkingTrader(final String traderId) {
		return workingTraders.containsKey(traderId);
	}

	public String[] getSubscriberIds(final String specialistId) {
		final Set<String> subscribers = subscriptions.get(specialistId);
		if (subscribers == null) {
			return null;
		} else {
			return subscribers.toArray(new String[0]);
		}
	}

	public void start() {
		controller = GameController.getInstance();
		clock = controller.getClock();
	}

	public void stop() {
		workingTraders.clear();
		workingSpecialists.clear();
		failedTraders.clear();
		failedSpecialists.clear();
		expectedSpecialists.clear();
		activeSpecialists.clear();
		shouts.clear();
		transactions.clear();
		subscriptions.clear();

		// workingTraders = null;
		// workingSpecialists = null;
		// failedTraders = null;
		// failedSpecialists = null;
		// expectedSpecialists = null;
		// activeSpecialists = null;
		// shouts = null;
		// transactions = null;
		// subscriptions = null;

		controller = null;
		clock = null;
	}

	public int getNumOfClients() {
		return getNumOfSpecialists() + getNumOfTraders();
	}

	public int getNumOfSpecialists() {
		return workingSpecialists.size() + expectedSpecialists.size()
				+ failedSpecialists.size();
	}

	public int getNumOfActiveSpecialists() {
		return activeSpecialists.size();
	}

	public int getNumOfTraders() {
		return workingTraders.size() + failedTraders.size();
	}

	public Trader getTrader(final String traderId) {
		if (workingTraders.containsKey(traderId)) {
			return workingTraders.get(traderId);
		} else {
			return failedTraders.get(traderId);
		}
	}

	public Trader[] getTraders() {
		final String traderIds[] = getTraderIds();
		final Trader traders[] = new Trader[traderIds.length];

		for (int i = 0; i < traderIds.length; i++) {
			traders[i] = getTrader(traderIds[i]);
		}

		return traders;
	}

	public int getNumOfWorkingTraders() {
		return workingTraders.size();
	}

	public int getNumOfWorkingSpecialists() {
		return workingSpecialists.size();
	}

	public Specialist getSpecialist(final String specialistId) {
		if (workingSpecialists.containsKey(specialistId)) {
			return workingSpecialists.get(specialistId);
		} else if (expectedSpecialists.containsKey(specialistId)) {
			return expectedSpecialists.get(specialistId);
		} else {
			return failedSpecialists.get(specialistId);
		}
	}

	public String[] getSpecialistIds() {

		final String list[] = new String[workingSpecialists.size()
				+ expectedSpecialists.size() + failedSpecialists.size()];
		final String workingKeys[] = workingSpecialists.keySet().toArray(
				new String[0]);
		for (int i = 0; i < workingKeys.length; i++) {
			list[i] = workingKeys[i];
		}

		final String expectedKeys[] = expectedSpecialists.keySet().toArray(
				new String[0]);
		for (int i = 0; i < expectedKeys.length; i++) {
			list[workingKeys.length + i] = expectedKeys[i];
		}

		final String failedKeys[] = failedSpecialists.keySet().toArray(
				new String[0]);
		for (int i = 0; i < failedKeys.length; i++) {
			list[workingKeys.length + expectedKeys.length + i] = failedKeys[i];
		}

		Arrays.sort(list);
		return list;
	}

	public Specialist[] getSpecialists() {
		final String specialistIds[] = getSpecialistIds();
		final Specialist specialists[] = new Specialist[specialistIds.length];

		for (int i = 0; i < specialistIds.length; i++) {
			specialists[i] = getSpecialist(specialistIds[i]);
		}

		return specialists;
	}

	public String[] getTraderIds() {

		final String list[] = new String[workingTraders.size()
				+ failedTraders.size()];
		final String workingKeys[] = workingTraders.keySet().toArray(new String[0]);
		for (int i = 0; i < workingKeys.length; i++) {
			list[i] = workingKeys[i];
		}

		final String failedKeys[] = failedTraders.keySet().toArray(new String[0]);
		for (int i = 0; i < failedKeys.length; i++) {
			list[workingKeys.length + i] = failedKeys[i];
		}

		Arrays.sort(list);
		return list;
	}

	public boolean containsSpecialist(final String specialistId) {
		return failedSpecialists.containsKey(specialistId)
				|| workingSpecialists.containsKey(specialistId)
				|| expectedSpecialists.containsKey(specialistId);
	}

	public boolean containsTrader(final String traderId) {
		return failedTraders.containsKey(traderId)
				|| workingTraders.containsKey(traderId);
	}

	public int getNumOfWorkingClients() {
		return workingSpecialists.size() + workingTraders.size();
	}

	public String getClientStatInfo() {
		String s = "specialists: " + getNumOfSpecialists();
		if (!failedSpecialists.isEmpty()) {
			s += " (" + failedSpecialists.size() + " dead)";
		}

		if (!expectedSpecialists.isEmpty()) {
			s += " (" + expectedSpecialists.size() + " expected)";
		}

		s += "\t traders: " + getNumOfTraders();
		if (!failedTraders.isEmpty()) {
			s += " (" + failedTraders.size() + " dead)";
		}

		return s;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
