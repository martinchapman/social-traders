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

package edu.cuny.cat.trader.marketselection;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AvailableMarketsAnnouncedEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.FeesAnnouncedEvent;
import edu.cuny.cat.trader.AbstractTradingAgent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Resetable;

/**
 * An abstract market selection strategy, the super class of all the
 * implementations of market selection strategy.
 * 
 * @author Kai Cai
 * @version $Revision: 1.25 $
 * 
 */

public abstract class AbstractMarketSelectionStrategy extends Observable
		implements Parameterizable, Resetable {

	static Logger logger = Logger
			.getLogger(AbstractMarketSelectionStrategy.class);

	/**
	 * the trading agent that plays this strategy
	 */
	protected AbstractTradingAgent agent;

	/**
	 * the complete list of specialists including active and inactive ones
	 */
	protected Specialist[] markets;

	/**
	 * active specialists' indices in {@link #markets} from which this strategy
	 * will choose one daily
	 */
	protected SortedSet<Integer> activeMarkets;

	/**
	 * mapping from specialists to their indices in {@link #markets}
	 */
	protected Map<Specialist, Integer> marketIndices;

	/**
	 * the index of selected specialist in {@link #markets}.
	 */
	protected int currentMarketIndex;

	public AbstractMarketSelectionStrategy() {
		activeMarkets = Collections.synchronizedSortedSet(new TreeSet<Integer>());
		marketIndices = Collections
				.synchronizedMap(new HashMap<Specialist, Integer>());
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		// do nothing
	}

	public void initialize() {
		// do nothing
	}

	public void reset() {
		// markets = null;
		// marketIndices.clear();
		activeMarkets.clear();
		currentMarketIndex = -1;
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof AvailableMarketsAnnouncedEvent) {
			setupMarkets(((AvailableMarketsAnnouncedEvent) event).getMarkets());
		} else if (event instanceof DayOpenedEvent) {
			if (activeMarkets.size() > 0) {
				selectMarket();
			} else {
				AbstractMarketSelectionStrategy.logger
						.debug("No markets are available for " + agent.getTraderId() + " !");
				currentMarketIndex = -1;
			}
			registerMarket();
		} else if (event instanceof DayOpeningEvent) {
			// market all specialists inactive unless their price lists arrive

			activeMarkets.clear();

		} else if (event instanceof FeesAnnouncedEvent) {
			final Specialist specialist = ((FeesAnnouncedEvent) event)
					.getSpecialist();
			addActiveSpecialist(specialist);
		}
	}

	protected void addActiveSpecialist(final Specialist specialist) {
		final Integer index = marketIndices.get(specialist);
		if (index == null) {
			AbstractMarketSelectionStrategy.logger
					.error("Specialist providing the price list doesn't exist in markets available !");
		} else {
			// market the specialist active
			activeMarkets.add(index);
		}
	}

	/**
	 * initiates specialists traders may register with in the game.
	 * 
	 * @param marketColl
	 *          the collection of specialists participating in the game
	 */
	protected void setupMarkets(final Collection<Specialist> marketColl) {
		markets = marketColl.toArray(new Specialist[0]);
		for (int i = 0; i < markets.length; i++) {
			marketIndices.put(markets[i], new Integer(i));
		}
	}

	/**
	 * selects a specialist from the list of active ones in the game, which is
	 * stored in {@link #activeMarkets}.
	 */
	public abstract void selectMarket();

	/**
	 * requests to register with the specialist returned from
	 * {@link #getCurrenMarket()}.
	 */
	public void registerMarket() {
		setChanged();
		notifyObservers(getCurrenMarket());
	}

	/**
	 * @return the specialist selected to register for the current day if one is
	 *         chosen; or null if none selected.
	 */
	public Specialist getCurrenMarket() {
		if (hasValidCurrentMarket()) {
			return markets[currentMarketIndex];
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @return true if a specialist is chosen to register with or false otherwise.
	 */
	public boolean hasValidCurrentMarket() {
		return (currentMarketIndex >= 0) && (currentMarketIndex < markets.length);
	}

	/**
	 * sets up the trading agent that plays this strategy.
	 * 
	 * @param agent
	 */
	public void setAgent(final AbstractTradingAgent agent) {
		this.agent = agent;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
