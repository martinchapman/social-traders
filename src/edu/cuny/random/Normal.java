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
 * href="http://en.wikipedia.org/wiki/Normal_distribution">normal
 * distribution</a>.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.mean</tt><br>
 * <font size=-1>double (0 by default)</font></td>
 * <td valign=top></td>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.stdev</tt><br>
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
 * <td valign=top><tt>normal</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class Normal extends cern.jet.random.Normal implements Parameterizable,
		StateCopyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(Normal.class);

	public static final String P_MEAN = "mean";

	public static final String P_STDEV = "stdev";

	public static final String P_DEF_BASE = "normal";

	public static final int DEFAULT_MEAN = 0;

	public static final double DEFAULT_STDEV = 1;

	public Normal() {
		this(Normal.DEFAULT_MEAN, Normal.DEFAULT_STDEV, Galaxy.getInstance()
				.getDefaultTyped(GlobalPRNG.class).getEngine());
	}

	public Normal(final double mean, final double stdev,
			final RandomEngine randomGenerator) {
		super(mean, stdev, randomGenerator);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(Normal.P_DEF_BASE);

		final double mean = parameters.getIntWithDefault(base.push(Normal.P_MEAN),
				defBase.push(Normal.P_MEAN), Normal.DEFAULT_MEAN);
		final double stdev = parameters.getDoubleWithDefault(base
				.push(Normal.P_STDEV), defBase.push(Normal.P_STDEV),
				Normal.DEFAULT_STDEV);

		setState(mean, stdev);
	}

	public void copyStateFrom(final Object example) {
		final Normal normal = (Normal) example;
		setState(normal.mean, normal.standardDeviation);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " mean:" + mean + " stdev:"
				+ standardDeviation;
	}
}