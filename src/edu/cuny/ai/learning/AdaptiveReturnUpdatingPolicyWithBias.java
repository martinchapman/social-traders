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

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Utils;

/**
 * <p>
 * An expected return updating policy that uses bias towards those untaken
 * actions each time.
 * </p>
 * 
 * <p>
 * This is motivated by the fact that a typical return calculation is using the
 * all-time reward average, which would give advantages to those actions that
 * become available only recently and exhibit high rewards. This policy uses a
 * fraction of the reward of the taken action each time as rewards for all the
 * untaken actions including those unavailable. This modification would provide
 * incentive for actions to become available as early as possible if they are
 * indeed good choices.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.untaken_action_biased</tt><br>
 * <font size=-1>boolean (<code>false</code> by default)</font></td>
 * <td valign=top>(whether to use a fraction of the reward of the taken action
 * as reward to those untaken actions)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.biasrate</tt><br>
 * <font size=-1>0 <= double <= 1 (<code>0.5</code> by default)</font></td>
 * <td valign=top>(the rate that controls what percentage of the reward of the
 * taken action is used as rewards for other actions)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>adaptive_return_updating_with_bias</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class AdaptiveReturnUpdatingPolicyWithBias extends
		AdaptiveReturnUpdatingPolicy {

	public static final String P_DEF_BASE = "adaptive_return_updating_with_bias";

	public static final String P_BIASRATE = "biasrate";

	public static final double DEFAULT_BIASRATE = 0.5;

	/*
	 * the percentage rate of reward of taken action for untaken actions
	 */
	protected double biasRate = AdaptiveReturnUpdatingPolicyWithBias.DEFAULT_BIASRATE;

	static Logger logger = Logger
			.getLogger(AdaptiveReturnUpdatingPolicyWithBias.class);

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(
				AdaptiveReturnUpdatingPolicyWithBias.P_DEF_BASE);
		biasRate = parameters.getDoubleWithDefault(base
				.push(AdaptiveReturnUpdatingPolicyWithBias.P_BIASRATE), defBase
				.push(AdaptiveReturnUpdatingPolicyWithBias.P_BIASRATE), biasRate);
		if ((biasRate > 1) && (biasRate < 0)) {
			biasRate = AdaptiveReturnUpdatingPolicyWithBias.DEFAULT_BIASRATE;
			AdaptiveReturnUpdatingPolicyWithBias.logger
					.error("bias rate should be a percentage value ! The default value, "
							+ AdaptiveReturnUpdatingPolicyWithBias.DEFAULT_BIASRATE
							+ " is used instead.");
		}
	}

	public double getBiasRate() {
		return biasRate;
	}

	public void setBiasRate(final double biasRate) {
		this.biasRate = biasRate;
	}

	@Override
	public void reward(final int action, final double reward) {
		super.reward(action, reward);

		/* feed biased rewards to untaken actions */
		for (int i = 0; i < learners.length; i++) {
			if ((action != i) && (learners[i] != null)) {
				/*
				 * note that when reward is negative, a better reward sent to untaken
				 * actions
				 */
				learners[i].train(reward * biasRate);
				returns[i] = learners[i].act();
			}
		}
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n"
				+ Utils.indent(AdaptiveReturnUpdatingPolicyWithBias.P_BIASRATE + ":"
						+ biasRate);
		return s;
	}
}