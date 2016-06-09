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
 * href="http://en.wikipedia.org/wiki/Hypergeometric_distribution"
 * >hypergeometric distribution</a>.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.N</tt><br>
 * <font size=-1>int >= 2 (2 by default)</font></td>
 * <td valign=top></td>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.s</tt><br>
 * <font size=-1>int <= N (1 by default)</font></td>
 * <td valign=top></td>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.n</tt><br>
 * <font size=-1>int <= N (1 by default)</font></td>
 * <td valign=top></td>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>hypergeometric</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class HyperGeometric extends cern.jet.random.HyperGeometric implements
		Parameterizable, StateCopyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(HyperGeometric.class);

	public static final String P_N = "N";

	public static final String P_s = "s";

	public static final String P_n = "n";

	public static final String P_DEF_BASE = "hypergeometric";

	public static final int DEFAULT_N = 2;

	public static final int DEFAULT_s = 1;

	public static final int DEFAULT_n = 1;

	public HyperGeometric() {
		this(HyperGeometric.DEFAULT_N, HyperGeometric.DEFAULT_s,
				HyperGeometric.DEFAULT_n, Galaxy.getInstance().getDefaultTyped(
						GlobalPRNG.class).getEngine());
	}

	public HyperGeometric(final int N, final int s, final int n,
			final RandomEngine randomGenerator) {
		super(N, s, n, randomGenerator);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(HyperGeometric.P_DEF_BASE);

		final int N = parameters.getIntWithDefault(base.push(HyperGeometric.P_N),
				defBase.push(HyperGeometric.P_N), HyperGeometric.DEFAULT_N);
		final int s = parameters.getIntWithDefault(base.push(HyperGeometric.P_s),
				defBase.push(HyperGeometric.P_s), HyperGeometric.DEFAULT_s);
		final int n = parameters.getIntWithDefault(base.push(HyperGeometric.P_n),
				defBase.push(HyperGeometric.P_n), HyperGeometric.DEFAULT_n);

		setState(N, s, n);
	}

	public void copyStateFrom(final Object example) {
		final HyperGeometric hg = (HyperGeometric) example;
		hg.setState(hg.my_N, hg.my_s, hg.my_n);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " N:" + my_N + " s:" + my_s + " n:"
				+ my_n;
	}
}