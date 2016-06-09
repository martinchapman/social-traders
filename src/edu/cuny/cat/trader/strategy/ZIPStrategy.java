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
import edu.cuny.cat.trader.AbstractTradingAgent;
import edu.cuny.util.Prototypeable;

/**
 * <p>
 * An implementation of the Zero-Intelligence-Plus (ZIP) strategy. See:
 * </p>
 * 
 * <p>
 *"Minimal Intelligence Agents for Bargaining Behaviours in Market-based
 * Environments" Dave Cliff 1997.
 * </p>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.13 $
 */

public class ZIPStrategy extends MomentumStrategy implements Prototypeable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(ZIPStrategy.class);

	public ZIPStrategy() {
		this(null);
	}

	public ZIPStrategy(final AbstractTradingAgent agent) {
		super(agent);
	}

	@Override
	public Object protoClone() {
		final ZIPStrategy clone = new ZIPStrategy();
		clone.scaling = scaling;
		clone.learner = (MimicryLearner) learner.protoClone();
		clone.reset();
		return clone;
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
			} else if (agent.isActive() && lastShout.isBid()) {
				adjustMargin(targetMargin(lastShoutPrice - perterb(lastShoutPrice)));
			}
		} else {
			if (agent.isActive() && lastShout.isAsk()
					&& (currentPrice >= lastShoutPrice)) {
				adjustMargin(targetMargin(lastShoutPrice - perterb(lastShoutPrice)));
			}
		}
	}

	protected void buyerStrategy() {
		if (lastShoutAccepted) {
			if (currentPrice >= lastShoutPrice) {
				adjustMargin(targetMargin(lastShoutPrice - perterb(lastShoutPrice)));
			} else if (agent.isActive() && lastShout.isAsk()) {
				adjustMargin(targetMargin(lastShoutPrice + perterb(lastShoutPrice)));
			}
		} else {
			if (agent.isActive() && lastShout.isBid()
					&& (currentPrice <= lastShoutPrice)) {
				adjustMargin(targetMargin(lastShoutPrice + perterb(lastShoutPrice)));
			}
		}
	}
}