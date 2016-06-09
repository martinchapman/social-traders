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

import cern.jet.random.Uniform;

import org.apache.log4j.Logger;

/**
 * <p>
 * A policy used by {@link StimuliResponseLearner} to update the expected
 * returns of multiple discrete actions.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public abstract class ReturnUpdatingPolicy implements LearningPolicy {

	public static final String P_DEF_BASE = "return_updating";

	static Logger logger = Logger.getLogger(ReturnUpdatingPolicy.class);

	/**
	 * the learner that uses this policy
	 */
	protected StimuliResponseLearner parentLearner;

	/**
	 * an array of current expected returns with each for an action
	 */
	protected double returns[];

	protected Uniform distribution;

	protected final static double DEFAULT_SCALE = 0.01;

	public void initialize() {
		init1();
	}

	private void init1() {
		returns = new double[parentLearner.getNumberOfActions()];
	}

	public void reset() {
		init1();
	}

	public Object protoClone() {
		try {
			final ReturnUpdatingPolicy copy = (ReturnUpdatingPolicy) clone();
			copy.returns = returns.clone();
			return copy;
		} catch (final CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public StimuliResponseLearner getParentLearner() {
		return parentLearner;
	}

	public void setParentLearner(final StimuliResponseLearner parentLearner) {
		this.parentLearner = parentLearner;
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
	public abstract void reward(int action, double reward);

	/**
	 * 
	 * @return the expected returns of actions, which should not be changed
	 *         externally.
	 */
	public double[] getReturns() {
		return returns;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
