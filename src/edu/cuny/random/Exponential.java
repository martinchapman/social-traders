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
 * href="http://en.wikipedia.org/wiki/Exponential_distribution">exponential
 * distribution</a>.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.lambda</tt><br>
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
 * <td valign=top><tt>exponential</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class Exponential extends cern.jet.random.Exponential implements
		Parameterizable, StateCopyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(Exponential.class);

	public static final String P_LAMBDA = "lambda";

	public static final String P_DEF_BASE = "exponential";

	public static final double DEFAULT_LAMBDA = 1;

	public Exponential() {
		this(Exponential.DEFAULT_LAMBDA, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	public Exponential(final double lambda, final RandomEngine randomGenerator) {
		super(lambda, randomGenerator);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(Exponential.P_DEF_BASE);

		final double lambda = parameters.getDoubleWithDefault(base
				.push(Exponential.P_LAMBDA), defBase.push(Exponential.P_LAMBDA),
				Exponential.DEFAULT_LAMBDA);

		setState(lambda);
	}

	public void copyStateFrom(final Object example) {
		final Exponential e = (Exponential) example;
		setState(e.lambda);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " lambda:" + lambda;
	}
}