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

package edu.cuny.cat.trader;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.FeesAnnouncedEvent;
import edu.cuny.cat.event.GameOverEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.IdAssignedEvent;
import edu.cuny.cat.event.PrivateValueAssignedEvent;
import edu.cuny.cat.event.RegistrationEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.event.RoundClosingEvent;
import edu.cuny.cat.event.RoundOpenedEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.cat.event.SubscriptionEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.trader.marketselection.AbstractMarketSelectionStrategy;
import edu.cuny.cat.trader.strategy.AbstractStrategy;
import edu.cuny.util.ParamClassLoadException;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Resetable;

/**
 * An abstract implementation of trading agents that can bid in a market and can
 * choose between markets to maximize their profit.
 * 
 * @author Kai Cai
 * @version $Revision: 1.63 $
 */

public abstract class AbstractTradingAgent extends Observable implements
		AuctionEventListener, Serializable, Parameterizable, Prototypeable,
		Cloneable, Observer, Resetable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected double privateValue = 0;

	/**
	 * Flag indicating whether this trader is a seller or buyer.
	 */
	protected boolean isSeller = false;

	/**
	 * The bidding strategy for this trader. The default strategy is to bid
	 * truthfully for a single unit.
	 */
	protected AbstractStrategy strategy = null;

	protected AbstractMarketSelectionStrategy marketSelectionStrategy = null;

	protected ResettingCondition resettingCondition = null;

	/**
	 * The profit made in the last accepted shout.
	 */
	protected double lastShoutProfit = 0;

	/**
	 * The profit made in the last day.
	 */
	protected double lastDayProfit = 0;

	/**
	 * The total cumulativeProfits to date
	 */
	protected double profits = 0;

	/**
	 * The current shout for this trader.
	 */
	protected Shout currentShout = new Shout();

	protected String traderId;

	protected Map<String, Specialist> availableMarkets;

	protected Set<String> shoutIdList;

	protected double registrationFee = 0;

	protected double informationFee = 0;

	protected double shoutFee = 0;

	protected double transactionFee = 0;

	protected double profitFee = 0;

	static Logger logger = Logger.getLogger(AbstractTradingAgent.class);

	/**
	 * Parameter names used when initialising from parameter db
	 */

	public static final String P_DEF_BASE = "agent";

	public static final String P_IS_SELLER = "isseller";

	public static final String P_STRATEGY = "strategy";

	public static final String P_INITIAL_MARGIN = "initialmargin";

	public static final String P_MARKET_SELECTION_STRATEGY = "marketselectionstrategy";

	public static final String P_RESETTING_CONDITION = "resetting";

	public AbstractTradingAgent() {
		this(0, false);
	}

	public AbstractTradingAgent(final double privateValue, final boolean isSeller) {
		this(privateValue, isSeller, null);
	}

	public AbstractTradingAgent(final double privateValue,
			final boolean isSeller, final AbstractStrategy strategy) {
		this.privateValue = privateValue;
		this.isSeller = isSeller;
		this.strategy = strategy;
		availableMarkets = Collections
				.synchronizedMap(new HashMap<String, Specialist>());
		shoutIdList = Collections.synchronizedSet(new HashSet<String>());
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		final Parameter defBase = new Parameter(AbstractTradingAgent.P_DEF_BASE);

		isSeller = parameters.getBoolean(base
				.push(AbstractTradingAgent.P_IS_SELLER), null, false);

		strategy = parameters.getInstanceForParameter(base
				.push(AbstractTradingAgent.P_STRATEGY), defBase
				.push(AbstractTradingAgent.P_STRATEGY), AbstractStrategy.class);
		strategy.setAgent(this);
		strategy.addObserver(this);
		((Parameterizable) strategy).setup(parameters, base
				.push(AbstractTradingAgent.P_STRATEGY));
		strategy.initialize();

		marketSelectionStrategy = parameters.getInstanceForParameter(base
				.push(AbstractTradingAgent.P_MARKET_SELECTION_STRATEGY), defBase
				.push(AbstractTradingAgent.P_MARKET_SELECTION_STRATEGY),
				AbstractMarketSelectionStrategy.class);
		marketSelectionStrategy.setAgent(this);
		marketSelectionStrategy.addObserver(this);
		((Parameterizable) marketSelectionStrategy).setup(parameters, base
				.push(AbstractTradingAgent.P_MARKET_SELECTION_STRATEGY));
		marketSelectionStrategy.initialize();

		try {
			resettingCondition = parameters.getInstanceForParameter(base
					.push(AbstractTradingAgent.P_RESETTING_CONDITION), defBase
					.push(AbstractTradingAgent.P_RESETTING_CONDITION),
					ResettingCondition.class);
			resettingCondition.setAgent(this);
			resettingCondition.addObserver(this);
			((Parameterizable) resettingCondition).setup(parameters, base
					.push(AbstractTradingAgent.P_RESETTING_CONDITION));
			resettingCondition.initialize();
		} catch (final ParamClassLoadException e) {
			resettingCondition = null;
		}
	}

	public void initialize() {
		init1();
	}

	private void init1() {
		lastShoutProfit = 0;
		lastDayProfit = 0;
		profits = 0;
	}

	public void reset() {
		init1();

		if (strategy != null) {
			strategy.reset();
		}

		if (marketSelectionStrategy != null) {
			marketSelectionStrategy.reset();
		}

		if (resettingCondition != null) {
			resettingCondition.reset();
		}
	}

	public void shoutAccepted(final Shout shout, final double price,
			final int quantity) {

		// logger.info(traderId + " : " + privateValue + " -> " + shout.getPrice() +
		// " -> " + price);
		// logger.info("\n");
		if (isBuyer()) {
			lastShoutProfit = ((privateValue - price) - profitFee
					* (shout.getPrice() - price))
					* quantity - transactionFee;
		} else {
			lastShoutProfit = ((price - privateValue) - profitFee
					* (price - shout.getPrice()))
					* quantity - transactionFee;
		}
		lastDayProfit += lastShoutProfit;
		profits += lastShoutProfit;
	}

	public void requestShout() {

		currentShout = strategy.modifyShout(currentShout);

		if (isActive()) {
			if (currentShout != null) {
				setChanged();
				notifyObservers(currentShout);
			} else {
				AbstractTradingAgent.logger.info(getTraderId()
						+ "! chose not to make a shout.");
			}
		} else {
			AbstractTradingAgent.logger.info(getTraderId()
					+ "! is inactive, no shout is to be made, possibly because they have expended their trade entitlement.");
		}
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof GameStartingEvent) {
			processGameStarting((GameStartingEvent) event);
		} else if (event instanceof GameStartedEvent) {
			processGameStarted((GameStartedEvent) event);
		} else if (event instanceof GameOverEvent) {
			processGameOver((GameOverEvent) event);
		} else if (event instanceof DayOpenedEvent) {
			processDayOpened((DayOpenedEvent) event);
		} else if (event instanceof PrivateValueAssignedEvent) {
			processPrivateValueAssigned((PrivateValueAssignedEvent) event);
		} else if (event instanceof RoundOpenedEvent) {
			processRoundOpened((RoundOpenedEvent) event);
		} else if (event instanceof RoundClosingEvent) {
			processRoundClosing((RoundClosingEvent) event);
		} else if (event instanceof RoundClosedEvent) {
			processRoundClosed((RoundClosedEvent) event);
		} else if (event instanceof DayClosedEvent) {
			processDayClosed((DayClosedEvent) event);
		} else if (event instanceof FeesAnnouncedEvent) {
			processFeesAnnounced((FeesAnnouncedEvent) event);
		} else if (event instanceof RegistrationEvent) {
			processRegistration((RegistrationEvent) event);
		} else if (event instanceof TransactionExecutedEvent) {
			processTransactionExecuted((TransactionExecutedEvent) event);
		} else if (event instanceof SubscriptionEvent) {
			processSubscription((SubscriptionEvent) event);
		} else if (event instanceof ShoutPlacedEvent) {
			processShoutPlaced((ShoutPlacedEvent) event);
		} else if (event instanceof IdAssignedEvent) {
			processIdAssigned((IdAssignedEvent) event);
		}

		strategy.eventOccurred(event);
		marketSelectionStrategy.eventOccurred(event);

		if (resettingCondition != null) {
			resettingCondition.eventOccurred(event);
		}
	}

	protected void processIdAssigned(final IdAssignedEvent event) {
		setTraderId(event.getId());
	}

	public void processFeesAnnounced(final FeesAnnouncedEvent event) {
		final double registrationFee = event.getRegistrationFee();
		final double informationFee = event.getInformationFee();
		final double shoutFee = event.getShoutFee();
		final double transactionFee = event.getTransactionFee();
		final double profitFee = event.getProfitFee();

		final String specialistId = event.getSpecialist().getId();
		Specialist specialist = availableMarkets.get(specialistId);

		if (specialist != null) {
			specialist.setRegistrationFee(registrationFee);
			specialist.setInformationFee(informationFee);
			specialist.setShoutFee(shoutFee);
			specialist.setTransactionFee(transactionFee);
			specialist.setProfitFee(profitFee);
		} else {
			specialist = new Specialist(specialistId, null, registrationFee,
					informationFee, shoutFee, transactionFee, profitFee);
			availableMarkets.put(specialistId, specialist);
		}
	}

	public void processRegistration(final RegistrationEvent event) {
		final Specialist specialist = availableMarkets.get(event.getSpecialistId());

		if (specialist == null) {
			final Exception e = new Exception("Possible bug: "
					+ event.getSpecialistId() + " " + getTraderId()
					+ " entered cannot be found in the available market list !");
			e.printStackTrace();
			AbstractTradingAgent.logger.fatal(e);
			AbstractTradingAgent.logger.fatal(availableMarkets);
			return;
		} else {
			registrationFee = specialist.getRegistrationFee();
			informationFee = specialist.getInformationFee();
			shoutFee = specialist.getShoutFee();
			transactionFee = specialist.getTransactionFee();
			profitFee = specialist.getProfitFee();

			lastDayProfit -= registrationFee;
			profits -= registrationFee;

			if (strategy.requiresAuctionHistory()) {
				// make subscription request

				setChanged();
				notifyObservers(new String[] { specialist.getId() });
			}
		}
	}

	public void processGameStarting(final GameStartingEvent event) {
	}

	public void processGameStarted(final GameStartedEvent event) {
	}

	public void processGameOver(final GameOverEvent event) {
		reset();
	}

	public void processRoundOpened(final RoundOpenedEvent event) {
		if ((marketSelectionStrategy != null)
				&& marketSelectionStrategy.hasValidCurrentMarket()) {
			requestShout();
		}
	}

	protected void processRoundClosing(final RoundClosingEvent event) {
	}

	public void processRoundClosed(final RoundClosedEvent event) {
	}

	public void processDayClosed(final DayClosedEvent event) {
	}

	public void processDayOpened(final DayOpenedEvent event) {
		shoutIdList.clear();
		lastDayProfit = 0;
		lastShoutProfit = 0;

		// TODO: need initialization? In jasa, simply null
		currentShout = new Shout();
	}

	public void processPrivateValueAssigned(final PrivateValueAssignedEvent event) {
		setPrivateValue(event.getPrivateValue());
	}

	public void processTransactionExecuted(final TransactionExecutedEvent event) {
		final Transaction transaction = event.getTransaction();
		if (shoutIdList.contains(transaction.getAsk().getId())) {
			shoutAccepted(transaction.getAsk(), transaction.getPrice(), transaction
					.getQuantity());
		} else if (shoutIdList.contains(transaction.getBid().getId())) {
			shoutAccepted(transaction.getBid(), transaction.getPrice(), transaction
					.getQuantity());
		}
	}

	public void processSubscription(final SubscriptionEvent event) {
		final Specialist subscribedMarket = availableMarkets.get(event
				.getSpecialistId());
		final double informationFee = subscribedMarket.getInformationFee();

		lastDayProfit -= informationFee;
		profits -= informationFee;
	}

	public void processShoutPlaced(final ShoutPlacedEvent event) {
		final Shout shout = event.getShout();

		if (shout.getTrader() == null) {
			// from unknown source, disregard
			return;
		}

		// TODO: now ShoutPlacedEvent delivers only shouts placed by myself
		//
		// only interested in shouts placed by myself
		if (getTraderId().equals(shout.getTrader().getId())) {
			if (shoutIdList.contains(shout.getId())) {
				// do nothing, this shout is a modified one
			} else {
				shoutIdList.add(event.getShout().getId());

				lastDayProfit -= shoutFee;
				profits -= shoutFee;
			}
		}
	}

	public String getTraderId() {
		return traderId;
	}

	public void setPrivateValue(final double privateValue) {
		this.privateValue = privateValue;
	}

	public double getPrivateValue() {
		return privateValue;
	}

	public Shout getCurrentShout() {
		return currentShout;
	}

	public void setIsSeller(final boolean isSeller) {
		this.isSeller = isSeller;
	}

	public boolean isSeller() {
		return isSeller;
	}

	public boolean isBuyer() {
		return !isSeller;
	}

	public void setTraderId(final String traderId) {
		this.traderId = traderId;
	}

	public void setStrategy(final AbstractStrategy strategy) {
		this.strategy = strategy;
		strategy.setAgent(this);
	}

	public AbstractStrategy getStrategy() {
		return strategy;
	}

	public void setMarketSelectionStrategy(
			final AbstractMarketSelectionStrategy marketSelectionStrategy) {
		this.marketSelectionStrategy = marketSelectionStrategy;
		marketSelectionStrategy.setAgent(this);
	}

	public AbstractMarketSelectionStrategy getMarketSelectionStrategy() {
		return marketSelectionStrategy;
	}

	public double getProfits() {
		return profits;
	}

	public void setProfits(final double profits) {
		this.profits = profits;
	}

	public double getLastDayProfit() {
		return lastDayProfit;
	}

	public void setLastDayProfit(final double lastDayProfit) {
		this.lastDayProfit = lastDayProfit;
	}

	public double getLastShoutProfit() {
		return lastShoutProfit;
	}

	public void setLastShoutProfit(final double lastShoutProfit) {
		this.lastShoutProfit = lastShoutProfit;
	}

	// ~MDC
	public abstract void setAdviceTaken(boolean taken);
	
	// ~MDC
	public abstract boolean getAdviceTaken();

	/**
	 * 
	 * @return true if this trading agent is actively trading or false otherwise.
	 */
	public abstract boolean isActive();

	/**
	 * used by various strategies or conditions to notify trader client to
	 * register, subscribe, or reset
	 */
	public void update(final Observable o, final Object arg) {
		if (o instanceof ResettingCondition) {
			reset();
		} else {
			setChanged();
			notifyObservers(arg);
		}
	}
}
