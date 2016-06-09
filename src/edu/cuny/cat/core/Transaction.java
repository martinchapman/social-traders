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

package edu.cuny.cat.core;

import edu.cuny.util.Utils;

/**
 * This class records a match between an ask and a bid.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.12 $
 */

public class Transaction implements Cloneable {

	/**
	 * The identity allocated to this transaction.
	 */
	protected String id;

	protected Specialist specialist;

	/**
	 * The offers that led to this transaction.
	 */
	protected Shout ask;

	/**
	 * The offers that led to this transaction.
	 */
	protected Shout bid;

	/**
	 * The price at which the good was sold for.
	 */
	protected double price;

	public Transaction(final Shout ask, final Shout bid, final double price) {
		this(null, ask, bid, price);
	}

	public Transaction(final String id, final Shout ask, final Shout bid,
			final double price) {
		this.id = id;
		this.ask = ask;
		this.bid = bid;
		this.price = price;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Transaction transaction = null;
		transaction = (Transaction) super.clone();
		if (transaction.specialist != null) {
			transaction.specialist = (Specialist) transaction.specialist.clone();
		}
		if (transaction.ask != null) {
			transaction.ask = (Shout) transaction.ask.clone();
		}
		if (transaction.bid != null) {
			transaction.bid = (Shout) transaction.bid.clone();
		}

		return transaction;
	}

	/**
	 * compares whether this transaction equals another one. If the two asks
	 * equals and the two bids equals, the two transactions are considered equal.
	 * 
	 * @param anotherTrans
	 *          the transaction to compare
	 * @return true if equal; false otherwise
	 */
	public boolean equals(final Transaction anotherTrans) {
		if (ask.equals(anotherTrans.getAsk()) && bid.equals(anotherTrans.getBid())) {
			return true;
		}
		return false;
	}

	public String getId() {
		return id;
	}

	public Specialist getSpecialist() {
		return specialist;
	}

	public Shout getAsk() {
		return ask;
	}

	public Shout getBid() {
		return bid;
	}

	public double getPrice() {
		return price;
	}

	public void setAsk(final Shout ask) {
		this.ask = ask;
	}

	public void setBid(final Shout bid) {
		this.bid = bid;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setSpecialist(final Specialist specialist) {
		this.specialist = specialist;
	}

	public void setPrice(final double price) {
		this.price = price;
	}

	public int getQuantity() {
		return ask.getQuantity();
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();
		s += ": " + ask.getId() + "@" + Utils.format(ask.getPrice()) + " "
				+ bid.getId() + "@" + Utils.format(bid.getPrice());
		return s;
	}

}
