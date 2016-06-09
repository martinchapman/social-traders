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

package edu.cuny.cat.stat;

import org.apache.log4j.Logger;

import cern.jet.random.Uniform;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * A {@link ScoreDaysCondition} with which game days after a certain point are
 * counted with a certain probability for scoring.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.start</tt><br>
 * <font size=-1>int >=0 (0 by default)</font></td>
 * <td valign=top>(the starting day to be considered with certain probability
 * for scoring)</td>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.prob</tt><br>
 * <font size=-1>double in [0,1] (0.1 by default)</font></td>
 * <td valign=top>(the probability with which to consider whether a gaming day
 * is used for scoring)</td>
 * <tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.13 $
 */

public class DefaultScoreDaysCondition extends AbstractScoreDaysCondition {

	static final Logger logger = Logger
			.getLogger(DefaultScoreDaysCondition.class);

	public static final String P_START = "start";

	public static final String P_PROB = "prob";

	int start = 0;

	protected double prob = 0.1;

	protected Uniform distribution;

	public DefaultScoreDaysCondition() {
		distribution = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		start = parameters.getIntWithDefault(base
				.push(DefaultScoreDaysCondition.P_START), null, start);
		prob = parameters.getDoubleWithDefault(base
				.push(DefaultScoreDaysCondition.P_PROB), null, prob);
	}

	@Override
	protected boolean updateTaken(final int day) {
		if (day >= start) {
			if (prob >= distribution.nextDouble()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return super.toString() + " start:" + start + " prob:" + prob;
	}
}
