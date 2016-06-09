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

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.FeesAnnouncedEvent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * <p>
 * This charge-cutting charging policy set the charges by scaling down the
 * lowest charges of markets imposed on the previous day. This is based on the
 * observation that traders all prefer markets with lower charges.
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
 * <tr>
 * <td valign=top><i>base</i><tt>.scale</tt><br>
 * <font size=-1>double [0, 1]</font></td>
 * <td valign=top>(the scale of the lowest charges)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>charge_cutting_charging</tt></td>
 * </tr>
 * </table>
 * 
 * @author Kai Cai
 * @version $Revision: 1.9 $
 * 
 */

public class ChargeCuttingChargingPolicy extends ChargingPolicy {

	static Logger logger = Logger.getLogger(ChargeCuttingChargingPolicy.class);

	public static final String P_SCALE = "scale";

	public static final String P_DEF_BASE = "charge_cutting_charging";

	protected double scale = 0.8;

	protected double[] currentMinFees;

	public ChargeCuttingChargingPolicy() {
		currentMinFees = new double[fees.length];
		init0();
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(
				ChargeCuttingChargingPolicy.P_DEF_BASE);
		for (int i = 0; i < fees.length; i++) {
			fees[i] = parameters.getDoubleWithDefault(base
					.push(ChargingPolicy.P_FEES[i]), defBase
					.push(ChargingPolicy.P_FEES[i]), fees[i]);
		}

		scale = parameters.getDoubleWithDefault(base
				.push(ChargeCuttingChargingPolicy.P_SCALE), defBase
				.push(ChargeCuttingChargingPolicy.P_SCALE), scale);
	}

	private void init0() {
		for (int i = 0; i < currentMinFees.length; i++) {
			currentMinFees[i] = Double.POSITIVE_INFINITY;
		}
	}

	@Override
	public void reset() {
		super.reset();
		init0();
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {

		if (event instanceof FeesAnnouncedEvent) {

			updateCurrentMinFees((FeesAnnouncedEvent) event);

		} else if (event instanceof DayClosedEvent) {

			updateFees();
		}
	}

	protected void updateCurrentMinFees(final FeesAnnouncedEvent event) {
		if (!event.getSpecialist().getId().equalsIgnoreCase(
				getAuctioneer().getName())) {

			final double[] tempFees = event.getFees();

			for (int i = 0; i < currentMinFees.length; i++) {
				if ((currentMinFees[i] > tempFees[i]) && (tempFees[i] != 0)) {
					currentMinFees[i] = tempFees[i];
				}
			}
		}
	}

	protected void updateFees() {
		for (int i = 0; i < fees.length; i++) {
			if (currentMinFees[i] < fees[i]) {
				fees[i] = currentMinFees[i] * scale;
			}
		}
	}
}