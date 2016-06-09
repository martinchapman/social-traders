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

package edu.cuny.cat.server;

import org.apache.log4j.Logger;

import edu.cuny.cat.valuation.ValuationPolicy;
import edu.cuny.cat.valuation.ValuerGenerator;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * A factory class providing {@link edu.cuny.cat.valuation.ValuerGenerator}
 * instances that can in turn create
 * {@link edu.cuny.cat.valuation.ValuationPolicy} instances to generate demand
 * and supply schedules among simulated trading agents.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.buyer</tt><br>
 * <font size=-1>class, implementing
 * {@link edu.cuny.cat.valuation.ValuerGenerator}</font></td>
 * <td valign=top>(the type of demand schedule generator)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.seller</tt><br>
 * <font size=-1>class, implementing
 * {@link edu.cuny.cat.valuation.ValuerGenerator}</font></td>
 * <td valign=top>(the type of supply schedule generator)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>valuation</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.12 $
 */
public final class ValuerFactory implements Parameterizable {

	static Logger logger = Logger.getLogger(ValuerFactory.class);

	public static final String P_DEF_BASE = "valuation";

	public static final String P_BUYER = "buyer";

	public static final String P_SELLER = "seller";

	protected ValuerGenerator buyerValuerGenerator;

	protected ValuerGenerator sellerValuerGenerator;

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		buyerValuerGenerator = parameters.getInstanceForParameter(base
				.push(ValuerFactory.P_BUYER), null, ValuerGenerator.class);
		if (buyerValuerGenerator instanceof Parameterizable) {
			((Parameterizable) buyerValuerGenerator).setup(parameters, base
					.push(ValuerFactory.P_BUYER));
		}

		sellerValuerGenerator = parameters.getInstanceForParameter(base
				.push(ValuerFactory.P_SELLER), null, ValuerGenerator.class);
		if (sellerValuerGenerator instanceof Parameterizable) {
			((Parameterizable) sellerValuerGenerator).setup(parameters, base
					.push(ValuerFactory.P_SELLER));
		}
	}

	public ValuationPolicy createValuer(final boolean isSeller) {
		ValuationPolicy valuer = null;
		if (isSeller) {
			valuer = sellerValuerGenerator.createValuer();
		} else {
			valuer = buyerValuerGenerator.createValuer();
		}
		return valuer;
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();
		s += "\n"
				+ Utils.indent("sellerValuerGenerator:"
						+ sellerValuerGenerator.toString());
		s += "\n"
				+ Utils.indent("buyerValuerGenerator:"
						+ buyerValuerGenerator.toString());

		return s;
	}
}
