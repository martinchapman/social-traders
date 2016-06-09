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
 * href="http://en.wikipedia.org/wiki/Zeta_distribution">zeta distribution</a>.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.ro</tt><br>
 * <font size=-1>double > 0 (1 by default)</font></td>
 * <td valign=top></td>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.pk</tt><br>
 * <font size=-1>double >= 0 (1 by default)</font></td>
 * <td valign=top></td>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>zeta</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class Zeta extends cern.jet.random.Zeta implements Parameterizable,
		StateCopyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(Zeta.class);

	public static final String P_RO = "ro";

	public static final String P_PK = "pk";

	public static final String P_DEF_BASE = "zeta";

	public static final int DEFAULT_RO = 1;

	public static final double DEFAULT_PK = 1;

	public Zeta() {
		this(Zeta.DEFAULT_RO, Zeta.DEFAULT_PK, Galaxy.getInstance()
				.getDefaultTyped(GlobalPRNG.class).getEngine());
	}

	public Zeta(final double ro, final double pk,
			final RandomEngine randomGenerator) {
		super(ro, pk, randomGenerator);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(Zeta.P_DEF_BASE);

		final double ro = parameters.getIntWithDefault(base.push(Zeta.P_RO),
				defBase.push(Zeta.P_RO), Zeta.DEFAULT_RO);
		final double pk = parameters.getDoubleWithDefault(base.push(Zeta.P_PK),
				defBase.push(Zeta.P_PK), Zeta.DEFAULT_PK);

		setState(ro, pk);
	}

	public void copyStateFrom(final Object example) {
		final Zeta zeta = (Zeta) example;
		setState(zeta.ro, zeta.pk);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " ro:" + ro + " pk:" + pk;
	}
}