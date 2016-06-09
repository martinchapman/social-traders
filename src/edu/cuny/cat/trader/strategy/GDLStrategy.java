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
/*
 * JASA Java Auction Simulator API
 * Copyright (C) 2001-2005 Steve Phelps
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

package edu.cuny.cat.trader.strategy;

import org.apache.log4j.Logger;

/**
 * <p>
 * A modified implementation of the Gjerstad Dickhaut strategy using a linear
 * interpolation instead of a cubic one in the original
 * {@link edu.cuny.cat.trader.strategy.GDStrategy}.
 * </p>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>gdl_strategy</tt></td>
 * </tr>
 * </table>
 * 
 * @author Marek Marcinkiewicz
 * @version $Revision: 1.17 $
 */

public class GDLStrategy extends GDStrategy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String P_DEF_BASE = "gdl_strategy";

	static Logger logger = Logger.getLogger(GDLStrategy.class);

	public GDLStrategy() {
	}

	@Override
	public Object protoClone() {
		final GDLStrategy clone = new GDLStrategy();
		return clone;
	}

	/**
	 * looks for the point in [a1, a2] producing max expected profit. It simply
	 * checks every point in the range.
	 * 
	 * @param a1
	 * @param p1
	 * @param a2
	 * @param p2
	 */
	@Override
	protected void getMax(double a1, final double p1, double a2, final double p2) {

		if (a1 > maxPrice) {
			a1 = maxPrice;
		}

		if (a2 > maxPrice) {
			a2 = maxPrice;
		}

		if ((p1 < 0) || (p1 > 1) || (p2 < 0) || (p2 > 1)) {
			System.out.println("p1 = " + p1);
			System.out.println("p2 = " + p2);
		}

		final double pvalue = agent.getPrivateValue();

		double temp = 0;

		double p = 0;

		double start = a1;
		double end = a2;
		if (agent.isBuyer()) {
			if (a2 > pvalue) {
				end = pvalue;
			}
		} else {
			if (a1 < pvalue) {
				start = pvalue;
			}
		}

		for (double i = start; i < end; i++) {
			p = p1 + ((p2 - p1) * ((i - a1) / (a2 - a1)));
			if (agent.isBuyer()) {
				temp = p * (pvalue - i);
			} else {
				temp = p * (i - pvalue);
			}
			if (temp > max) {
				max = temp;
				maxPoint = i;
				maxProb = p;
			}
		}
	}
}
