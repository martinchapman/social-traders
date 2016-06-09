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

import edu.cuny.cat.MarketRegistry;
import edu.cuny.cat.core.IllegalShoutException;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.cat.market.accepting.ShoutAcceptingPolicy;
import edu.cuny.cat.market.charging.ChargingPolicy;
import edu.cuny.cat.market.clearing.MarketClearingCondition;
import edu.cuny.cat.market.matching.ShoutEngine;
import edu.cuny.cat.market.pricing.PricingPolicy;
import edu.cuny.cat.market.quoting.QuotingPolicy;
import edu.cuny.cat.market.subscribing.SubscribingPolicy;

/**
 * <p>
 * An interface representing an auctioneer managing shouts in an auction.
 * Different auction rules should be encapsulated in different Auctioneer
 * classes.
 * </p>
 * 
 * <p>
 * An auctioneer is configured with:
 * <ul>
 * <li>{@link ShoutAcceptingPolicy}: regulats what shouts are acceptable;</li>
 * <li>{@link MarketClearingCondition}: defines when the market is cleared,
 * trying to match asks and bids;</li>
 * <li>{@link PricingPolicy}: determines prices in transactions between matching
 * asks and bids;</li>
 * <li>{@link ChargingPolicy}: tells how much the auctioneer charges;</li>
 * </ul>
 * </p>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.13 $
 */

public interface Auctioneer extends QuoteProvider, AuctionEventListener {

	/**
	 * Perform the clearing operation for the auction; match buyers with sellers
	 * and inform the auction of any deals.
	 */
	public void clear();

	/**
	 * Code for handling a new shout in the auction. Subclasses should override
	 * this method if they wish to provide different handling for different
	 * auction rules.
	 * 
	 * @param shout
	 *          The new shout to be processed
	 * 
	 * @exception IllegalShoutException
	 *              Thrown if the shout is invalid in some way.
	 */
	public void newShout(Shout shout) throws IllegalShoutException;

	/**
	 * Handle a request to retract a shout.
	 */
	public void removeShout(Shout shout);

	/**
	 * 
	 * @param shoutId
	 *          the id of desired shout.
	 * 
	 * @return the shout with the id
	 */
	public Shout getShout(String shoutId);

	/**
	 * Log the current status of the auction.
	 */
	public void printState();

	/**
	 * @return an iteration of asks in the auction.
	 */
	public Iterator<Shout> askIterator();

	/**
	 * @return an iteration of bids in the auction.
	 */
	public Iterator<Shout> bidIterator();

	/**
	 * @return the ask market quote.
	 */
	public double askQuote();

	/**
	 * @return the bid market quote.
	 */
	public double bidQuote();

	/**
	 * 
	 * @return the shout engine used by the auctioneer.
	 */
	public ShoutEngine getShoutEngine();

	/**
	 * @return the quoting policy used in the auction.
	 */
	public QuotingPolicy getQuotingPolicy();

	/**
	 * @return the charging policy used in the auction.
	 */
	public ChargingPolicy getChargingPolicy();

	/**
	 * @return the clearning condition used in the auction.
	 */
	public MarketClearingCondition getClearingCondition();

	/**
	 * @return the pricing policy used in the auction.
	 */
	public PricingPolicy getPricingPolicy();

	/**
	 * @return the shout accepting policy in the auction.
	 */
	public ShoutAcceptingPolicy getAcceptingPolicy();

	/**
	 * @return the subscribing policy in the auction.
	 */
	public SubscribingPolicy getSubscribingPolicy();

	/**
	 * @return the registry used by the specialist.
	 */
	public MarketRegistry getRegistry();

	/**
	 * @return the name of the auction.
	 */
	public String getName();

	/**
	 * @param name
	 *          the name for the auction.
	 */
	public void setName(String name);

}