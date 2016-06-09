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
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Resetable;
import edu.cuny.util.Utils;
import edu.cuny.util.io.DataWriter;

/**
 * A memory-less version of the Q-Learning algorithm.
 * 
 * This class implements {@link StimuliResponseLearner} instead of
 * {@link MDPLearner}, and so can be used in place of, e.g. a
 * {@link RothErevLearner}.
 * 
 * We use the standard MDP QLearner class, but fool it with this wrapper into
 * thinking that there is only one status.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.11 $
 */

public class StatelessQLearner extends AbstractLearner implements
		StimuliResponseLearner, Parameterizable, Resetable, Serializable,
		Prototypeable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	QLearner qLearner;

	public StatelessQLearner() {
		qLearner = new QLearner();
	}

	public StatelessQLearner(final int numActions, final double epsilon,
			final double learningRate, final double discountRate) {

		qLearner = new QLearner(1, numActions, epsilon, learningRate, discountRate);
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(QLearner.P_DEF_BASE);

		final double learningRate = parameters.getDoubleWithDefault(base
				.push(QLearner.P_LEARNING_RATE),
				defBase.push(QLearner.P_LEARNING_RATE), QLearner.DEFAULT_LEARNING_RATE);

		final double discountRate = parameters.getDoubleWithDefault(base
				.push(QLearner.P_DISCOUNT_RATE),
				defBase.push(QLearner.P_DISCOUNT_RATE), QLearner.DEFAULT_DISCOUNT_RATE);

		final double epsilon = parameters.getDoubleWithDefault(base
				.push(QLearner.P_EPSILON), defBase.push(QLearner.P_EPSILON),
				QLearner.DEFAULT_EPSILON);

		final int numActions = parameters.getInt(base.push(QLearner.P_NUM_ACTIONS),
				defBase.push(QLearner.P_NUM_ACTIONS));

		qLearner.setStatesAndActions(1, numActions);
		qLearner.setLearningRate(learningRate);
		qLearner.setEpsilon(epsilon);
		qLearner.setDiscountRate(discountRate);
		qLearner.initialize();
	}

	public void reset() {
		qLearner.reset();
	}

	public int act() {
		return qLearner.act();
	}

	public int act(final Set<Integer> actions) {
		return qLearner.act(actions);
	}

	public void reward(final double reward) {
		qLearner.newState(reward, 0);
	}

	@Override
	public double getLearningDelta() {
		return qLearner.getLearningDelta();
	}

	public int getNumberOfActions() {
		return qLearner.getNumberOfActions();
	}

	public void setNumberOfActions(final int n) {
		qLearner.setStatesAndActions(1, n);
	}

	@Override
	public void dumpState(final DataWriter out) {
		qLearner.dumpState(out);
	}

	public Object protoClone() {
		try {
			final StatelessQLearner cloned = (StatelessQLearner) clone();
			cloned.qLearner = (QLearner) qLearner.protoClone();
			return cloned;
		} catch (final CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n" + Utils.indent(qLearner.toString());
		return s;
	}

}