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

package edu.cuny.cat.valuation;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Utils;

/**
 * This valuer generator creates valuation policies in which we randomly
 * determine our valuation across all auctions and all units at
 * agent-initialisation time. Valuations are drawn from a uniform distribution
 * on ranges that vary game from game in a multi-game simulation. This is in
 * contrast to the constant range and various types of distributions used in
 * {@link RandomValuerGenerator}.
 * 
 * </p>
 * <p>
 * <b>Parameters </b>
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.minvaluemin</tt><br>
 * <font size=-1>double &gt;= 0 </font></td>
 * <td valign=top>(the lower bound of the minimum valuations of ranges)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.minvaluemax</tt><br>
 * <font size=-1>double &gt;=0 </font></td>
 * <td valign=top>(the upper bound of the minimum valuations of ranges)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.rangemin</tt><br>
 * <font size=-1>double &gt;= 0 </font></td>
 * <td valign=top>(the lower bound of the sizes of ranges)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.rangemax</tt><br>
 * <font size=-1>double &gt;=0 </font></td>
 * <td valign=top>(the upper bound of the sizes of ranges)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>distinct_distribution_valuer</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */
public class DistinctDistributionValuerGenerator implements ValuerGenerator {

	protected double minValueMin;

	protected double minValueMax;

	protected double rangeMin;

	protected double rangeMax;

	public static final String P_DEF_BASE = "distinct_distribution_valuer";

	public static final String P_MINVALUEMIN = "minvaluemin";

	public static final String P_MINVALUEMAX = "minvaluemax";

	public static final String P_RANGEMIN = "rangemin";

	public static final String P_RANGEMAX = "rangemax";

	public DistinctDistributionValuerGenerator() {
		super();
	}

	public DistinctDistributionValuerGenerator(final double minValueMin,
			final double minValueMax, final double rangeMin, final double rangeMax) {
		this.minValueMin = minValueMin;
		this.minValueMax = minValueMax;
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		final Parameter defBase = new Parameter(
				DistinctDistributionValuerGenerator.P_DEF_BASE);

		minValueMin = parameters.getDouble(base
				.push(DistinctDistributionValuerGenerator.P_MINVALUEMIN), defBase
				.push(DistinctDistributionValuerGenerator.P_MINVALUEMIN), 0.0);

		minValueMax = parameters.getDouble(base
				.push(DistinctDistributionValuerGenerator.P_MINVALUEMAX), defBase
				.push(DistinctDistributionValuerGenerator.P_MINVALUEMAX), minValueMin);

		rangeMin = parameters.getDouble(base
				.push(DistinctDistributionValuerGenerator.P_RANGEMIN), defBase
				.push(DistinctDistributionValuerGenerator.P_RANGEMIN), 0.0);

		rangeMax = parameters.getDouble(base
				.push(DistinctDistributionValuerGenerator.P_RANGEMAX), defBase
				.push(DistinctDistributionValuerGenerator.P_RANGEMAX), rangeMin);
	}

	public ValuationPolicy createValuer() {
		final DistinctDistributionValuer valuer = new DistinctDistributionValuer();
		valuer.setGenerator(this);
		return valuer;
	}

	public void reset() {
		// do nothing
	}

	public void eventOccurred(final AuctionEvent event) {
		// do nothing
	}

	public double getMinValueMin() {
		return minValueMin;
	}

	public void setMinValueMin(final double minValueMin) {
		this.minValueMin = minValueMin;
	}

	public double getMinValueMax() {
		return minValueMax;
	}

	public void setMinValueMax(final double minValueMax) {
		this.minValueMax = minValueMax;
	}

	public double getRangeMin() {
		return rangeMin;
	}

	public void setRangeMin(final double rangeMin) {
		this.rangeMin = rangeMin;
	}

	public double getRangeMax() {
		return rangeMax;
	}

	public void setRangeMax(final double rangeMax) {
		this.rangeMax = rangeMax;
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();
		s += "\n" + Utils.indent("minValueMin:" + minValueMin);
		s += "\n" + Utils.indent("minValueMax:" + minValueMax);
		s += "\n" + Utils.indent("rangeMin:" + rangeMin);
		s += "\n" + Utils.indent("rangeMax:" + rangeMax);

		return s;
	}
}
