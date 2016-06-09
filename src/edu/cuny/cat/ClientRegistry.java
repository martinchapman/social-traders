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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import edu.cuny.cat.comm.CatpMessage;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.IdAssignedEvent;
import edu.cuny.cat.event.RegistrationEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.cat.event.ShoutPostedEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.event.TransactionPostedEvent;

/**
 * A registry for a game client to track shouts, transactions, traders, and
 * specialists in the game.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public abstract class ClientRegistry implements AuctionEventListener {

	protected static Logger logger = Logger.getLogger(ClientRegistry.class);

	/**
	 * game day
	 */
	protected int day = -1;

	/**
	 * the id of the client
	 */
	protected String id;

	/**
	 * a mapping of trader Ids to traders for the current game
	 */
	protected SortedMap<String, Trader> traders;

	/**
	 * a mapping of specialist Ids to specialists for the current game.
	 */
	protected SortedMap<String, Specialist> specialists;

	/**
	 * daily shouts.
	 */
	protected Map<String, Shout> shouts;

	/**
	 * daily transactions.
	 */
	protected Map<String, Transaction> transactions;

	public ClientRegistry() {
		traders = Collections.synchronizedSortedMap(new TreeMap<String, Trader>());
		specialists = Collections
				.synchronizedSortedMap(new TreeMap<String, Specialist>());
		shouts = Collections.synchronizedMap(new HashMap<String, Shout>());
		transactions = Collections
				.synchronizedMap(new HashMap<String, Transaction>());
	}

	public Shout getShout(String id) {
		return shouts.get(id);
	}

	public Transaction getTransaction(String id) {
		return transactions.get(id);
	}

	public Trader addTrader(final String id, final String desc,
			final boolean isSeller) {
		final Trader trader = new Trader(id, desc, isSeller);
		traders.put(id, trader);
		return trader;
	}

	public Trader getTrader(final String id) {
		return traders.get(id);
	}

	public Collection<Trader> getTraders() {
		return traders.values();
	}

	public Collection<String> getTraderIds() {
		return traders.keySet();
	}

	public Specialist addSpecialist(final String id) {
		final Specialist specialist = new Specialist(id);
		specialists.put(id, specialist);
		return specialist;
	}

	public Specialist getSpecialist(final String id) {
		return specialists.get(id);
	}

	public Collection<Specialist> getSpecialists() {
		return specialists.values();
	}

	public Collection<String> getSpecialistIds() {
		return specialists.keySet();
	}

	public void eventOccurred(AuctionEvent event) {
		if (event instanceof IdAssignedEvent) {
			processIdAssigned((IdAssignedEvent) event);
		} else if (event instanceof GameStartingEvent) {
			processGameStarting((GameStartingEvent) event);
		} else if (event instanceof DayOpeningEvent) {
			processDayOpening((DayOpeningEvent) event);
		} else if (event instanceof RegistrationEvent) {
			processRegistration((RegistrationEvent) event);
		} else if (event instanceof ShoutPlacedEvent) {
			// TODO: may not be needed.
			processShoutPlaced((ShoutPlacedEvent) event);
		} else if (event instanceof ShoutPostedEvent) {
			processShoutPosted((ShoutPostedEvent) event);
		} else if (event instanceof TransactionExecutedEvent) {
			// TODO: may not be needed.
			processTransactionExecuted((TransactionExecutedEvent) event);
		} else if (event instanceof TransactionPostedEvent) {
			processTransactionPosted((TransactionPostedEvent) event);
		}
	}

	protected void processIdAssigned(IdAssignedEvent event) {
		id = event.getId();
	}

	protected void processGameStarting(GameStartingEvent event) {
		day = -1;

		for (final Trader trader : traders.values()) {
			trader.reset();
		}

		for (final Specialist specialist : specialists.values()) {
			specialist.reset();
		}
	}

	protected void processDayOpening(DayOpeningEvent event) {
		day = event.getDay();
		shouts.clear();
		transactions.clear();
	}

	protected void processRegistration(RegistrationEvent event) {
		registerTrader(event.getTraderId(), event.getSpecialistId());
	}

	protected void registerTrader(String traderId, String specialistId) {
		Trader trader = traders.get(traderId);
		if (trader == null) {
			trader = addTrader(traderId, null, traderId.toLowerCase().startsWith(
					CatpMessage.SELLER.toLowerCase()));
		}

		Specialist specialist = specialists.get(specialistId);
		if (specialist == null) {
			specialist = addSpecialist(specialistId);
		}

		registerTrader(trader, specialist);
	}

	protected void registerTrader(Trader trader, Specialist specialist) {
		specialist.registerTrader(trader);
		trader.setSpecialistId(specialist.getId());
	}

	protected void processShoutPlaced(ShoutPlacedEvent event) {
		final Shout shout = event.getShout();
		shouts.put(shout.getId(), shout);
	}

	protected void processShoutPosted(ShoutPostedEvent event) {
		final Shout shout = event.getShout();
		shouts.put(shout.getId(), shout);
	}

	protected void processTransactionExecuted(TransactionExecutedEvent event) {
		final Transaction transaction = event.getTransaction();
		transactions.put(transaction.getId(), transaction);
	}

	protected void processTransactionPosted(TransactionPostedEvent event) {
		final Transaction transaction = event.getTransaction();
		transactions.put(transaction.getId(), transaction);
	}

	public void printStatus() {
		ClientRegistry.logger.info("");
		ClientRegistry.logger.info("Traders:");
		ClientRegistry.logger.info(traders.keySet());

		ClientRegistry.logger.info("");
		ClientRegistry.logger.info("Specialists:");
		ClientRegistry.logger.info(specialists.keySet());

		ClientRegistry.logger.info("");
		ClientRegistry.logger.info("Shouts:");
		ClientRegistry.logger.info(shouts.values());

		ClientRegistry.logger.info("");
		ClientRegistry.logger.info("Transactions:");
		ClientRegistry.logger.info(transactions.values());
	}
}
