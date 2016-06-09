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
 * receive an identical fixed private value at <code>value</code>.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.value</tt><br>
 * <font size=-1>double &gt;= 0 </font></td>
 * <td valign=top>(private value allocated)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>fixed_valuer</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class FixedValuerGenerator implements ValuerGenerator {

	public static final String P_DEF_BASE = "fixed_valuer";

	public static final String P_VALUE = "value";

	protected double value;

	public FixedValuerGenerator() {
	}

	public FixedValuerGenerator(final double value) {
		this.value = value;
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		value = parameters.getDouble(base.push(FixedValuerGenerator.P_VALUE),
				new Parameter(FixedValuerGenerator.P_DEF_BASE)
						.push(FixedValuerGenerator.P_VALUE), 0);
	}

	public void reset() {
		// do nothing
	}

	public synchronized ValuationPolicy createValuer() {
		final FixedValuer valuer = new FixedValuer(value);

		return valuer;
	}

	public void eventOccurred(final AuctionEvent event) {
		// do nothing
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();
		s += "\n" + Utils.indent("value:" + value);

		return s;
	}
}