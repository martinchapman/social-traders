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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.FeesAnnouncedEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.RegisteredTradersAnnouncedEvent;
import edu.cuny.cat.event.RegistrationEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.cat.event.ShoutPostedEvent;
import edu.cuny.cat.event.TransactionPostedEvent;
import edu.cuny.cat.market.DuplicateShoutException;
import edu.cuny.cat.market.EquilibriumCalculator;
import edu.cuny.cat.market.core.SpecialistInfo;
import edu.cuny.cat.market.core.TraderInfo;
import edu.cuny.cat.market.matching.FourHeapShoutEngine;
import edu.cuny.util.Utils;

/**
 * A registry for a market client, which extends {@link ClientRegistry}, and
 * tracks and provides additional information for decision making in the market.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.88 $
 * 
 */

public class MarketRegistry extends ClientRegistry {

	static Logger logger = Logger.getLogger(MarketRegistry.class);

	/**
	 * active specialist on the current day
	 */
	protected Set<String> activeSpecialistIdSet;

	/**
	 * estimate of the entitlement of a trader each day
	 */
	protected int entitlement;

	protected FourHeapShoutEngine globalShoutEngine;

	protected double glCEMaxPrice;

	protected double glCEMinPrice;

	protected double glCEMidPrice;

	protected Map<String, Double> specialistBalances;

	protected int numOfActiveTraders;

	/**
	 * number of traders that are traced on the current day
	 */
	protected int numOfTracedTraders;

	/**
	 * number of traders changing market within the last two consecutive days
	 */
	protected int numOfMobileTraders;

	/**
	 * number of traders staying in a same market in the last two consecutive days
	 */
	protected int numOfStationaryTraders;

	public MarketRegistry() {
		activeSpecialistIdSet = Collections.synchronizedSet(new HashSet<String>());
		specialistBalances = Collections
				.synchronizedMap(new HashMap<String, Double>());
		globalShoutEngine = new FourHeapShoutEngine();

		glCEMaxPrice = glCEMinPrice = glCEMidPrice = Double.NaN;
	}

	@Override
	public Trader addTrader(final String id, final String desc,
			final boolean isSeller) {
		final TraderInfo traderInfo = new TraderInfo(id, desc, isSeller);
		traders.put(id, traderInfo);
		return traderInfo;
	}

	@Override
	public Specialist addSpecialist(final String id) {
		final SpecialistInfo specialistInfo = new SpecialistInfo(id);
		specialists.put(id, specialistInfo);
		return specialistInfo;
	}

	public TraderInfo getTraderInfo(String id) {
		return (TraderInfo) traders.get(id);
	}

	public SpecialistInfo getSpecialistInfo(String id) {
		return (SpecialistInfo) specialists.get(id);
	}

	public SpecialistInfo getMyInfo() {
		return getSpecialistInfo(id);
	}

	@Override
	public void eventOccurred(AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof GameStartedEvent) {
			processGameStarted((GameStartedEvent) event);
		} else if (event instanceof FeesAnnouncedEvent) {
			processFeesAnnounced((FeesAnnouncedEvent) event);
		} else if (event instanceof RegisteredTradersAnnouncedEvent) {
			processRegisteredTraders((RegisteredTradersAnnouncedEvent) event);
		} else if (event instanceof DayClosedEvent) {
			processDayClosed((DayClosedEvent) event);
		}
	}

	@Override
	protected void processGameStarting(GameStartingEvent event) {
		super.processGameStarting(event);

		entitlement = 1;
	}

	protected void processGameStarted(GameStartedEvent event) {
		for (final String specialistId : specialists.keySet()) {
			specialistBalances.put(specialistId, new Double(0));
		}
	}

	@Override
	protected void processDayOpening(DayOpeningEvent event) {
		super.processDayOpening(event);

		activeSpecialistIdSet.clear();

		numOfActiveTraders = 0;
		numOfMobileTraders = numOfStationaryTraders = 0;

		globalShoutEngine.reset();

		for (final Trader trader : traders.values()) {
			((TraderInfo) trader).dayOpening();
		}

		for (final Specialist specialist : specialists.values()) {
			((SpecialistInfo) specialist).dayOpening();
		}
	}

	protected void processFeesAnnounced(FeesAnnouncedEvent event) {
		activeSpecialistIdSet.add(event.getSpecialist().getId());
	}

	@Override
	protected void processRegistration(RegistrationEvent event) {
		if (!event.getSpecialistId().equalsIgnoreCase(id)) {
			MarketRegistry.logger
					.error("Unexpected registration event for other specialists received !");
		}

		super.processRegistration(event);
	}

	public void processRegisteredTraders(RegisteredTradersAnnouncedEvent event) {
		final Specialist specialist = event.getSpecialist();
		final SpecialistInfo spInfo = (SpecialistInfo) specialist;
		spInfo.addNumOfTraders(event.getNumOfTraders());
		numOfActiveTraders += event.getNumOfTraders();
	}

	protected void processDayClosed(DayClosedEvent event) {
		double DailySpecialistsProfit = 0.0;
		for (final String specialistId : activeSpecialistIdSet) {
			final SpecialistInfo specialistInfo = getSpecialistInfo(specialistId);
			if (specialistBalances.containsKey(specialistId)) {
				// no previous info, just leave this specialist out

				specialistInfo.setDailyProfit(specialistInfo.getAccount().getBalance()
						- specialistBalances.get(specialistId).doubleValue());
				DailySpecialistsProfit += specialistInfo.getDailyProfit();
			}
			specialistBalances.put(specialistId, new Double(specialistInfo
					.getAccount().getBalance()));
		}

		for (final String specialistId : activeSpecialistIdSet) {
			final SpecialistInfo specialistInfo = getSpecialistInfo(specialistId);
			specialistInfo.calculateDailyScore(numOfActiveTraders,
					DailySpecialistsProfit);
		}

		TraderInfo traderInfo = null;
		for (final String traderId : traders.keySet()) {
			traderInfo = getTraderInfo(traderId);
			traderInfo.dayClosed();

			// update the maximul entitlement globally
			if (traderInfo.getEntitlement() > entitlement) {
				entitlement = traderInfo.getEntitlement();
			}
		}

		calculateGlobalEquilibrium();
		updateTraderStatus();
	}

	private void calculateGlobalEquilibrium() {

		globalShoutEngine.reset();
		numOfTracedTraders = 0;

		TraderInfo traderInfo = null;
		for (final String traderId : traders.keySet()) {
			traderInfo = getTraderInfo(traderId);

			if (traderInfo.isTraced()) {
				// only traders that are traced will be counted
				numOfTracedTraders++;

				final int quantity = traderInfo.getEntitlement();
				final double value = traderInfo.getPrivateValue();
				final boolean isBid = !traderInfo.isSeller();
				final Shout shout = new Shout(quantity, value, isBid);
				shout.setTrader(traderInfo);
				try {
					globalShoutEngine.newShout(shout);
				} catch (final DuplicateShoutException e) {
					e.printStackTrace();
				}

			}
		}

		final EquilibriumCalculator glCECalculator = new EquilibriumCalculator(
				globalShoutEngine);
		glCEMidPrice = glCECalculator.getMidEquilibriumPrice();
		glCEMinPrice = glCECalculator.getMinEquilibriumPrice();
		glCEMaxPrice = glCECalculator.getMaxEquilibriumPrice();
	}

	protected void updateTraderStatus() {

		TraderInfo traderInfo = null;
		for (final String traderId : traders.keySet()) {
			traderInfo = getTraderInfo(traderId);

			if (traderInfo.isTraced()) {
				if (traderInfo.isSeller()) {
					if (traderInfo.getPrivateValue() <= glCEMaxPrice) {
						traderInfo.setMarginalStatus(TraderInfo.INTRA_MARGINAL);
					}
				} else {
					if (traderInfo.getPrivateValue() >= glCEMinPrice) {
						traderInfo.setMarginalStatus(TraderInfo.INTRA_MARGINAL);
					}
				}

				final SpecialistInfo specialistInfo = getSpecialistInfo(traderInfo
						.getSpecialistId());
				if (specialistInfo != null) {
					if (traderInfo.isSeller()) {
						specialistInfo.addSupply(traderInfo.getEntitlement());
					} else {
						specialistInfo.addDemand(traderInfo.getEntitlement());
					}

					if (traderInfo.isStationary()) {
						numOfStationaryTraders++;
					} else {
						numOfMobileTraders++;
					}
				}
			}
		}
	}

	@Override
	protected void processShoutPlaced(ShoutPlacedEvent event) {
		final Shout shout = event.getShout();
		if (shout.getSpecialist() == null) {
			MarketRegistry.logger.error(id
					+ " has a shout placed with no specialist info !");
		} else if (!id.equalsIgnoreCase(shout.getSpecialist().getId())) {
			MarketRegistry.logger.fatal("Placed shout at " + id
					+ " is associated with another specialist "
					+ shout.getSpecialist().getId() + " !");
		}

		super.processShoutPlaced(event);
	}

	@Override
	protected void processShoutPosted(ShoutPostedEvent event) {
		super.processShoutPosted(event);

		final Shout shout = event.getShout();
		final TraderInfo traderInfo = getTraderInfo(shout.getTrader().getId());
		final SpecialistInfo specialistInfo = getSpecialistInfo(shout
				.getSpecialist().getId());

		if (specialistInfo.getTrader(traderInfo.getId()) == null) {
			// use the posted shouts info to track registeration of traders with
			// specialists
			registerTrader(traderInfo, specialistInfo);
		}

		traderInfo.shoutPlaced(shout.getPrice());
		specialistInfo.shoutPosted(shout);

		traderInfo.updateTrace(day);
	}

	@Override
	protected void processTransactionPosted(TransactionPostedEvent event) {
		super.processTransactionPosted(event);

		final Transaction transaction = event.getTransaction();
		final Shout ask = transaction.getAsk();
		final Shout bid = transaction.getBid();

		final TraderInfo sellerInfo = getTraderInfo(ask.getTrader().getId());
		final TraderInfo buyerInfo = getTraderInfo(bid.getTrader().getId());
		buyerInfo.increaseGoodsTraded(transaction.getQuantity());
		sellerInfo.increaseGoodsTraded(transaction.getQuantity());

		final SpecialistInfo specialistInfo = getSpecialistInfo(transaction
				.getSpecialist().getId());
		specialistInfo.transactionPosted(transaction);
	}

	public double getGlCEMinPrice() {
		return glCEMinPrice;
	}

	public double getGlCEMaxPrice() {
		return glCEMaxPrice;
	}

	public double getGlCEMidPrice() {
		return glCEMidPrice;
	}

	public int getTraderEntitlement() {
		return entitlement;
	}

	public String[] getActiveOpponentIds() {
		final String[] list = new String[activeSpecialistIdSet.size() - 1];

		int i = 0;
		for (final String specialistId : activeSpecialistIdSet) {
			if (!id.equalsIgnoreCase(specialistId)) {
				list[i++] = specialistId;
			}
		}

		Arrays.sort(list);

		return list;
	}

	public int getNumOfTracedTraders() {
		return numOfTracedTraders;
	}

	@Override
	public void printStatus() {
		MarketRegistry.logger.info("\n");
		MarketRegistry.logger.info("Registry Status at " + id);
		MarketRegistry.logger.info("------------------------------");

		MarketRegistry.logger.info("Scores on Day " + day);
		for (final String specialistId : specialists.keySet()) {
			final SpecialistInfo specialistInfo = getSpecialistInfo(specialistId);
			MarketRegistry.logger.info(Utils.indent(Utils.align(specialistInfo
					.getId())
					+ "  "
					+ specialistInfo.getDailyScore().toString()
					+ "  P:"
					+ Utils.format(specialistInfo.getAccount().getBalance())
					+ " T:"
					+ specialistInfo.getNumOfTraders()
					+ " (R:"
					+ specialistInfo.getTraderMap().size()
					+ ")"
					+ "  shout:"
					+ specialistInfo.getShouts().size()
					+ " trans:"
					+ specialistInfo.getTransactions().size()));
		}

		MarketRegistry.logger.info("Global equilibrium");
		MarketRegistry.logger.info(Utils.indent("\tmin:"
				+ Utils.format(glCEMinPrice) + " mid:" + Utils.format(glCEMidPrice)
				+ " max:" + Utils.format(glCEMaxPrice)));
		MarketRegistry.logger.info("Traders");
		MarketRegistry.logger.info(Utils.indent("\ttraced:" + numOfTracedTraders
				+ " stationary:" + numOfStationaryTraders + " mobile:"
				+ numOfMobileTraders));
		MarketRegistry.logger.info("\n");
	}
}
