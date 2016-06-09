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

import org.apache.log4j.Logger;

import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Resetable;
import edu.cuny.util.Utils;
import edu.cuny.util.io.DataWriter;

/**
 * <p>
 * An implementation of the Q-learning algorithm, with epsilon-greedy
 * exploration.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.k</tt><br>
 * <font size=-1>int >= 0</font></td>
 * <td valign=top>(the number of a possible actions)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.s</tt><br>
 * <font size=-1>int >= 0</font></td>
 * <td valign=top>(the number of states)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.e</tt><br>
 * <font size=-1>double [0,1]</font></td>
 * <td valign=top>(the epsilon parameter)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.p</tt><br>
 * <font size=-1>double [0,1]</font></td>
 * <td valign=top>(the learning rate)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.g</tt><br>
 * <font size=-1>double [0,1]</font></td>
 * <td valign=top>(the discount rate)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>qlearner</tt></td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.17 $
 */

public class QLearner extends AbstractLearner implements MDPLearner, Resetable,
		Serializable, Parameterizable, Prototypeable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The number of possible states
	 */
	protected int numStates;

	/**
	 * The number of possible actions
	 */
	protected int numActions;

	/**
	 * The matrix representing the estimated payoff of each possible action in
	 * each possible status.
	 */
	protected double q[][];

	/**
	 * The learning rate.
	 */
	protected double learningRate;

	/**
	 * The discount rate for future payoffs.
	 */
	protected double discountRate;

	/**
	 * The parameter representing the probability of choosing a random action on
	 * any given iteration.
	 */
	protected double epsilon;

	/**
	 * The previous status
	 */
	protected int previousState;

	/**
	 * The current status
	 */
	protected int currentState;

	/**
	 * The last action that was chosen.
	 */
	protected int lastActionChosen;

	/**
	 * The best action for the current status
	 */
	protected int bestAction;

	protected Uniform randomActionDistribution;

	public static final String P_DEF_BASE = "q_learner";

	public static final double DEFAULT_EPSILON = 0.2;

	public static final double DEFAULT_LEARNING_RATE = 0.5;

	public static final double DEFAULT_DISCOUNT_RATE = 0.8;

	public static final String P_EPSILON = "e";

	public static final String P_LEARNING_RATE = "p";

	public static final String P_DISCOUNT_RATE = "g";

	public static final String P_NUM_ACTIONS = "k";

	public static final String P_NUM_STATES = "s";

	static Logger logger = Logger.getLogger(QLearner.class);

	public QLearner() {
		this(0, 0, QLearner.DEFAULT_EPSILON, QLearner.DEFAULT_LEARNING_RATE,
				QLearner.DEFAULT_DISCOUNT_RATE);
	}

	public QLearner(final int numStates, final int numActions,
			final double epsilon, final double learningRate, final double discountRate) {
		this.numStates = numStates;
		this.numActions = numActions;
		this.learningRate = learningRate;
		this.discountRate = discountRate;
		this.epsilon = epsilon;

		randomActionDistribution = new Uniform(Galaxy.getInstance()
				.getDefaultTyped(GlobalPRNG.class).getEngine());

	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(QLearner.P_DEF_BASE);

		learningRate = parameters.getDoubleWithDefault(base
				.push(QLearner.P_LEARNING_RATE),
				defBase.push(QLearner.P_LEARNING_RATE), QLearner.DEFAULT_LEARNING_RATE);

		discountRate = parameters.getDoubleWithDefault(base
				.push(QLearner.P_DISCOUNT_RATE),
				defBase.push(QLearner.P_DISCOUNT_RATE), QLearner.DEFAULT_DISCOUNT_RATE);

		epsilon = parameters.getDoubleWithDefault(base.push(QLearner.P_EPSILON),
				defBase.push(QLearner.P_EPSILON), QLearner.DEFAULT_EPSILON);

		numStates = parameters.getInt(base.push(QLearner.P_NUM_STATES), defBase
				.push(QLearner.P_NUM_STATES));

		numActions = parameters.getInt(base.push(QLearner.P_NUM_ACTIONS), defBase
				.push(QLearner.P_NUM_ACTIONS));
	}

	@Override
	public void initialize() {
		super.initialize();

		q = new double[numStates][numActions];
		init1();
	}

	private void init1() {
		currentState = 0;
		previousState = 0;
		bestAction = 0;
		lastActionChosen = 0;
	}

	public void reset() {
		for (int s = 0; s < numStates; s++) {
			for (int a = 0; a < numActions; a++) {
				q[s][a] = 0;
			}
		}
		init1();
	}

	public Object protoClone() {
		try {
			final QLearner cloned = (QLearner) clone();
			return cloned;
		} catch (final CloneNotSupportedException e) {
			QLearner.logger.error(e.getMessage());
			throw new Error(e);
		}
	}

	public void setStatesAndActions(final int numStates, final int numActions) {
		this.numStates = numStates;
		this.numActions = numActions;
	}

	public void setState(final int newState) {
		previousState = currentState;
		currentState = newState;
	}

	public int getState() {
		return currentState;
	}

	public int act() {
		final RandomEngine prng = Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine();
		if (prng.raw() <= epsilon) {
			// lastActionChosen = prng.choose(0, numActions-1);
			lastActionChosen = randomActionDistribution.nextIntFromTo(0,
					numActions - 1);
		} else {
			lastActionChosen = bestAction(currentState);
		}
		return lastActionChosen;
	}

	/**
	 * TODO: has yet to implement
	 */
	public int act(final Set<Integer> actions) {
		QLearner.logger.error(getClass()
				+ ".act(Set actions) has yet to be implemented !");
		return -1;
	}

	public void newState(final double reward, final int newState) {
		updateQ(reward, newState);
		setState(newState);
	}

	protected void updateQ(final double reward, final int newState) {
		q[currentState][lastActionChosen] = learningRate
				* (reward + discountRate * maxQ(newState)) + (1 - learningRate)
				* q[currentState][lastActionChosen];
	}

	public double maxQ(final int newState) {
		double max = Double.NEGATIVE_INFINITY;
		for (int a = 0; a < numActions; a++) {
			if (q[newState][a] > max) {
				max = q[newState][a];
				bestAction = a;
			}
		}
		return max;
	}

	public int bestAction(final int state) {
		maxQ(state);
		return bestAction;
	}

	public void setDiscountRate(final double discountRate) {
		this.discountRate = discountRate;
	}

	public double getDiscountRate() {
		return discountRate;
	}

	public void setEpsilon(final double epsilon) {
		this.epsilon = epsilon;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public int getLastActionChosen() {
		return lastActionChosen;
	}

	@Override
	public double getLearningDelta() {
		return 0; // TODO
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
		initialize();
	}

	public int getNumberOfStates() {
		return numStates;
	}

	public void setNumberOfStates(final int numStates) {
		this.numStates = numStates;
		initialize();
	}

	public double getLearningRate() {
		return learningRate;
	}

	public void setLearningRate(final double learningRate) {
		this.learningRate = learningRate;
	}

	public int getPreviousState() {
		return previousState;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n"
				+ Utils.indent("lastActionChosen:" + lastActionChosen + " epsilon:"
						+ epsilon + " learningRate:" + learningRate + " discountRate:"
						+ discountRate);
		return s;
	}
}