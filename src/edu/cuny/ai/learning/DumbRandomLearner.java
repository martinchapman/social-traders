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
 * JASA Java Auction Simulator API
 * Copyright (C) 2001-2005 Steve Phelps
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

package edu.cuny.ai.learning;

import java.io.Serializable;
import java.util.Set;

import cern.jet.random.Uniform;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Utils;
import edu.cuny.util.io.DataWriter;

/**
 * A learner that simply plays a random action on each iteration without any
 * learning. This is useful for control experiments.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.k</tt><br>
 * <font size=-1>int</font></td>
 * <td valign=top>(the number of actions to choose from)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>dumb_random_learner</tt></td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.14 $
 */

public class DumbRandomLearner extends AbstractLearner implements
		Parameterizable, StimuliResponseLearner, Serializable, Prototypeable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int numActions;

	protected Uniform distribution;

	public static final int DEFAULT_NUM_ACTIONS = 10;

	public static final String P_K = "k";

	public static final String P_DEF_BASE = "dumb_random_learner";

	public DumbRandomLearner() {
		this(DumbRandomLearner.DEFAULT_NUM_ACTIONS);
	}

	public DumbRandomLearner(final int numActions) {
		this.numActions = numActions;
		distribution = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	@Override
	public void setup(final ParameterDatabase params, final Parameter base) {
		numActions = params
				.getIntWithDefault(base.push(DumbRandomLearner.P_K), new Parameter(
						DumbRandomLearner.P_DEF_BASE).push(DumbRandomLearner.P_K),
						DumbRandomLearner.DEFAULT_NUM_ACTIONS);
	}

	public void reset() {
		// Do nothing
	}

	public Object protoClone() {
		try {
			return clone();
		} catch (final CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	public int act() {
		return distribution.nextIntFromTo(0, numActions - 1);
	}

	public int act(final Set<Integer> actions) {
		final Integer indices[] = actions.toArray(new Integer[0]);
		if (indices.length > 0) {
			final int i = distribution.nextIntFromTo(0, indices.length - 1);
			return indices[i].intValue();
		} else {
			return -1;
		}
	}

	@Override
	public double getLearningDelta() {
		return 0.0;
	}

	@Override
	public void dumpState(final DataWriter out) {
		// TODO
	}

	public int getNumberOfActions() {
		return numActions;
	}

	public void setNumberOfActions(final int numActions) {
		this.numActions = numActions;
	}

	public void reward(final double reward) {
		// No action
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n" + Utils.indent(DumbRandomLearner.P_K + ":" + numActions);
		return s;
	}
}
