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
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Utils;

/**
 * <p>
 * A charging policy that divides the duration of a game into several phases and
 * use one of its child charging policies during each phase.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.<i>i</i></tt><br>
 * <font size=-1>int</font></td>
 * <td valign=top>(the parameter base of the ith phase)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.<i>i</i>.start</tt><br>
 * <font size=-1>int</font></td>
 * <td valign=top>(the start day of the ith phase (inclusive))</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.<i>i</i>.end</tt><br>
 * <font size=-1>int</font></td>
 * <td valign=top>(the end day of the ith phase (inclusive))</td>
 * </tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.23 $
 * 
 */
public class SerialChargingPolicy extends CombiChargingPolicy {

	static Logger logger = Logger.getLogger(SerialChargingPolicy.class);

	public final static String P_START = "start";

	public final static String P_END = "end";

	protected ChargingPhase phases[];

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final int n = parameters.getIntWithDefault(base
				.push(CombiChargingPolicy.P_NUM), null, 0);
		if (n < 0) {
			SerialChargingPolicy.logger
					.error("Invalid number of serial phases: " + n);
		} else {
			phases = new ChargingPhase[n];
			for (int i = 0; i < n; i++) {
				phases[i] = new ChargingPhase();
				phases[i].start = parameters.getIntWithDefault(base.push(
						String.valueOf(i)).push(SerialChargingPolicy.P_START), null,
						Integer.MAX_VALUE);

				phases[i].end = parameters.getIntWithDefault(base.push(
						String.valueOf(i)).push(SerialChargingPolicy.P_END), null,
						Integer.MIN_VALUE);

				if (phases[i].start > phases[i].end) {
					SerialChargingPolicy.logger
							.error("ChargingPhase with negative range defined !");
				}
			}
		}
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		for (int i = 0; i < policies.length; i++) {
			policies[i].eventOccurred(event);
		}

		int day = -1;
		if (event instanceof GameStartingEvent) {
			day = 0;
		} else if (event instanceof DayClosedEvent) {
			day = ((DayClosedEvent) event).getDay() + 1;
		}

		if (day >= 0) {
			final int index = findPhase(day);
			if (index >= 0) {
				final double feesOfChildPolicy[] = policies[index].getFees();
				System.arraycopy(feesOfChildPolicy, 0, fees, 0,
						feesOfChildPolicy.length);
			}
		}
	}

	protected int findPhase(int day) {
		for (int i = 0; i < phases.length; i++) {
			if (phases[i].include(day)) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();
		if (policies != null) {
			for (int i = 0; i < policies.length; i++) {
				s += "\n" + Utils.indent(phases[i].toSring());
				s += "\n" + Utils.indent(policies[i].toString());
			}
		}

		return s;
	}

	class ChargingPhase {
		public int start;

		public int end;

		public boolean include(int day) {
			return (start <= day) && (day <= end);
		}

		public String toSring() {
			return "[" + start + ", " + end + "]";
		}
	}
}