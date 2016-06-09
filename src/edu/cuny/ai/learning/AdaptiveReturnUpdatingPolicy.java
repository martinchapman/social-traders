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

import org.apache.log4j.Logger;

import edu.cuny.util.ParamClassLoadException;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * <p>
 * A policy used by {@link StimuliResponseLearner} to update the expected
 * returns of multiple discrete actions.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.learner</tt><br>
 * <font size=-1>name of class, inheriting
 * {@link edu.cuny.ai.learning.MimicryLearner}</font></td>
 * <td valign=top>(type of learner used to update the expected return of an
 * action)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>adaptive_return_updating</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class AdaptiveReturnUpdatingPolicy extends ReturnUpdatingPolicy
		implements Parameterizable {

	public static final String P_DEF_BASE = "adaptive_return_updating";

	public static final String P_LEARNER = "learner";

	protected MimicryLearner learnerTemplate;

	protected MimicryLearner learners[];

	static Logger logger = Logger.getLogger(AdaptiveReturnUpdatingPolicy.class);

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(
				AdaptiveReturnUpdatingPolicy.P_DEF_BASE);

		try {
			learnerTemplate = parameters.getInstanceForParameter(base
					.push(AdaptiveReturnUpdatingPolicy.P_LEARNER), defBase
					.push(AdaptiveReturnUpdatingPolicy.P_LEARNER), MimicryLearner.class);
			if (learnerTemplate instanceof Parameterizable) {
				((Parameterizable) learnerTemplate).setup(parameters, base
						.push(AdaptiveReturnUpdatingPolicy.P_LEARNER));
			}
		} catch (final ParamClassLoadException e) {
			/* use all-time average as the default updating rule */
			learnerTemplate = new AveragingLearner();
		}
		learnerTemplate.initialize();
	}

	@Override
	public void initialize() {
		super.initialize();
		init1();
	}

	private void init1() {
		learners = new MimicryLearner[parentLearner.getNumberOfActions()];
		for (int i = 0; i < learners.length; i++) {
			learners[i] = (MimicryLearner) learnerTemplate.protoClone();
			learners[i].initialize();
		}
	}

	@Override
	public void reset() {
		super.reset();
		init1();
	}

	@Override
	public Object protoClone() {
		final AdaptiveReturnUpdatingPolicy copy = (AdaptiveReturnUpdatingPolicy) super
				.protoClone();
		copy.learnerTemplate = (MimicryLearner) copy.learnerTemplate.protoClone();
		copy.learners = new MimicryLearner[learners.length];
		for (int i = 0; i < learners.length; i++) {
			copy.learners[i] = (MimicryLearner) learners[i].protoClone();
		}

		return copy;
	}

	/**
	 * updates the returns of actions after taking an action and receiving a
	 * reward.
	 * 
	 * @param action
	 *          the action taken
	 * @param reward
	 *          the reward received after taking the action
	 */
	@Override
	public void reward(final int action, final double reward) {
		if ((action >= learners.length) || (action < 0)) {
			AdaptiveReturnUpdatingPolicy.logger.error("Invalid action: " + action
					+ " in " + getClass().getSimpleName());
		} else if (learners[action] == null) {
			AdaptiveReturnUpdatingPolicy.logger
					.error("Learner unavailable for action return updating !");
		} else {
			learners[action].train(reward);
			returns[action] = learners[action].act();
		}
	}

	public MimicryLearner getLearnerTemplate() {
		return learnerTemplate;
	}

	public void setLearnerTemplate(final MimicryLearner learnerTemplate) {
		this.learnerTemplate = learnerTemplate;
	}

	@Override
	public String toString() {
		String s = super.toString();

		if (learnerTemplate != null) {
			s += "\n" + Utils.indent(learnerTemplate.toString());
		}

		return s;
	}
}
