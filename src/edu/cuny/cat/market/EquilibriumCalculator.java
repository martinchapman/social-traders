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
/*
 * JASA Java Auction Simulator API
 * Copyright (C) 2001-2005 Steve Phelps
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

package edu.cuny.cat.market;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.market.matching.FourHeapShoutEngine;
import edu.cuny.util.CumulativeDistribution;

/**
 * A class calculating equilibrium-related quantity, price, etc. in an auction,
 * based on a given {@link FourHeapShoutEngine} instance.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.10 $
 */

public class EquilibriumCalculator {

	protected FourHeapShoutEngine shoutEngine;

	protected boolean equilibriaFound = false;

	protected List<Shout> matchedShouts;

	protected int quantity;

	/**
	 * The minimum equilibrium price.
	 */
	protected double minPrice;

	/**
	 * The maximum equilibrium price.
	 */
	protected double maxPrice;

	protected CumulativeDistribution askPriceDistribution;

	protected CumulativeDistribution bidPriceDistribution;

	Logger logger = Logger.getLogger(EquilibriumCalculator.class);

	public EquilibriumCalculator(final FourHeapShoutEngine shoutEngine) {
		this.shoutEngine = shoutEngine;

		// DO NOT ALTER THE ORDER OF THE FOLLOWING STATEMENTS
		calculateShoutPriceRanges();
		calculateEquilibria();
	}

	/**
	 * calculates equilibria.
	 */
	public void calculateEquilibria() {
		final Shout hiAsk = shoutEngine.getHighestMatchedAsk();
		final Shout loBid = shoutEngine.getLowestMatchedBid();
		if ((hiAsk == null) || (loBid == null)) {
			equilibriaFound = false;
		} else {
			calculateEquilibriaPriceRange();
			equilibriaFound = true;
			matchedShouts = shoutEngine.getMatchedShouts();
			calculateEquilibriaQuantity();
		}
	}

	/**
	 * 
	 * @return true if an equilibrium exist, or false otherwise.
	 */
	public boolean isEquilibriaFound() {
		return equilibriaFound;
	}

	/**
	 * calculates the price ranges of asks and bids.
	 */
	public void calculateShoutPriceRanges() {
		askPriceDistribution = calculateShoutPriceDistribution(shoutEngine
				.askIterator());
		bidPriceDistribution = calculateShoutPriceDistribution(shoutEngine
				.bidIterator());
	}

	private CumulativeDistribution calculateShoutPriceDistribution(
			final Iterator<Shout> shoutIterator) {
		final CumulativeDistribution dist = new CumulativeDistribution();
		Shout shout = null;
		while (shoutIterator.hasNext()) {
			shout = shoutIterator.next();
			dist.newData(shout.getPrice(), shout.getQuantity());
		}
		return dist;
	}

	/**
	 * calculates the quantity at the equilibrium.
	 */
	protected void calculateEquilibriaQuantity() {
		quantity = 0;
		final Iterator<Shout> i = matchedShouts.iterator();
		while (i.hasNext()) {
			// skip bids
			i.next();

			// just count ask quantity
			final Shout ask = i.next();
			quantity += ask.getQuantity();
		}
	}

	/**
	 * calculates the price range at the equilibrium.
	 */
	protected void calculateEquilibriaPriceRange() {

		minPrice = Shout.maxPrice(shoutEngine.getHighestMatchedAsk(), shoutEngine
				.getHighestUnmatchedBid());

		maxPrice = Shout.minPrice(shoutEngine.getLowestUnmatchedAsk(), shoutEngine
				.getLowestMatchedBid());
	}

	/**
	 * @return the lower bound of the price range at the equilibrium.
	 */
	public double getMinEquilibriumPrice() {
		return minPrice;
	}

	/**
	 * @return the upper bound of the price range at the equilibrium.
	 */
	public double getMaxEquilibriumPrice() {
		return maxPrice;
	}

	/**
	 * 
	 * @return the quantity at the equilibrium.
	 */
	public int getEquilibriumQuantity() {
		return quantity;
	}

	/**
	 * @return whether an equilibrium exists or not.
	 */
	public boolean equilibriaExists() {
		return equilibriaFound;
	}

	/**
	 * @return the mid-point of the equilibrium price range.
	 */
	public double getMidEquilibriumPrice() {
		return (getMinEquilibriumPrice() + getMaxEquilibriumPrice()) / 2;
	}

	/**
	 * @return a list of matched shouts, in the order of b0, a0, b1, a1, ...
	 */
	public List<Shout> getMatchedShouts() {
		return matchedShouts;
	}

	public CumulativeDistribution getAskPriceDistribution() {
		return askPriceDistribution;
	}

	public CumulativeDistribution getBidPriceDistribution() {
		return bidPriceDistribution;
	}
}
