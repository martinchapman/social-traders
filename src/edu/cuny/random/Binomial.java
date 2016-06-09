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
 * href="http://en.wikipedia.org/wiki/Binomial_distribution">binomial
 * distribution</a>.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.n</tt><br>
 * <font size=-1>int >= 0 (1 by default)</font></td>
 * <td valign=top>(the number of trials)</td>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.p</tt><br>
 * <font size=-1>double in [0,1] (0.5 by default)</font></td>
 * <td valign=top>(the probability of success)</td>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>binomial</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class Binomial extends cern.jet.random.Binomial implements
		Parameterizable, StateCopyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(Binomial.class);

	public static final String P_N = "n";

	public static final String P_P = "p";

	public static final String P_DEF_BASE = "binomial";

	public static final int DEFAULT_N = 1;

	public static final double DEFAULT_P = 0.5;

	public Binomial() {
		this(Binomial.DEFAULT_N, Binomial.DEFAULT_P, Galaxy.getInstance()
				.getDefaultTyped(GlobalPRNG.class).getEngine());
	}

	public Binomial(final int n, final double p,
			final RandomEngine randomGenerator) {
		super(n, p, randomGenerator);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(Binomial.P_DEF_BASE);

		final int n = parameters.getIntWithDefault(base.push(Binomial.P_N), defBase
				.push(Binomial.P_N), Binomial.DEFAULT_N);
		final double p = parameters.getDoubleWithDefault(base.push(Binomial.P_P),
				defBase.push(Binomial.P_P), Binomial.DEFAULT_P);

		setNandP(n, p);
	}

	public void copyStateFrom(final Object example) {
		final Binomial binomial = (Binomial) example;
		setNandP(binomial.n, binomial.p);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " n:" + n + " p:" + p;
	}
}