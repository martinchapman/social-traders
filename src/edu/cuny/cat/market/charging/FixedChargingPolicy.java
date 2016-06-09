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

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * <p>
 * A fixed charging policy in a market charges at a constant rate, i.e.
 * remaining the same over time.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.registration</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(charge on registration)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.information</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(charge on information)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.shout</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(charge on shout)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.transaction</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(charge on transaction)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.profit</tt><br>
 * <font size=-1>double [0,1]</font></td>
 * <td valign=top>(charge on profit)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>fixed_charging</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.17 $
 * 
 */
public class FixedChargingPolicy extends ChargingPolicy {

	public final static String P_DEF_BASE = "fixed_charging";

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(FixedChargingPolicy.P_DEF_BASE);
		for (int i = 0; i < fees.length; i++) {
			fees[i] = parameters.getDoubleWithDefault(base
					.push(ChargingPolicy.P_FEES[i]), defBase
					.push(ChargingPolicy.P_FEES[i]), fees[i]);
		}
	}
}