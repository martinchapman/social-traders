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

import edu.cuny.util.Resetable;

/**
 * A moneytary account that can be owned by a trader or specialist.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.14 $
 */
public class Account implements Resetable {

	public final static String TRANSACTION_FEE = "transaction";

	public final static String REGISTRATION_FEE = "registration";

	public final static String SHOUT_FEE = "shout";

	public final static String INFORMATION_FEE = "info";

	public final static String PROFIT_FEE = "profit";

	public final static String GOODS = "goods";

	public final static String ASSETS = "assets";

	public final static String EXPENSE = "expense";

	public final static String INCOME = "income";

	protected double balance;

	// protected Vector log;

	public Account() {
		// log = new Vector();
		init0();
	}

	private void init0() {
		balance = 0.0d;
	}

	public void reset() {
		init0();
		// log.clear();
	}

	public void receiveFund(final String type, final String payerId,
			final double amount) {
		balance += amount;
		// TODO: disable to save space
		// log.add(amount + " <--- " + payerId + " (" + type + ")");
	}

	/**
	 * transfers fund up to the amount available in the account or the requested
	 * amount, whichever is smaller.
	 * 
	 * @param type
	 *          describes the type of this transfer
	 * @param receiverId
	 * @param amount
	 *          the amount requested
	 * @return the amount actually transferred
	 */
	public double payFundAvailable(final String type, final String receiverId,
			final double amount) {
		final double transferAmount = Math.min(balance, amount);
		payFund(type, receiverId, transferAmount);
		return transferAmount;
	}

	public void payFund(final String type, final String receiverId,
			final double amount) {
		balance -= amount;
		// TODO: disable to save space
		// log.add(amount + " ---> " + receiverId + " (" + type + ")");
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(final double balance) {
		this.balance = balance;
	}

	// public Enumeration getLog() {
	// return log.elements();
	// }
}
