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

package edu.cuny.cat.stat;

import org.apache.log4j.Logger;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * A {@link ScoreDaysCondition} with which game days falling into a certain
 * frame are all counted for scoring.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.start</tt><br>
 * <font size=-1>int >=0 (0 by default)</font></td>
 * <td valign=top>(the first day of the scoring time frame)</td>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.end</tt><br>
 * <font size=-1>int >= 0 (game len by default)</font></td>
 * <td valign=top>(the last day of the scoring time frame)</td>
 * <tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class RangeBasedScoreDaysCondition extends AbstractScoreDaysCondition {

	static final Logger logger = Logger
			.getLogger(RangeBasedScoreDaysCondition.class);

	public static final String P_START = "start";

	public static final String P_END = "end";

	protected int start = 0;

	protected int end = -1;

	protected boolean taken;

	protected int day;

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		start = parameters.getIntWithDefault(base
				.push(RangeBasedScoreDaysCondition.P_START), null, start);
		if (start < 0) {
			RangeBasedScoreDaysCondition.logger
					.warn("Scoring time frame must start on a valid day !");
			start = 0;
		}

		end = parameters.getIntWithDefault(base
				.push(RangeBasedScoreDaysCondition.P_END), null, end);
		if (end < start) {
			RangeBasedScoreDaysCondition.logger
					.warn("Scoring time frame must end on a day after the starting day !");
			end = start;
		}
	}

	@Override
	protected boolean updateTaken(final int day) {
		if ((day >= start) && (day <= end)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return super.toString() + " start:" + start + " end:" + end;
	}
}
