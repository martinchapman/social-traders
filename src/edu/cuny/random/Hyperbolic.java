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
 * href="http://en.wikipedia.org/wiki/Hypergeometric_distribution">Hyperbolic
 * distribution</a>.
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
 * <td valign=top><tt>hyperbolic</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class Hyperbolic extends cern.jet.random.Hyperbolic implements
		Parameterizable, StateCopyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(Hyperbolic.class);

	public static final String P_ALPHA = "alpha";

	public static final String P_BETA = "beta";

	public static final String P_DEF_BASE = "hyperbolic";

	public static final double DEFAULT_ALPHA = 1;

	public static final double DEFAULT_BETA = 1;

	public Hyperbolic() {
		this(Hyperbolic.DEFAULT_ALPHA, Hyperbolic.DEFAULT_BETA, Galaxy
				.getInstance().getDefaultTyped(GlobalPRNG.class).getEngine());
	}

	public Hyperbolic(final double alpha, final double beta,
			final RandomEngine randomGenerator) {
		super(alpha, beta, randomGenerator);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(Hyperbolic.P_DEF_BASE);

		final double alpha = parameters.getDoubleWithDefault(base
				.push(Hyperbolic.P_ALPHA), defBase.push(Hyperbolic.P_ALPHA),
				Hyperbolic.DEFAULT_ALPHA);
		final double beta = parameters.getDoubleWithDefault(base
				.push(Hyperbolic.P_BETA), defBase.push(Hyperbolic.P_BETA),
				Hyperbolic.DEFAULT_BETA);

		setState(alpha, beta);
	}

	public void copyStateFrom(final Object example) {
		final Hyperbolic hb = (Hyperbolic) example;
		setState(hb.alpha, hb.beta);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " alpha:" + alpha + " beta:" + beta;
	}
}