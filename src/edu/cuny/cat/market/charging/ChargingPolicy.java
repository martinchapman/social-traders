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

package edu.cuny.cat.market.charging;

import edu.cuny.cat.market.AuctioneerPolicy;

/**
 * <p>
 * A charging policy determines how much a market charges.
 * </p>
 * 
 * <p>
 * Currently, 5 different types of charges are supported, flat charge on
 * registration (participation in a market), flat charge on information on
 * market activities, flat charge on shouts, flat charge on transactions, and
 * fractional charge on transaction profit.
 * </p>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>charging</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.23 $
 * 
 */
public abstract class ChargingPolicy extends AuctioneerPolicy {

	public static final String P_DEF_BASE = "charging";

	public static final String P_FEES[] = new String[] { "registration",
			"information", "shout", "transaction", "profit" };

	/**
	 * index of registration fee in {@link #P_FEES}.
	 */
	public static final int REGISTRATION_INDEX = 0;

	/**
	 * index of information fee in {@link #P_FEES}.
	 */
	public static final int INFORMATION_INDEX = 1;

	/**
	 * index of shout fee in {@link #P_FEES}.
	 */
	public static final int SHOUT_INDEX = 2;

	/**
	 * index of transaction fee in {@link #P_FEES}.
	 */
	public static final int TRANSACTION_INDEX = 3;

	/**
	 * index of profit fee in {@link #P_FEES}.
	 */
	public static final int PROFIT_INDEX = 4;

	/**
	 * type of fee charged flatly
	 */
	public static final int FLAT = 0;

	/**
	 * type of fee charged at a fraction of another value
	 */
	public static final int FRACTIONAL = 1;

	/**
	 * maximum values for different types of fees
	 */
	public static final double MAXES[] = { 1000D, 1.0D };

	/**
	 * minimum values for different types of fees
	 */
	public static final double MINES[] = { 0.0D, 0.0D };

	/**
	 * types of the fees a specialist may charge, being {@link #FLAT} or
	 * {@link #FRACTIONAL}
	 */
	public static final int FEE_TYPES[] = new int[] { ChargingPolicy.FLAT,
			ChargingPolicy.FLAT, ChargingPolicy.FLAT, ChargingPolicy.FLAT,
			ChargingPolicy.FRACTIONAL };

	/**
	 * current charges
	 */
	protected double fees[] = new double[ChargingPolicy.P_FEES.length];

	public double[] getFees() {
		return fees;
	}

	public double getRegistrationFee() {
		return fees[0];
	}

	public double getInformationFee() {
		return fees[1];
	}

	public double getShoutFee() {
		return fees[2];
	}

	public double getTransactionFee() {
		return fees[3];
	}

	public double getProfitFee() {
		return fees[4];
	}

	public static boolean adjustFees(final double fees[]) {
		boolean adjusted = false;
		for (int i = 0; i < fees.length; i++) {
			if (Double.isNaN(fees[i])) {
				fees[i] = 0;
				adjusted = true;
			} else if (fees[i] < ChargingPolicy.MINES[ChargingPolicy.FEE_TYPES[i]]) {
				fees[i] = ChargingPolicy.MINES[ChargingPolicy.FEE_TYPES[i]];
				adjusted = true;
			} else if (fees[i] > ChargingPolicy.MAXES[ChargingPolicy.FEE_TYPES[i]]) {
				fees[i] = ChargingPolicy.MAXES[ChargingPolicy.FEE_TYPES[i]];
				adjusted = true;
			}
		}
		return adjusted;
	}

	@Override
	public String toString() {
		String s = super.toString();
		for (int i = 0; i < fees.length; i++) {
			s += " " + ChargingPolicy.P_FEES[i] + ":" + fees[i];
		}

		return s;
	}
}