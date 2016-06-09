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

package edu.cuny.cat.server;

import edu.cuny.cat.core.IllegalShoutInTransactionException;
import edu.cuny.cat.core.IllegalTransactionException;
import edu.cuny.cat.core.IllegalTransactionPriceException;
import edu.cuny.cat.core.Shout;
import edu.cuny.util.MathUtil;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * A class used by the game server to check the validity of a transaction.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class TransactionValidator implements Parameterizable {

	public void setup(final ParameterDatabase parameters, final Parameter base) {
	}

	/**
	 * checks whether a transaction from a specialist is valid or not. To be
	 * valid, the transaction price must be no lower than the ask price and no
	 * higher than the bid price.
	 * {@link edu.cuny.cat.core.IllegalTransactionException} will be thrown if the
	 * transaction is found illegal.
	 * 
	 * @param ask
	 *          the ask in the transaction request
	 * @param bid
	 *          the bid in the transaction request
	 * @param price
	 *          the transaction price in the transaction request
	 * @throws IllegalTransactionException
	 *           thrown when the transaction is invalid
	 */
	public void check(final Shout ask, final Shout bid, final double price)
			throws IllegalTransactionException {
		if ((ask == null) || (bid == null)) {
			throw new IllegalShoutInTransactionException(
					"Null ask and/or bid in transaction request !");
		} else if ((ask.getState() != Shout.PLACED)
				|| (bid.getState() != Shout.PLACED)) {
			throw new IllegalShoutInTransactionException(
					"Invalid ask and/or bid state in transaction request !");
		} else if (!MathUtil.approxSmaller(ask.getPrice(), price)
				|| !MathUtil.approxBigger(bid.getPrice(), price)) {
			throw new IllegalTransactionPriceException("Invalid transaction price "
					+ price + " for ask " + ask.getPrice() + " and bid " + bid.getPrice()
					+ " !");
		}
	}
}
