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
 * <p>
 * Agents configured with valuation policies created by this generator will
 * receive a unique private value from a common set of values starting at
 * <code>minValue</code> and incrementing by <code>step</code> as each agent is
 * assigned a valuation at agent setup time. This is useful for quickly
 * specifying supply or demand curves with a constant "slope" (step).
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.minvalue</tt><br>
 * <font size=-1>double &gt;= 0 (50.0 by default)</font></td>
 * <td valign=top>(the minimal private value to be allocated)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.step</tt><br>
 * <font size=-1>double &gt;= 0 (5.0 by default)</font></td>
 * <td valign=top>(the amount to increase each step for next private value)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>interval_valuer</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class IntervalValuerGenerator implements ValuerGenerator {

	public static final String P_DEF_BASE = "interval_valuer";

	public static final String P_MINVALUE = "minvalue";

	public static final String P_STEP = "step";

	public static final double DEFAULT_MINVALUE = 50.0;

	public static final double DEFAULT_STEP = 5.0;

	/**
	 * The minimum valuation that any buyer will receive.
	 */
	protected double minValue = IntervalValuerGenerator.DEFAULT_MINVALUE;

	/**
	 * The increment in valuation to use
	 */
	protected double step = IntervalValuerGenerator.DEFAULT_STEP;

	protected double nextValue;

	protected boolean firstValue = true;

	public IntervalValuerGenerator() {
		this(IntervalValuerGenerator.DEFAULT_MINVALUE,
				IntervalValuerGenerator.DEFAULT_STEP);
	}

	public IntervalValuerGenerator(final double minValue, final double step) {
		this.minValue = minValue;
		this.step = step;

		init0();
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		minValue = parameters.getDouble(base
				.push(IntervalValuerGenerator.P_MINVALUE), new Parameter(
				IntervalValuerGenerator.P_DEF_BASE)
				.push(IntervalValuerGenerator.P_MINVALUE), minValue);
		step = parameters.getDouble(base.push(IntervalValuerGenerator.P_STEP),
				new Parameter(IntervalValuerGenerator.P_DEF_BASE)
						.push(IntervalValuerGenerator.P_STEP), step);
	}

	private void init0() {
		firstValue = true;
	}

	public void reset() {
		init0();
	}

	protected boolean firstValue() {
		return firstValue;
	}

	protected double getMinValue() {
		return minValue;
	}

	protected double getNextValue() {
		return nextValue;
	}

	protected double getStep() {
		return step;
	}

	protected void setFirstValue(final boolean firstValue) {
		this.firstValue = firstValue;
	}

	protected void setMinValue(final double value) {
		minValue = value;
	}

	protected void setNextValue(final double value) {
		nextValue = value;
	}

	protected void setStep(final double step) {
		this.step = step;
	}

	public synchronized ValuationPolicy createValuer() {
		if (firstValue) {
			nextValue = minValue;
			firstValue = false;
		} else {
			nextValue += step;
		}

		final FixedValuer valuer = new FixedValuer();
		valuer.setValue(nextValue);

		return valuer;
	}

	public void eventOccurred(final AuctionEvent event) {
		// do nothing
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();

		s += "\n" + Utils.indent("minValue:" + minValue);
		s += "\n" + Utils.indent("step:" + step);

		return s;
	}
}