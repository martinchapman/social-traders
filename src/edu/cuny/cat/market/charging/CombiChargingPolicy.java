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

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * <p>
 * A charging policy that combines multiple child charging policies.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * 
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.n</tt><br>
 * <font size=-1>int</font></td>
 * <td valign=top>(the number of child charging policies)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.<i>i</i></tt><br>
 * <font size=-1>int</font></td>
 * <td valign=top>(the parameter base of the ith child charging policies)</td>
 * </tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.23 $
 * 
 */
public abstract class CombiChargingPolicy extends ChargingPolicy {

	static Logger logger = Logger.getLogger(CombiChargingPolicy.class);

	public static final String P_NUM = "n";

	protected ChargingPolicy policies[];

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final int n = parameters.getIntWithDefault(base
				.push(CombiChargingPolicy.P_NUM), null, 0);
		if (n < 0) {
			CombiChargingPolicy.logger.error("Invalid number of charging policies: "
					+ n);
		} else {
			policies = new ChargingPolicy[n];
			for (int i = 0; i < n; i++) {
				policies[i] = parameters.getInstanceForParameter(base.push(String
						.valueOf(i)), null, ChargingPolicy.class);
				if (policies[i] instanceof Parameterizable) {
					((Parameterizable) policies[i]).setup(parameters, base.push(String
							.valueOf(i)));
				}
				policies[i].initialize();
			}
		}
	}

	@Override
	public void reset() {
		super.reset();

		if (policies != null) {
			for (int i = 0; i < policies.length; i++) {
				policies[i].reset();
			}
		}
	}

	@Override
	public String toString() {
		String s = super.toString();

		if (policies != null) {
			for (int i = 0; i < policies.length; i++) {
				s += "\n" + Utils.indent(policies[i].toString());
			}
		}

		return s;
	}
}