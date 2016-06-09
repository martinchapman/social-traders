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

package edu.cuny.cat.trader.strategy;

import org.apache.log4j.Logger;

import edu.cuny.ai.learning.MimicryLearner;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.ShoutPostedEvent;
import edu.cuny.cat.event.TransactionPostedEvent;
import edu.cuny.cat.trader.AbstractTradingAgent;
import edu.cuny.util.Prototypeable;

/**
 * <p>
 * A simplified implementation of the Zero-Intelligence-Plus (ZIP) strategy,
 * which works well in CDAs. It is named ZIQ because it follows ZIP and keeps
 * trying to beat the market quotes. See:
 * </p>
 * 
 * <p>
 *"Minimal Intelligence Agents for Bargaining Behaviours in Market-based
 * Environments" Dave Cliff 1997.
 * </p>
 * 
 * @see ZIPStrategy
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class ZIQStrategy extends MomentumStrategy implements Prototypeable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(ZIQStrategy.class);

	public ZIQStrategy() {
		this(null);
	}

	public ZIQStrategy(final AbstractTradingAgent agent) {
		super(agent);
	}

	@Override
	public Object protoClone() {
		final ZIQStrategy clone = new ZIQStrategy();
		clone.scaling = scaling;
		clone.learner = (MimicryLearner) learner.protoClone();
		clone.reset();
		return clone;
	}

	@Override
	protected void shoutPosted(final ShoutPostedEvent event) {
		lastShout = event.getShout();
		lastShoutAccepted = false;

		final Trader trader = event.getShout().getTrader();

		if (!trader.getId().equals(agent.getTraderId())) {
			// if not a shout placed by me
			adjustMargin();
		}
	}

	@Override
	protected void transactionPosted(final TransactionPostedEvent event) {
		final Transaction transaction = event.getTransaction();
		lastShoutAccepted = (lastShout.isAsk() && transaction.getAsk().equals(
				lastShout))
				|| (lastShout.isBid() && transaction.getBid().equals(lastShout));
		if (lastShoutAccepted) {
			lastShoutPrice = lastShout.getPrice();
		}
	}

	@Override
	protected void adjustMargin() {

		if (lastShout == null) {
			return;
		}

		if (agent.isSeller()) {
			sellerStrategy();
		} else {
			buyerStrategy();
		}
	}

	protected void sellerStrategy() {
		if (lastShoutAccepted) {
			if (currentPrice <= lastShoutPrice) {
				adjustMargin(targetMargin(lastShoutPrice + perterb(lastShoutPrice)));
			} else if (agent.isActive()) {
				adjustMargin(targetMargin(lastShoutPrice - perterb(lastShoutPrice)));
			}
		} else {
			if (agent.isActive()) {
				adjustMargin(targetMargin(lastShoutPrice - perterb(lastShoutPrice)));
			}
		}
	}

	protected void buyerStrategy() {
		if (lastShoutAccepted) {
			if (currentPrice >= lastShoutPrice) {
				adjustMargin(targetMargin(lastShoutPrice - perterb(lastShoutPrice)));
			} else if (agent.isActive()) {
				adjustMargin(targetMargin(lastShoutPrice + perterb(lastShoutPrice)));
			}
		} else {
			if (agent.isActive()) {
				adjustMargin(targetMargin(lastShoutPrice + perterb(lastShoutPrice)));
			}
		}
	}
}