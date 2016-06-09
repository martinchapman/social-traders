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

package edu.cuny.cat.market.accepting;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.IllegalShoutException;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;

/**
 * the accepting policy that tracks matched asks and bids and uses lowest
 * matched bid and highest matched ask to restrict the shouts to be accepted.
 * This aims to increase the transation success rate.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public class TransactionBasedAcceptingPolicy extends
		OnlyNewShoutDecidingAcceptingPolicy {

	static Logger logger = Logger
			.getLogger(TransactionBasedAcceptingPolicy.class);

	protected double minDailyAcceptedBid;

	protected double maxDailyAcceptedAsk;

	protected double lowestBidPrice;

	protected double highestAskPrice;

	/**
	 * used to relax the restriction
	 */
	protected double delta = 5;

	public TransactionBasedAcceptingPolicy() {
		init0();
	}

	private void init0() {
		lowestBidPrice = Double.NEGATIVE_INFINITY;
		highestAskPrice = Double.POSITIVE_INFINITY;

		minDailyAcceptedBid = Double.POSITIVE_INFINITY;
		maxDailyAcceptedAsk = Double.NEGATIVE_INFINITY;
	}

	@Override
	public void reset() {
		init0();
	}

	/**
	 * accepts all shouts and {@link IllegalShoutException} is never thrown.
	 * 
	 * @see edu.cuny.cat.market.accepting.OnlyNewShoutDecidingAcceptingPolicy#check(edu.cuny.cat.core.Shout)
	 */
	@Override
	public void check(final Shout shout) throws IllegalShoutException {
		if (shout.isAsk()) {
			if (shout.getPrice() > highestAskPrice + delta) {
				// logger.info("too high ask than " + (highestAskPrice + delta));
				throw new IllegalShoutException("too high ask price !");
			}
		} else {
			if (shout.getPrice() < lowestBidPrice - delta) {
				// logger.info("too low bid than " + (lowestBidPrice - delta));
				throw new IllegalShoutException("too low bid price !");
			}
		}
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof TransactionExecutedEvent) {
			final Transaction transaction = ((TransactionExecutedEvent) event)
					.getTransaction();
			if ((transaction.getSpecialist() != null)
					&& transaction.getSpecialist().getId().equalsIgnoreCase(
							getAuctioneer().getName())) {
				if (transaction.getAsk().getPrice() > maxDailyAcceptedAsk) {
					maxDailyAcceptedAsk = transaction.getAsk().getPrice();
					// logger.info("max accepted ask: " + maxDailyAcceptedAsk);
				}

				if (transaction.getBid().getPrice() < minDailyAcceptedBid) {
					minDailyAcceptedBid = transaction.getBid().getPrice();
					// logger.info("min accepted bid: " + minDailyAcceptedBid);
				}

			}
		} else if (event instanceof DayClosedEvent) {
			// TODO: if there is no transaction, all restriction set below will be too
			// tight

			// logger.info(lowestBidPrice + " \t " + highestAskPrice);

			if (minDailyAcceptedBid == Double.POSITIVE_INFINITY) {
				lowestBidPrice = Double.NEGATIVE_INFINITY;
			} else {
				if (Double.isInfinite(lowestBidPrice)) {
					lowestBidPrice = minDailyAcceptedBid;
				} else {
					lowestBidPrice = (lowestBidPrice + minDailyAcceptedBid) / 2;
				}
			}

			if (maxDailyAcceptedAsk == Double.NEGATIVE_INFINITY) {
				highestAskPrice = Double.POSITIVE_INFINITY;
			} else {
				if (Double.isInfinite(highestAskPrice)) {
					highestAskPrice = maxDailyAcceptedAsk;
				} else {
					highestAskPrice = (highestAskPrice + maxDailyAcceptedAsk) / 2;
				}
			}

			minDailyAcceptedBid = Double.POSITIVE_INFINITY;
			maxDailyAcceptedAsk = Double.NEGATIVE_INFINITY;
		}
	}
}
