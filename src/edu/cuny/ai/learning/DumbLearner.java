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

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;
import edu.cuny.util.io.DataWriter;

/**
 * A learner that chooses the same specified action on every iteration.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.action</tt><br>
 * <font size=-1>int</font></td>
 * <td valign=top>(the action always to choose)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>dumb_learner</tt></td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.12 $
 */

public class DumbLearner extends AbstractLearner implements DiscreteLearner,
		Parameterizable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int action;

	static final String P_ACTION = "action";

	public static final String P_DEF_BASE = "dumb_learner";

	public DumbLearner() {
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);
		action = parameters.getInt(base.push(DumbLearner.P_ACTION), new Parameter(
				DumbLearner.P_DEF_BASE).push(DumbLearner.P_ACTION), 0);
	}

	public void reset() {
		// do nothing
	}

	public void setAction(final int action) {
		this.action = action;
	}

	public int getAction() {
		return action;
	}

	public int act() {
		return action;
	}

	public int act(final Set<Integer> actions) {
		if (actions.contains(new Integer(action))) {
			return action;
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
		return 1;
	}

	public void setNumberOfActions(final int numActions) {
		// simply overlook the number of actions
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n" + Utils.indent(DumbLearner.P_ACTION + ":" + action);
		return s;
	}
}
