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

import java.io.Serializable;

import edu.cuny.cat.core.Shout;

/**
 * A price quote summarising the current status of an auction.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.9 $
 */

public class MarketQuote implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The current ask-quote. Buyers need to beat this in order for their offers
	 * to get matched.
	 */
	protected double ask;

	/**
	 * The current bid-quote. Sellers need to ask less than this in order for
	 * their offers to get matched.
	 */
	protected double bid;

	public MarketQuote(final double ask, final double bid) {
		this.ask = ask;
		this.bid = bid;
	}

	public MarketQuote(final Shout ask, final Shout bid) {
		if (ask == null) {
			this.ask = Double.MAX_VALUE;
		} else {
			this.ask = ask.getPrice();
		}
		if (bid == null) {
			this.bid = 0;
		} else {
			this.bid = bid.getPrice();
		}
	}

	public void setAsk(final double ask) {
		this.ask = ask;
	}

	public void setBid(final double bid) {
		this.bid = bid;
	}

	public double getAsk() {
		return ask;
	}

	public double getBid() {
		return bid;
	}

	public double getMid() {
		return (ask + bid) / 2;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " bid:" + bid + " ask:" + ask;
	}
}