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
 * href="http://en.wikipedia.org/wiki/Chi-square_distribution">chi-square
 * distribution</a>.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.freedom</tt><br>
 * <font size=-1>double > 0 (1 by default)</font></td>
 * <td valign=top>(degrees of freedom)</td>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>chisquare</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class ChiSquare extends cern.jet.random.ChiSquare implements
		Parameterizable, StateCopyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(ChiSquare.class);

	public static final String P_FREEDOM = "freedom";

	public static final String P_DEF_BASE = "chisquare";

	public static final double DEFAULT_FREEDOM = 1;

	public ChiSquare() {
		this(ChiSquare.DEFAULT_FREEDOM, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	public ChiSquare(final double freedom, final RandomEngine randomGenerator) {
		super(freedom, randomGenerator);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(ChiSquare.P_DEF_BASE);

		final double freedom = parameters.getDoubleWithDefault(base
				.push(ChiSquare.P_FREEDOM), defBase.push(ChiSquare.P_FREEDOM),
				ChiSquare.DEFAULT_FREEDOM);

		setState(freedom);
	}

	public void copyStateFrom(final Object example) {
		final ChiSquare cs = (ChiSquare) example;
		setState(cs.freedom);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " freedom:" + freedom;
	}
}