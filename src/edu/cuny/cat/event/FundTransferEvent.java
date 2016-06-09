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

package edu.cuny.cat.event;

import edu.cuny.cat.core.AccountHolder;

/**
 * An event that is fired when fund of a certain amount is transfered between
 * two {@link edu.cuny.cat.core.AccountHolder}s.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public class FundTransferEvent extends AuctionEvent {

	AccountHolder payer;

	AccountHolder payee;

	String type;

	double amount;

	public FundTransferEvent(final AccountHolder payer,
			final AccountHolder payee, final String type) {
		this(payer, payee, type, 0);
	}

	public FundTransferEvent(final AccountHolder payer,
			final AccountHolder payee, final String type, final double amount) {
		this.payer = payer;
		this.payee = payee;
		this.type = type;
		this.amount = amount;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(final double amount) {
		this.amount = amount;
	}

	public AccountHolder getPayee() {
		return payee;
	}

	public void setPayee(final AccountHolder payee) {
		this.payee = payee;
	}

	public AccountHolder getPayer() {
		return payer;
	}

	public void setPayer(final AccountHolder payer) {
		this.payer = payer;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}
}
