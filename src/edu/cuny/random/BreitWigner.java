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
 * href="http://en.wikipedia.org/wiki/Relativistic_Breit-Wigner_distribution"
 * >Breit-Wigner distribution</a>.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.mean</tt><br>
 * <font size=-1>double (0 by default)</font></td>
 * <td valign=top></td>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.gamma</tt><br>
 * <font size=-1>double > 0 (1 by default)</font></td>
 * <td valign=top></td>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.cut</tt><br>
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
 * <td valign=top><tt>breitwigner</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class BreitWigner extends cern.jet.random.BreitWigner implements
		Parameterizable, StateCopyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(BreitWigner.class);

	public static final String P_MEAN = "mean";

	public static final String P_GAMMA = "gamma";

	public static final String P_CUT = "cut";

	public static final String P_DEF_BASE = "breitwigner";

	public static final double DEFAULT_MEAN = 0;

	public static final double DEFAULT_GAMMA = 1;

	public static final double DEFAULT_CUT = 1;

	public BreitWigner() {
		this(BreitWigner.DEFAULT_MEAN, BreitWigner.DEFAULT_GAMMA,
				BreitWigner.DEFAULT_CUT, Galaxy.getInstance().getDefaultTyped(
						GlobalPRNG.class).getEngine());
	}

	public BreitWigner(final double mean, final double gamma, final double cut,
			final RandomEngine randomGenerator) {
		super(mean, gamma, cut, randomGenerator);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(BreitWigner.P_DEF_BASE);

		final double mean = parameters.getDoubleWithDefault(base
				.push(BreitWigner.P_MEAN), defBase.push(BreitWigner.P_MEAN),
				BreitWigner.DEFAULT_MEAN);
		final double gamma = parameters.getDoubleWithDefault(base
				.push(BreitWigner.P_GAMMA), defBase.push(BreitWigner.P_GAMMA),
				BreitWigner.DEFAULT_GAMMA);
		final double cut = parameters.getDoubleWithDefault(base
				.push(BreitWigner.P_CUT), defBase.push(BreitWigner.P_CUT),
				BreitWigner.DEFAULT_CUT);

		setState(mean, gamma, cut);
	}

	public void copyStateFrom(final Object example) {
		final BreitWigner bw = (BreitWigner) example;
		setState(bw.mean, bw.gamma, bw.cut);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " mean:" + mean + " gamma:" + gamma
				+ " cut:" + cut;
	}
}