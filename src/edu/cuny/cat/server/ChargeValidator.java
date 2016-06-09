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

import org.apache.log4j.Logger;

import edu.cuny.cat.core.InvalidChargeException;
import edu.cuny.cat.market.charging.ChargingPolicy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * A class used by the game server to check the validity of the charges from a
 * specialist.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.maxflat</tt><br>
 * <font size=-1>double (1000 by default)</font></td>
 * <td valign=top>(the upper bound of a flat fee charged by a specialist,
 * including registration fee, information fee, shout fee, and transaction fee.)
 * </td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.minflat</tt><br>
 * <font size=-1>double (0 by default)</font></td>
 * <td valign=top>(the lower bound of the a flat fee charged by a specialist)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.maxfractional</tt><br>
 * <font size=-1>double (1 by default)</font></td>
 * <td valign=top>(the upper bound of a fractional fee charged by a specialist,
 * including profit fee.)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.minfractional</tt><br>
 * <font size=-1>double (0 by default)</font></td>
 * <td valign=top>(the lower bound of the a fractional fee charged by a
 * specialist)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.isbanning</tt><br>
 * <font size=-1>boolean (<code>true</code> by default)</font></td>
 * <td valign=top>(whether to ban a specialist on a day when its price list
 * includes invalid values)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>charge</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 */

public class ChargeValidator implements Parameterizable {

	/**
	 * the maximal flat fee a specialist may charge
	 */
	protected double maxFlat;

	/**
	 * the minimal flat fee a specialist may charge
	 */
	protected double minFlat;

	/**
	 * the maximal fractional fee a specialist may charge
	 */
	protected double maxFractional;

	/**
	 * the minimal fractional fee a specialist may charge
	 */
	protected double minFractional;

	/**
	 * whether to ban a specialist when its price list includes invalid values
	 */
	protected boolean isBanning;

	public static final String P_MAXFLAT = "maxflat";

	public static final String P_MINFLAT = "minflat";

	public static final String P_MAXFRACTIONAL = "maxfractional";

	public static final String P_MINFRACTIONAL = "minfractional";

	public static final String P_ISBANNING = "isbanning";

	public static final String P_DEF_BASE = "charge";

	static Logger logger = Logger.getLogger(ChargeValidator.class);

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(ChargeValidator.P_DEF_BASE);

		maxFlat = parameters.getDoubleWithDefault(base
				.push(ChargeValidator.P_MAXFLAT), defBase
				.push(ChargeValidator.P_MAXFLAT),
				ChargingPolicy.MAXES[ChargingPolicy.FLAT]);
		minFlat = parameters.getDoubleWithDefault(base
				.push(ChargeValidator.P_MINFLAT), defBase
				.push(ChargeValidator.P_MINFLAT),
				ChargingPolicy.MINES[ChargingPolicy.FLAT]);

		maxFractional = parameters.getDoubleWithDefault(base
				.push(ChargeValidator.P_MAXFRACTIONAL), defBase
				.push(ChargeValidator.P_MAXFRACTIONAL),
				ChargingPolicy.MAXES[ChargingPolicy.FRACTIONAL]);
		minFractional = parameters.getDoubleWithDefault(base
				.push(ChargeValidator.P_MINFRACTIONAL), defBase
				.push(ChargeValidator.P_MINFRACTIONAL),
				ChargingPolicy.MINES[ChargingPolicy.FRACTIONAL]);
		isBanning = parameters.getBoolean(base.push(ChargeValidator.P_ISBANNING),
				defBase.push(ChargeValidator.P_ISBANNING), true);
	}

	public double getMaxFlat() {
		return maxFlat;
	}

	public double getMinFlat() {
		return minFlat;
	}

	public double getMaxFractional() {
		return maxFractional;
	}

	public double getMinFractional() {
		return minFractional;
	}

	public double getMax(int type) {
		if (type == ChargingPolicy.FRACTIONAL) {
			return getMaxFractional();
		} else if (type == ChargingPolicy.FLAT) {
			return getMaxFlat();
		} else {
			return Double.NaN;
		}
	}

	public double getMin(int type) {
		if (type == ChargingPolicy.FRACTIONAL) {
			return getMinFractional();
		} else if (type == ChargingPolicy.FLAT) {
			return getMinFlat();
		} else {
			return Double.NaN;
		}
	}

	/**
	 * Throws exceptions when NaN or Infinity values appear in the fee array and
	 * the banning policy is in effect. When the banning policy is not in effect,
	 * those invalid values will be replaced by minimum or maximum values allowed.
	 * This helps to avoid kicking out certain specialists that often propose
	 * invalid fees.
	 * 
	 * @param specialistId
	 * @param fees
	 * @throws InvalidChargeException
	 */
	public void check(final String specialistId, final double fees[])
			throws InvalidChargeException {
		if (fees == null) {
			throw new InvalidChargeException("Empty charge list !");
		} else if (fees.length != ChargingPolicy.P_FEES.length) {
			throw new InvalidChargeException("Charge list with invalid length !");
		} else {
			String err = null;
			double adjustedFee = Double.NaN;
			for (int i = 0; i < ChargingPolicy.P_FEES.length; i++) {
				if (Double.isNaN(fees[i])) {
					err = "Flat " + ChargingPolicy.P_FEES[i] + " fee from "
							+ specialistId + " cannot be NaN !";
					adjustedFee = getMin(ChargingPolicy.FEE_TYPES[i]);
				} else if (Double.isInfinite(fees[i])) {
					err = "Flat " + ChargingPolicy.P_FEES[i] + " fee from "
							+ specialistId + " cannot be Infinity !";
					adjustedFee = getMax(ChargingPolicy.FEE_TYPES[i]);
				} else {
					if (ChargingPolicy.FEE_TYPES[i] == ChargingPolicy.FLAT) {
						if (fees[i] < minFlat) {
							err = "Flat " + ChargingPolicy.P_FEES[i] + " fee from "
									+ specialistId + " is below minimum !";
							adjustedFee = getMin(ChargingPolicy.FEE_TYPES[i]);
						} else if (fees[i] > maxFlat) {
							err = "Flat " + ChargingPolicy.P_FEES[i] + " fee from "
									+ specialistId + " is above maximum !";
							adjustedFee = getMax(ChargingPolicy.FEE_TYPES[i]);
						}
					} else if (ChargingPolicy.FEE_TYPES[i] == ChargingPolicy.FRACTIONAL) {
						if (fees[i] < minFractional) {
							err = "Fractional " + ChargingPolicy.P_FEES[i] + " fee from "
									+ specialistId + " is below minimum !";
							adjustedFee = getMin(ChargingPolicy.FEE_TYPES[i]);
						} else if (fees[i] > maxFractional) {
							err = "Fractional " + ChargingPolicy.P_FEES[i] + " fee from "
									+ specialistId + " is above maximum !";
							adjustedFee = getMax(ChargingPolicy.FEE_TYPES[i]);
						}
					} else {
						ChargeValidator.logger.fatal("Invalid charge type !");
					}
				}

				if (err != null) {
					if (isBanning) {
						throw new InvalidChargeException(err);
					} else {
						ChargeValidator.logger.error(err);
						fees[i] = adjustedFee;
					}
				}
			}
		}
	}
}
