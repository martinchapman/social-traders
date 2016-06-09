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
 * href="http://en.wikipedia.org/wiki/Logarithmic_distribution">logarithmic
 * distribution</a>.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.p</tt><br>
 * <font size=-1>double in (0,1) (0.5 by default)</font></td>
 * <td valign=top></td>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>logarithmic</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class Logarithmic extends cern.jet.random.Logarithmic implements
		Parameterizable, StateCopyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(Logarithmic.class);

	public static final String P_P = "p";

	public static final String P_DEF_BASE = "logarithmic";

	public static final double DEFAULT_P = 0.5;

	public Logarithmic() {
		this(Logarithmic.DEFAULT_P, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	public Logarithmic(final double p, final RandomEngine randomGenerator) {
		super(p, randomGenerator);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(Logarithmic.P_DEF_BASE);

		final double p = parameters.getDoubleWithDefault(
				base.push(Logarithmic.P_P), defBase.push(Logarithmic.P_P),
				Logarithmic.DEFAULT_P);

		setState(p);
	}

	public void copyStateFrom(final Object example) {
		final Logarithmic log = (Logarithmic) example;
		setState(log.my_p);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " p:" + my_p;
	}
}