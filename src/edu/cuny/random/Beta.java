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
 * href="http://en.wikipedia.org/wiki/Beta_distribution">beta distribution</a>.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.alpha</tt><br>
 * <font size=-1>double > 0 (1 by default)</font></td>
 * <td valign=top></td>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.beta</tt><br>
 * <font size=-1>double > 0 (1 by default)</font></td>
 * <td valign=top></td>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>beta</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class Beta extends cern.jet.random.Beta implements Parameterizable,
		StateCopyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(Beta.class);

	public static final String P_ALPHA = "alpha";

	public static final String P_BETA = "beta";

	public static final String P_DEF_BASE = "beta";

	public static final double DEFAULT_ALPHA = 1;

	public static final double DEFAULT_BETA = 1;

	public Beta() {
		this(Beta.DEFAULT_ALPHA, Beta.DEFAULT_BETA, Galaxy.getInstance()
				.getDefaultTyped(GlobalPRNG.class).getEngine());
	}

	public Beta(final double alpha, final double beta,
			final RandomEngine randomGenerator) {
		super(alpha, beta, randomGenerator);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(Beta.P_DEF_BASE);

		final double alpha = parameters.getDoubleWithDefault(base
				.push(Beta.P_ALPHA), defBase.push(Beta.P_ALPHA), Beta.DEFAULT_ALPHA);
		final double beta = parameters.getDoubleWithDefault(base.push(Beta.P_BETA),
				defBase.push(Beta.P_BETA), Beta.DEFAULT_BETA);

		setState(alpha, beta);
	}

	public void copyStateFrom(final Object example) {
		final Beta beta = (Beta) example;
		setState(beta.alpha, beta.beta);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " alpha:" + alpha + " beta:" + beta;
	}
}