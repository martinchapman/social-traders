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
 * href="http://en.wikipedia.org/wiki/Student%27s_t-distribution">Student's
 * t-distribution</a>.
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
 * <td valign=top></td>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>studentt</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class StudentT extends cern.jet.random.StudentT implements
		Parameterizable, StateCopyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(StudentT.class);

	public static final String P_FREEDOM = "freedom";

	public static final String P_DEF_BASE = "studentt";

	public static final double DEFAULT_FREEDOM = 1;

	public StudentT() {
		this(StudentT.DEFAULT_FREEDOM, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	public StudentT(final double freedom, final RandomEngine randomGenerator) {
		super(freedom, randomGenerator);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(StudentT.P_DEF_BASE);

		final double freedom = parameters.getDoubleWithDefault(base
				.push(StudentT.P_FREEDOM), defBase.push(StudentT.P_FREEDOM),
				StudentT.DEFAULT_FREEDOM);

		setState(freedom);
	}

	public void copyStateFrom(final Object example) {
		final StudentT st = (StudentT) example;
		setState(st.freedom);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " freedom:" + freedom;
	}
}