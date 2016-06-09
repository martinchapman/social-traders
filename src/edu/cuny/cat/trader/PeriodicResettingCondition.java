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

package edu.cuny.cat.trader;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * resets a trading agent after every fixed-length period.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.days</tt><br>
 * <font size=-1>int (-1 by default)</font></td>
 * <td valign=top>(the length of period in terms of game days; a non-positive
 * length indicates no periodic resetting)</td>
 * <tr>
 * 
 * </table>
 * 
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>periodic_resetting</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class PeriodicResettingCondition extends ResettingCondition {

	static Logger logger = Logger.getLogger(PeriodicResettingCondition.class);

	public static final String P_DEF_BASE = "periodic_resetting";

	public static final String P_DAYS = "days";

	protected int days = -1;

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(
				PeriodicResettingCondition.P_DEF_BASE);
		days = parameters.getIntWithDefault(base
				.push(PeriodicResettingCondition.P_DAYS), defBase
				.push(PeriodicResettingCondition.P_DAYS), days);
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		if ((days > 0) && (event.getDay() >= days)) {
			if (event.getDay() % days == 0) {
				setChanged();
				notifyObservers();
			}
		}
	}

	@Override
	public String toString() {
		return super.toString() + " days:" + days;
	}
}