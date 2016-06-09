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
 * A parameterizable uniform distribution.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.min</tt><br>
 * <font size=-1>double (0 by default)</font></td>
 * <td valign=top></td>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.max</tt><br>
 * <font size=-1>double (1 by default)</font></td>
 * <td valign=top></td>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>gamma</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class Uniform extends cern.jet.random.Uniform implements
		Parameterizable, StateCopyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(Uniform.class);

	public static final String P_MIN = "min";

	public static final String P_MAX = "max";

	public static final String P_DEF_BASE = "uniform";

	public static final int DEFAULT_MIN = 0;

	public static final double DEFAULT_MAX = 1;

	public Uniform() {
		this(Uniform.DEFAULT_MIN, Uniform.DEFAULT_MAX, Galaxy.getInstance()
				.getDefaultTyped(GlobalPRNG.class).getEngine());
	}

	public Uniform(final double min, final double max,
			final RandomEngine randomGenerator) {
		super(min, max, randomGenerator);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(Uniform.P_DEF_BASE);

		final double min = parameters.getIntWithDefault(base.push(Uniform.P_MIN),
				defBase.push(Uniform.P_MIN), Uniform.DEFAULT_MIN);
		final double max = parameters.getDoubleWithDefault(
				base.push(Uniform.P_MAX), defBase.push(Uniform.P_MAX),
				Uniform.DEFAULT_MAX);

		setState(min, max);
	}

	public void copyStateFrom(final Object example) {
		final Uniform uniform = (Uniform) example;
		setState(uniform.min, uniform.max);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " min:" + min + " max:" + max;
	}
}