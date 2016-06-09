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
 * JAF - Java Application Framework
 * Copyright (C) 1999-2006 Jinzhong Niu
 */

package edu.cuny.random;

import org.apache.log4j.Logger;

import cern.jet.random.engine.RandomEngine;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * A parameterizable <a
 * href="http://en.wikipedia.org/wiki/Exponential_power_distribution"
 * >exponential power distribution</a>.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.tau</tt><br>
 * <font size=-1>double >= 1 (1 by default)</font></td>
 * <td valign=top></td>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>exponentialpower</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class ExponentialPower extends cern.jet.random.ExponentialPower
		implements Parameterizable, StateCopyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(ExponentialPower.class);

	public static final String P_TAU = "tau";

	public static final String P_DEF_BASE = "exponentialpower";

	public static final double DEFAULT_TAU = 1;

	public ExponentialPower() {
		this(ExponentialPower.DEFAULT_TAU, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	public ExponentialPower(final double tau, final RandomEngine randomGenerator) {
		super(tau, randomGenerator);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(ExponentialPower.P_DEF_BASE);

		final double tau = parameters.getDoubleWithDefault(base
				.push(ExponentialPower.P_TAU), defBase.push(ExponentialPower.P_TAU),
				ExponentialPower.DEFAULT_TAU);

		setState(tau);
	}

	public void copyStateFrom(final Object example) {
		final ExponentialPower ep = (ExponentialPower) example;
		setState(ep.tau);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " tau:" + tau;
	}
}