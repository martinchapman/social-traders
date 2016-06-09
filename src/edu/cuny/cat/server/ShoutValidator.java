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

import edu.cuny.cat.core.IllegalShoutException;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Trader;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * A class used by the game server to check the validity of a shout.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.maxprice</tt><br>
 * <font size=-1>double (1000 by default)</font></td>
 * <td valign=top>(the upper bound of the price of a shout)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.minprice</tt><br>
 * <font size=-1>double (0 by default)</font></td>
 * <td valign=top>(the lower bound of the price of a shout)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>shout</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class ShoutValidator implements Parameterizable {

	public static final String P_MAXPRICE = "maxprice";

	public static final String P_MINPRICE = "minprice";

	public static final String P_DEF_BASE = "shout";

	/**
	 * the maximal price a shout may offer
	 */
	protected double maxPrice = 1000;

	/**
	 * the minimal price a shout may offer
	 */
	protected double minPrice = 0;

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(ShoutValidator.P_DEF_BASE);

		maxPrice = parameters.getDoubleWithDefault(base
				.push(ShoutValidator.P_MAXPRICE), defBase
				.push(ShoutValidator.P_MAXPRICE), maxPrice);
		minPrice = parameters.getDoubleWithDefault(base
				.push(ShoutValidator.P_MINPRICE), defBase
				.push(ShoutValidator.P_MINPRICE), minPrice);
	}

	/**
	 * checks whether a shout from a trader is valid or not. To be valid, the
	 * price must be not money-losing and falls in the valid price range, i.e.
	 * between {@link #maxPrice} and {@link #minPrice}.
	 * {@link edu.cuny.cat.core.IllegalShoutException} will be thrown if the shout
	 * is found illegal.
	 * 
	 * @param shout
	 *          the shout to check its validity
	 * @throws IllegalShoutException
	 *           thrown when the shout is invalid
	 * 
	 * @see #check(boolean, double, double)
	 */
	public void check(final Shout shout) throws IllegalShoutException {
		final Trader trader = shout.getTrader();
		if (trader == null) {
			throw new IllegalShoutException("Shout without owner !");
		} else {
			check(trader.isSeller(), shout.getPrice(), trader.getPrivateValue());
		}
	}

	/**
	 * checks whether a shout from a trader is valid or not. To be valid, the
	 * price must be not money-losing and falls in the valid price range, i.e.
	 * between {@link #maxPrice} and {@link #minPrice}.
	 * {@link edu.cuny.cat.core.IllegalShoutException} will be thrown if the shout
	 * is found illegal.
	 * 
	 * @param isSeller
	 *          true if the trader is a seller; false otherwise.
	 * @param price
	 *          the price of the shout
	 * @param privateValue
	 *          the private value of the trader
	 * @throws IllegalShoutException
	 *           thrown when the shout is invalid
	 * 
	 * @see #check(Shout)
	 */
	public void check(final boolean isSeller, final double price,
			final double privateValue) throws IllegalShoutException {

		if (Double.isNaN(price) || Double.isInfinite(price)) {
			throw new IllegalShoutException("shout price cannot be NaN or Infinity !");
		}

		if ((isSeller && (price < privateValue))
				|| (!isSeller && (price > privateValue))) {
			throw new IllegalShoutException(
					"Trader should make only not-losing-money shouts !");
		} else if (price > maxPrice) {
			throw new IllegalShoutException("shout price over system limit: "
					+ maxPrice + " !");
		} else if (price < minPrice) {
			throw new IllegalShoutException("shout price below system limit: "
					+ minPrice + " !");
		}
	}
}
