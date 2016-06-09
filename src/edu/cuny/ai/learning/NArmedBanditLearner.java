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

package edu.cuny.ai.learning;

import java.io.Serializable;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Utils;
import edu.cuny.util.io.DataWriter;

/**
 * <p>
 * A learner that implements a solution framework for the n-armed bandit problem
 * described in Section 2.2, Sutton and Barto's RL book.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.k</tt><br>
 * <font size=-1>int >=1</font></td>
 * <td valign=top>(number of actions)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.action_choosing</tt><br>
 * <font size=-1>name of class, inheriting
 * {@link edu.cuny.ai.learning.ActionChoosingPolicy}</font></td>
 * <td valign=top>(the policy used to choose an action out of an action set
 * based on their expected returns)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.return_updating</tt><br>
 * <font size=-1>name of class, inheriting
 * {@link edu.cuny.ai.learning.ReturnUpdatingPolicy}</font></td>
 * <td valign=top>(the policy used to calculate the expected returns of actions)
 * </td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>n_armed_bandit_learner</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class NArmedBanditLearner extends AbstractLearner implements
		ExposedStimuliResponseLearner, Serializable, Prototypeable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(NArmedBanditLearner.class);

	public static final String P_DEF_BASE = "n_armed_bandit_learner";

	public static final String P_K = "k";

	public static final int DEFAULT_NUM_ACTIONS = 10;

	/**
	 * number of choices
	 */
	protected int numActions = NArmedBanditLearner.DEFAULT_NUM_ACTIONS;

	/**
	 * the policy used to choose an action out of an action set based on the
	 * expected returns of these actions
	 */
	protected ActionChoosingPolicy actionChoosingPolicy;

	/**
	 * the policy used to update expected returns of actions
	 */
	protected ReturnUpdatingPolicy returnUpdatingPolicy;

	/**
	 * the action taken last time.
	 */
	protected int lastAction;

	public NArmedBanditLearner() {
		this(NArmedBanditLearner.DEFAULT_NUM_ACTIONS);
	}

	public NArmedBanditLearner(final int numActions) {
		this.numActions = numActions;
		init0();
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(NArmedBanditLearner.P_DEF_BASE);

		numActions = parameters.getIntWithDefault(base
				.push(NArmedBanditLearner.P_K), defBase.push(NArmedBanditLearner.P_K),
				numActions);
		if (numActions < 1) {
			NArmedBanditLearner.logger
					.error("The number of actions available must be a positive integer !");
			numActions = NArmedBanditLearner.DEFAULT_NUM_ACTIONS;
		}

		actionChoosingPolicy = parameters.getInstanceForParameter(base
				.push(ActionChoosingPolicy.P_DEF_BASE), defBase
				.push(ActionChoosingPolicy.P_DEF_BASE), ActionChoosingPolicy.class);
		actionChoosingPolicy.setParentLearner(this);
		if (actionChoosingPolicy instanceof Parameterizable) {
			((Parameterizable) actionChoosingPolicy).setup(parameters, base
					.push(ActionChoosingPolicy.P_DEF_BASE));
		}
		actionChoosingPolicy.initialize();

		returnUpdatingPolicy = parameters.getInstanceForParameter(base
				.push(ReturnUpdatingPolicy.P_DEF_BASE), defBase
				.push(ReturnUpdatingPolicy.P_DEF_BASE), ReturnUpdatingPolicy.class);
		returnUpdatingPolicy.setParentLearner(this);
		if (returnUpdatingPolicy instanceof Parameterizable) {
			((Parameterizable) returnUpdatingPolicy).setup(parameters, base
					.push(ReturnUpdatingPolicy.P_DEF_BASE));
		}
		returnUpdatingPolicy.initialize();
	}

	private void init0() {
		lastAction = -1;
	}

	public Object protoClone() {
		try {
			final NArmedBanditLearner copy = (NArmedBanditLearner) clone();
			copy.setActionChoosingPolicy((ActionChoosingPolicy) actionChoosingPolicy
					.protoClone());
			copy.setReturnUpdatingPolicy((ReturnUpdatingPolicy) returnUpdatingPolicy
					.protoClone());
			return copy;
		} catch (final CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	public void reset() {
		init0();
		actionChoosingPolicy.reset();
		returnUpdatingPolicy.reset();
	}

	public int act() {
		lastAction = actionChoosingPolicy.act(returnUpdatingPolicy.getReturns());
		return lastAction;
	}

	public int act(final Set<Integer> actions) {
		lastAction = actionChoosingPolicy.act(returnUpdatingPolicy.getReturns(),
				actions);
		return lastAction;
	}

	@Override
	public double getLearningDelta() {
		// TODO:
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
		/* use all-time average as q */
		returnUpdatingPolicy.reward(lastAction, reward);
	}

	public ReturnUpdatingPolicy getReturnUpdatingPolicy() {
		return returnUpdatingPolicy;
	}

	public void setReturnUpdatingPolicy(
			final ReturnUpdatingPolicy returnUpdatingPolicy) {
		this.returnUpdatingPolicy = returnUpdatingPolicy;
	}

	public ActionChoosingPolicy getActionChoosingPolicy() {
		return actionChoosingPolicy;
	}

	public void setActionChoosingPolicy(
			final ActionChoosingPolicy actionChoosingPolicy) {
		this.actionChoosingPolicy = actionChoosingPolicy;
	}

	/**
	 * for debugging purpose only.
	 */
	public double[] getReturns() {
		return returnUpdatingPolicy.getReturns();
	}

	@Override
	public String toString() {
		String s = super.toString();

		s += " k:" + numActions;

		if (returnUpdatingPolicy != null) {
			s += "\n" + Utils.indent(returnUpdatingPolicy.toString());
		}

		if (actionChoosingPolicy != null) {
			s += "\n" + Utils.indent(actionChoosingPolicy.toString());
		}

		return s;
	}
}
