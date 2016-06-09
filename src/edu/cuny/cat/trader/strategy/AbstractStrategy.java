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

import java.io.Serializable;
import java.util.Observable;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.trader.AbstractTradingAgent;
import edu.cuny.util.Resetable;

/**
 * <p>
 * An abstract implementation of the Strategy interface that provides skeleton
 * functionality for making trading decisions.
 * </p>
 * 
 * @author Kai Cai
 * @version $Revision: 1.20 $
 */
public abstract class AbstractStrategy extends Observable implements
		Serializable, Strategy, Resetable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected AbstractTradingAgent agent;

	protected Shout.MutableShout currentShout;

	/**
	 * the smallest price unit in making a shout.
	 */
	public static final double MIN_PRICE_DIFFERENCE = 0.01;

	static Logger logger = Logger.getLogger(AbstractStrategy.class);

	public AbstractStrategy() {
		this(null);
	}

	public AbstractStrategy(final AbstractTradingAgent agent) {
		this.agent = agent;
		init0();
	}

	private void init0() {
		currentShout = new Shout.MutableShout();
	}

	public void initialize() {
		// do nothing
	}

	public void reset() {
		init0();
	}

	public Object protoClone() {
		try {
			final AbstractStrategy copy = (AbstractStrategy) clone();
			copy.reset();
			return copy;
		} catch (final CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	public Shout modifyShout(final Shout shout) {
		if (modifyShout(currentShout)) {
			return new Shout(currentShout.getId(), currentShout.getQuantity(),
					currentShout.getPrice(), currentShout.isBid());
		} else {
			return null;
		}
	}

	/**
	 * Modify the price and quantity of the given shout according to this
	 * strategy.
	 * 
	 * @return false if no shout is to be placed at this time
	 */
	public boolean modifyShout(final Shout.MutableShout shout) {
		shout.setIsBid(agent.isBuyer());
		return true;
	}

	public void eventOccurred(final AuctionEvent event) {
		// do nothing
	}

	public AbstractTradingAgent getAgent() {
		return agent;
	}

	public void setAgent(final AbstractTradingAgent agent) {
		this.agent = agent;
	}

	public boolean requiresAuctionHistory() {
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
