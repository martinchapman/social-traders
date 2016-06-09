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

package edu.cuny.cat.trader.strategy;

import java.io.Serializable;

import edu.cuny.ai.learning.Learner;
import edu.cuny.ai.learning.StimuliResponseLearner;
import edu.cuny.cat.trader.AbstractTradingAgent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Utils;

/**
 * <p>
 * A trading strategy that uses a stimuli-response learning algorithm, such as
 * the Roth-Erev algorithm, to adapt its trading behaviour in successive auction
 * rounds by using the agent's profits in the last round as a reward signal.
 * </p>
 * 
 * </p>
 * <p>
 * <b>Parameters</b>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.learner</tt><br>
 * <font size=-1>classname, inherits
 * {@link edu.cuny.ai.learning.StimuliResponseLearner}</font></td>
 * <td valign=top>(the learning algorithm to use)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>stimuli_response_strategy</tt></td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.19 $
 */

public class StimuliResponseStrategy extends DiscreteLearnerStrategy implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The learning algorithm to use.
	 */
	protected StimuliResponseLearner learner = null;

	public static final String P_DEF_BASE = "stimuli_response_strategy";

	public static final String P_LEARNER = "learner";

	public StimuliResponseStrategy() {
		this(null);
	}

	public StimuliResponseStrategy(final AbstractTradingAgent agent) {
		super(agent);
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter learnerParameter = base
				.push(StimuliResponseStrategy.P_LEARNER);
		learner = parameters.getInstanceForParameter(learnerParameter,
				new Parameter(StimuliResponseStrategy.P_DEF_BASE)
						.push(StimuliResponseStrategy.P_LEARNER),
				StimuliResponseLearner.class);
		if (learner instanceof Parameterizable) {
			((Parameterizable) learner).setup(parameters, learnerParameter);
		}
		learner.initialize();
	}

	@Override
	public void reset() {
		super.reset();
		learner.reset();
	}

	@Override
	public Object protoClone() {
		StimuliResponseStrategy clonedStrategy;
		try {
			clonedStrategy = (StimuliResponseStrategy) clone();
			clonedStrategy.learner = (StimuliResponseLearner) ((Prototypeable) learner)
					.protoClone();
		} catch (final CloneNotSupportedException e) {
			throw new Error(e);
		}
		return clonedStrategy;
	}

	@Override
	public int act() {
		return learner.act();
	}

	@Override
	public void learn() {
		learner.reward(agent.getLastShoutProfit());
	}

	public Learner getLearner() {
		return learner;
	}

	public void setLearner(final Learner learner) {
		this.learner = (StimuliResponseLearner) learner;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n" + Utils.indent(learner.toString());
		return s;
	}

}
