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

package edu.cuny.cat.market.clearing;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * <p>
 * Combines multiple market clearing conditions and the market is cleared if any
 * of the conditions triggers clearing.
 * </p>
 * 
 * <p>
 * For example, a combination of {@link ProbabilisticClearingCondition} and
 * {@link RoundClearingCondition} can express a continuum between CDA and CH.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.n</tt><br>
 * <font size=-1>int &gt;= 1</font></td>
 * <td valign=top>(the number of different clearing conditions to configure)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.</tt><i>n</i><br>
 * <font size=-1>name of class, inheriting {@link MarketClearingCondition}
 * </font></td>
 * <td valign=top>(the <i>n</i>th clearing condition)</td>
 * <tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.12 $
 * 
 */

public class CombiClearingCondition extends MarketClearingCondition {

	public static final String P_NUM = "n";

	MarketClearingCondition conditions[];

	static Logger logger = Logger.getLogger(CombiClearingCondition.class);

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final int n = parameters.getIntWithDefault(base
				.push(CombiClearingCondition.P_NUM), null, 0);
		if (n < 0) {
			CombiClearingCondition.logger
					.error("Invalid number of clearing conditions: " + n);
		} else {
			conditions = new MarketClearingCondition[n];
			for (int i = 0; i < n; i++) {
				conditions[i] = parameters.getInstanceForParameter(base.push(String
						.valueOf(i)), null, MarketClearingCondition.class);
				if (conditions[i] instanceof Parameterizable) {
					((Parameterizable) conditions[i]).setup(parameters, base.push(String
							.valueOf(i)));
				}
				conditions[i].initialize();
			}
		}
	}

	@Override
	public void reset() {
		super.reset();

		if (conditions != null) {
			for (int i = 0; i < conditions.length; i++) {
				conditions[i].reset();
			}
		}
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (conditions != null) {
			for (final MarketClearingCondition condition : conditions) {
				condition.eventOccurred(event);
			}
		}
	}

	@Override
	public String toString() {
		String s = super.toString();

		if (conditions != null) {
			for (int i = 0; i < conditions.length; i++) {
				s += "\n" + Utils.indent(conditions[i].toString());
			}
		}

		return s;
	}
}