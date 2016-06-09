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

import cern.jet.random.Uniform;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.ApproxNumberComparator;
import edu.cuny.util.Galaxy;
import edu.cuny.util.MathUtil;
import edu.cuny.util.NumberComparator;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Utils;
import edu.cuny.util.io.DataWriter;

/**
 * This is deprecated. Please use {@link NArmedBanditLearner} with
 * {@link EpsilonGreedyActionChoosingPolicy} and
 * {@link AdaptiveReturnUpdatingPolicy} (with {@link AveragingLearner}) instead.
 * It is moved from the source and left here for test purposes.
 * 
 * <p>
 * A learner that implements the epsilon-greedy algorithm for n-armed bandit
 * problem described in Section 2.2, Sutton and Barto's RL book.
 * </p>
 * 
 * <p>
 * <code>epsilon</code> is constant when <code>alpha</code> is 1, or reducing
 * down to <code>minepsilon</code> when <code>alpha</code> is less than 1.
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
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.epsilon</tt><br>
 * <font size=-1>double (0,1) (0.1 by default)</font></td>
 * <td valign=top>(the probability that the actions except the best action are
 * randomly chosen)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.minepsilon</tt><br>
 * <font size=-1>double >=0</font></td>
 * <td valign=top>(the minimum value for epsilon)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.alpha</tt><br>
 * <font size=-1>double (0, 1]</font></td>
 * <td valign=top>(the reducing rate of epsilon over time)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>epsilon_greedy_learner</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public class EpsilonGreedyLearner extends AbstractLearner implements
		ExposedStimuliResponseLearner, Serializable, Prototypeable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(EpsilonGreedyLearner.class);

	public static final String P_DEF_BASE = "epsilon_greedy_learner";

	public static final String P_K = "k";

	public static final String P_EPSILON = "epsilon";

	public static final String P_MINEPSILON = "minepsilon";

	public static final String P_ALPHA = "alpha";

	/**
	 * number of choices
	 */
	protected int numActions;

	/**
	 * initial value of {@link #epsilon}
	 */
	protected double initialEpsilon;

	/**
	 * the minimum value to which {@link #epsilon} can decrease down.
	 */
	protected double minEpsilon;

	/**
	 * the probability the best action is not chosen.
	 */
	protected double epsilon;

	/**
	 * decreasing rate of {@link #epsilon}
	 */
	protected double alpha;

	/**
	 * the average rewards of actions
	 */
	protected double[] q;

	/**
	 * the action taken last time.
	 */
	protected int lastAction;

	/**
	 * the numbers of actions selected.
	 */
	protected int[] times;

	protected Uniform distribution;

	protected NumberComparator comparator;

	public static final int DEFAULT_NUM_ACTIONS = 10;

	public static final double DEFAULT_EPSILON = 0.1;

	public static final double DEFAULT_MIN_EPSILON = 0;

	public static final double DEFAULT_ALPHA = 1;

	public EpsilonGreedyLearner() {
		this(EpsilonGreedyLearner.DEFAULT_NUM_ACTIONS,
				EpsilonGreedyLearner.DEFAULT_EPSILON);
	}

	public EpsilonGreedyLearner(final int numActions, final double epsilon) {
		this.numActions = numActions;
		this.epsilon = epsilon;

		distribution = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
		comparator = new ApproxNumberComparator(
				ApproxNumberComparator.DEFAULT_LARGE_ERROR);

	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(EpsilonGreedyLearner.P_DEF_BASE);

		numActions = parameters.getIntWithDefault(base
				.push(EpsilonGreedyLearner.P_K),
				defBase.push(EpsilonGreedyLearner.P_K),
				EpsilonGreedyLearner.DEFAULT_NUM_ACTIONS);
		if (numActions < 1) {
			EpsilonGreedyLearner.logger
					.error("The number of actions available must be a positive integer !");
			numActions = EpsilonGreedyLearner.DEFAULT_NUM_ACTIONS;
		}

		initialEpsilon = parameters.getDoubleWithDefault(base
				.push(EpsilonGreedyLearner.P_EPSILON), defBase
				.push(EpsilonGreedyLearner.P_EPSILON),
				EpsilonGreedyLearner.DEFAULT_EPSILON);
		if ((initialEpsilon < 0) || (initialEpsilon > 1)) {
			EpsilonGreedyLearner.logger
					.error("epsilon must be a double between 0 and 1 !");
			initialEpsilon = EpsilonGreedyLearner.DEFAULT_EPSILON;
		}

		minEpsilon = parameters.getDoubleWithDefault(base
				.push(EpsilonGreedyLearner.P_MINEPSILON), defBase
				.push(EpsilonGreedyLearner.P_MINEPSILON),
				EpsilonGreedyLearner.DEFAULT_MIN_EPSILON);
		if ((minEpsilon < 0) || (minEpsilon > 1)) {
			EpsilonGreedyLearner.logger
					.error("minepsilon must be a double between 0 and 1 !");
			minEpsilon = EpsilonGreedyLearner.DEFAULT_MIN_EPSILON;
		}

		alpha = parameters
				.getDoubleWithDefault(base.push(EpsilonGreedyLearner.P_ALPHA), defBase
						.push(EpsilonGreedyLearner.P_ALPHA),
						EpsilonGreedyLearner.DEFAULT_ALPHA);
		if ((alpha < 0) || (alpha > 1)) {
			EpsilonGreedyLearner.logger
					.error("alpha must be a double between 0 and 1 !");
			alpha = EpsilonGreedyLearner.DEFAULT_ALPHA;
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		init1();
	}

	private void init1() {
		times = new int[numActions];
		q = new double[numActions];
		epsilon = initialEpsilon;

		lastAction = -1;
	}

	public void reset() {
		init1();
	}

	public Object protoClone() {
		try {
			// TODO:
			return clone();
		} catch (final CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	private int bestAction(final double returns[]) {
		return MathUtil.maxIndex(returns, distribution, comparator);
	}

	public int act() {
		final double rand = distribution.nextDouble();
		if (rand < epsilon) {
			lastAction = distribution.nextIntFromTo(0, numActions - 1);
		} else {
			lastAction = bestAction(q);
		}

		updateEpsilon();

		return lastAction;
	}

	public int act(final Set<Integer> actions) {
		final Integer indices[] = actions.toArray(new Integer[0]);

		if (indices.length == 0) {
			return -1;
		}

		final double rand = distribution.nextDouble();
		int index;
		if (rand < epsilon) {
			index = distribution.nextIntFromTo(0, indices.length - 1);
		} else {
			final double tempQ[] = new double[indices.length];
			for (int i = 0; i < tempQ.length; i++) {
				tempQ[i] = q[(indices[i]).intValue()];
			}
			index = bestAction(tempQ);
		}

		lastAction = (indices[index]).intValue();

		updateEpsilon();

		return lastAction;
	}

	protected void updateEpsilon() {
		if (epsilon > minEpsilon) {
			epsilon *= alpha;
		}

		if (epsilon < minEpsilon) {
			epsilon = minEpsilon;
		}
	}

	public void reward(final double reward) {
		/* use all-time average as q */
		q[lastAction] = q[lastAction] + (reward - q[lastAction])
				/ (1 + times[lastAction]);
		times[lastAction]++;
	}

	@Override
	public double getLearningDelta() {
		// TODO:
		return 0.0;
	}

	/**
	 * for debugging purpose only.
	 */
	public double[] getReturns() {
		return q;
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

	public double getMinEpsilon() {
		return minEpsilon;
	}

	public void setMinEpsilon(final double minEpsilon) {
		this.minEpsilon = minEpsilon;
	}

	public double getInitialEpsilon() {
		return initialEpsilon;
	}

	public void setInitialEpsilon(final double initialEpsilon) {
		this.initialEpsilon = initialEpsilon;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(final double alpha) {
		this.alpha = alpha;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n"
				+ Utils.indent("epsilon:" + initialEpsilon + " minEpsilon:"
						+ minEpsilon + " alpha:" + alpha + " k:" + numActions);
		return s;
	}
}
