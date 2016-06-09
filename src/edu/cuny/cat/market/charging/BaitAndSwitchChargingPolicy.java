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
import edu.cuny.cat.event.RegisteredTradersAnnouncedEvent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Utils;

/**
 * <p>
 * This charge-cutting policy makes a specialist to cut its charge until it
 * captures a certain chunk of market share, then slowly increases its charge,
 * and then adjusts its charge downward again if its market share drops below a
 * certain level.
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
 * <td valign=top><i>base</i><tt>.cutratio</tt><br>
 * <font size=-1>double [0,1]</font></td>
 * <td valign=top>(the speed a specialist cuts its charges down from the lowest
 * charges of markets when it needs luring more traders to register)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.exploitratio</tt><br>
 * <font size=-1>double [0,1]</font></td>
 * <td valign=top>(the market share beyond which the specialist will try to
 * exploit traders)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>bait_and_switch_charging</tt></td>
 * </tr>
 * </table>
 * 
 * @author Kai Cai
 * @version $Revision: 1.8 $
 * 
 */

public class BaitAndSwitchChargingPolicy extends ChargingPolicy {

	static Logger logger = Logger.getLogger(BaitAndSwitchChargingPolicy.class);

	public static final String P_EXPLOIT_RATIO = "exploitratio";

	public static final String P_CUT_RATIO = "cutratio";

	public static final String P_DEF_BASE = "bait_and_switch_charging";

	public static final double DEFAULT_EXPLOIT_RATIO = 0.6;

	public static final double DEFAULT_CUT_RATIO = 0.5;

	protected double exploitRatio;

	protected double cutRatio;

	protected double[] minFees;

	protected double[] maxFees;

	protected int numberOfTradersRegisteredToday = 0;

	protected int totalNumberOfTraders = 0;

	protected boolean toLureTrader = true;

	public BaitAndSwitchChargingPolicy() {
		minFees = new double[ChargingPolicy.P_FEES.length];
		maxFees = new double[ChargingPolicy.P_FEES.length];

		init0();
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(
				BaitAndSwitchChargingPolicy.P_DEF_BASE);
		for (int i = 0; i < fees.length; i++) {
			fees[i] = parameters.getDoubleWithDefault(base
					.push(ChargingPolicy.P_FEES[i]), defBase
					.push(ChargingPolicy.P_FEES[i]), fees[i]);
		}

		exploitRatio = parameters.getDoubleWithDefault(base
				.push(BaitAndSwitchChargingPolicy.P_EXPLOIT_RATIO), defBase
				.push(BaitAndSwitchChargingPolicy.P_EXPLOIT_RATIO),
				BaitAndSwitchChargingPolicy.DEFAULT_EXPLOIT_RATIO);
		cutRatio = parameters.getDoubleWithDefault(base
				.push(BaitAndSwitchChargingPolicy.P_CUT_RATIO), defBase
				.push(BaitAndSwitchChargingPolicy.P_CUT_RATIO),
				BaitAndSwitchChargingPolicy.DEFAULT_CUT_RATIO);
	}

	private void init0() {
		toLureTrader = true;

		for (int i = 0; i < fees.length; i++) {
			minFees[i] = Double.POSITIVE_INFINITY;
			maxFees[i] = Double.NEGATIVE_INFINITY;
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

			updateMinAndMaxFees((FeesAnnouncedEvent) event);

		} else if (event instanceof RegisteredTradersAnnouncedEvent) {

			updateTraderRegistration(((RegisteredTradersAnnouncedEvent) event));

		} else if (event instanceof DayClosedEvent) {

			updateExploitStatus();
			updateFees();
		}
	}

	protected void updateTraderRegistration(
			final RegisteredTradersAnnouncedEvent event) {
		final int numberOfTraders = event.getNumOfTraders();

		if (event.getSpecialist().getId().equalsIgnoreCase(
				getAuctioneer().getName())) {
			numberOfTradersRegisteredToday = numberOfTraders;
		}

		totalNumberOfTraders += numberOfTraders;
	}

	protected void updateExploitStatus() {
		double curMarketShare = 0;
		if (totalNumberOfTraders != 0) {
			curMarketShare = (double) numberOfTradersRegisteredToday
					/ (double) totalNumberOfTraders;
		}

		if (curMarketShare >= exploitRatio) {
			toLureTrader = false;
		} else {
			toLureTrader = true;
		}

		numberOfTradersRegisteredToday = 0;
		totalNumberOfTraders = 0;
	}

	protected void updateMinAndMaxFees(final FeesAnnouncedEvent event) {

		if (!event.getSpecialist().getId().equalsIgnoreCase(
				getAuctioneer().getName())) {

			final double tempFees[] = event.getFees();

			for (int i = 0; i < minFees.length; i++) {
				if ((minFees[i] > tempFees[i]) && (tempFees[i] != 0)) {
					minFees[i] = tempFees[i];
				}

				if (maxFees[i] < tempFees[i]) {
					maxFees[i] = tempFees[i];
				}
			}
		}
	}

	protected void updateFees() {
		if (toLureTrader) {
			// logger.info("trader being lured ...");

			for (int i = 0; i < fees.length; i++) {
				if (minFees[i] <= fees[i]) {
					fees[i] = minFees[i] * cutRatio;
					// logger.info("fee for next day: " + fees[i]);
				}
			}
		} else {
			for (int i = 0; i < fees.length; i++) {
				if (maxFees[i] > fees[i]) {
					fees[i] = maxFees[i];
					// logger.info("fee for next day: " + fees[i]);
				}
			}
		}
	}

	@Override
	public String toString() {
		String s = super.toString();

		s += "\n"
				+ Utils.indent(BaitAndSwitchChargingPolicy.P_EXPLOIT_RATIO + ":"
						+ exploitRatio + " " + BaitAndSwitchChargingPolicy.P_CUT_RATIO
						+ ":" + cutRatio);

		return s;
	}
}