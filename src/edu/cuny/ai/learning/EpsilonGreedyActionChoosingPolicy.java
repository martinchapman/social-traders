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
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * <p>
 * A policy used by {@link StimuliResponseLearner} to update the expected
 * returns of multiple discrete actions based on the epsilon-greedy method in
 * Section 2.2, Sutton and Barto's RL book
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
 * <td valign=top><i>base</i><tt>.epsilon</tt><br>
 * <font size=-1>double in (0,1) (<code>0.1</code> by default)</font></td>
 * <td valign=top>(the probability that the actions except the best action are
 * randomly chosen)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.minepsilon</tt><br>
 * <font size=-1>double >= 0 (<code>0</code> by default)</font></td>
 * <td valign=top>(the minimum value for epsilon)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.alpha</tt><br>
 * <font size=-1>double in (0,1] (<code>1</code> by default)</font></td>
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
 * <td valign=top><tt>epsilon_greedy_action_choosing</tt></td>
 * </tr>
 * </table>
 * 
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class EpsilonGreedyActionChoosingPolicy extends ActionChoosingPolicy
		implements Parameterizable {

	static Logger logger = Logger
			.getLogger(EpsilonGreedyActionChoosingPolicy.class);

	public static final String P_DEF_BASE = "epsilon_greedy_action_choosing";

	public static final String P_EPSILON = "epsilon";

	public static final String P_MINEPSILON = "minepsilon";

	public static final String P_ALPHA = "alpha";

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

	protected Uniform distribution;

	protected NumberComparator comparator;

	public static final double DEFAULT_EPSILON = 0.1;

	public static final double DEFAULT_MIN_EPSILON = 0;

	public static final double DEFAULT_ALPHA = 1;

	public EpsilonGreedyActionChoosingPolicy() {
		this(EpsilonGreedyActionChoosingPolicy.DEFAULT_EPSILON);
	}

	public EpsilonGreedyActionChoosingPolicy(final double initialEpsilon) {
		this.initialEpsilon = initialEpsilon;

		distribution = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
		comparator = new ApproxNumberComparator(
				ApproxNumberComparator.DEFAULT_LARGE_ERROR);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(
				EpsilonGreedyActionChoosingPolicy.P_DEF_BASE);

		initialEpsilon = parameters.getDoubleWithDefault(base
				.push(EpsilonGreedyActionChoosingPolicy.P_EPSILON), defBase
				.push(EpsilonGreedyActionChoosingPolicy.P_EPSILON),
				EpsilonGreedyActionChoosingPolicy.DEFAULT_EPSILON);
		if ((initialEpsilon < 0) || (initialEpsilon > 1)) {
			EpsilonGreedyActionChoosingPolicy.logger
					.error("epsilon must be a double between 0 and 1 !");
			initialEpsilon = EpsilonGreedyActionChoosingPolicy.DEFAULT_EPSILON;
		}

		minEpsilon = parameters.getDoubleWithDefault(base
				.push(EpsilonGreedyActionChoosingPolicy.P_MINEPSILON), defBase
				.push(EpsilonGreedyActionChoosingPolicy.P_MINEPSILON),
				EpsilonGreedyActionChoosingPolicy.DEFAULT_MIN_EPSILON);
		if ((minEpsilon < 0) || (minEpsilon > 1)) {
			EpsilonGreedyActionChoosingPolicy.logger
					.error("minepsilon must be a double between 0 and 1 !");
			minEpsilon = EpsilonGreedyActionChoosingPolicy.DEFAULT_MIN_EPSILON;
		}

		alpha = parameters.getDoubleWithDefault(base
				.push(EpsilonGreedyActionChoosingPolicy.P_ALPHA), defBase
				.push(EpsilonGreedyActionChoosingPolicy.P_ALPHA),
				EpsilonGreedyActionChoosingPolicy.DEFAULT_ALPHA);
		if ((alpha < 0) || (alpha > 1)) {
			EpsilonGreedyActionChoosingPolicy.logger
					.error("alpha must be a double between 0 and 1 !");
			alpha = EpsilonGreedyActionChoosingPolicy.DEFAULT_ALPHA;
		}
	}

	public void initialize() {
		init1();
	}

	private void init1() {
		epsilon = initialEpsilon;
	}

	public void reset() {
		init1();
	}

	public Object protoClone() {
		try {
			final EpsilonGreedyActionChoosingPolicy copy = (EpsilonGreedyActionChoosingPolicy) clone();
			copy.distribution = (Uniform) distribution.clone();
			return copy;
		} catch (final CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public double getEpsilon() {
		return epsilon;
	}

	private int bestAction(final double returns[]) {
		return MathUtil.maxIndex(returns, distribution, comparator);
	}

	@Override
	public int act(final double returns[]) {
		final int lastAction = internalAct(returns);

		updateEpsilon();

		return lastAction;
	}

	protected int internalAct(final double returns[]) {
		int lastAction = -1;
		final double rand = distribution.nextDouble();
		if (rand < epsilon) {
			lastAction = distribution.nextIntFromTo(0, returns.length - 1);
		} else {
			lastAction = bestAction(returns);
		}

		return lastAction;
	}

	@Override
	public int act(final double returns[], final Set<Integer> actions) {
		Integer indices[] = new Integer[actions.size()];
		indices = actions.toArray(indices);

		if (indices.length == 0) {
			return -1;
		}

		int lastAction = -1;
		final double rand = distribution.nextDouble();
		int index;
		if (rand < epsilon) {
			index = distribution.nextIntFromTo(0, indices.length - 1);
		} else {
			final double tempReturns[] = new double[indices.length];
			for (int i = 0; i < tempReturns.length; i++) {
				tempReturns[i] = returns[indices[i].intValue()];
			}
			index = bestAction(tempReturns);
		}

		lastAction = indices[index].intValue();

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

	@Override
	public double[] getProbabilities(final double[] returns) {
		double value = 1.0;
		if ((returns != null) && (returns.length > 0)) {
			value = epsilon / returns.length;
		} else {
			return null;
		}

		final double[] probs = Utils.newDuplicateArray(value, returns.length);

		probs[MathUtil.maxIndex(returns, comparator)] += 1 - epsilon;
		return probs;
	}

	public double getInitialEpsilon() {
		return initialEpsilon;
	}

	public void setInitialEpsilon(final double initialEpsilon) {
		this.initialEpsilon = initialEpsilon;
	}

	public double getMinEpsilon() {
		return minEpsilon;
	}

	public void setMinEpsilon(final double minEpsilon) {
		this.minEpsilon = minEpsilon;
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

		s += " " + EpsilonGreedyActionChoosingPolicy.P_EPSILON + ":"
				+ initialEpsilon;
		s += " " + EpsilonGreedyActionChoosingPolicy.P_MINEPSILON + ":"
				+ minEpsilon;
		s += " " + EpsilonGreedyActionChoosingPolicy.P_ALPHA + ":" + alpha;

		return s;
	}
}