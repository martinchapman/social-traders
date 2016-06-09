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

/**
 * <p>
 * A policy framework used by {@link DiscreteLearner} to choose an action out of
 * multiple discrete actions.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public abstract class ActionChoosingPolicy implements LearningPolicy {

	public static final String P_DEF_BASE = "action_choosing";

	/**
	 * the learner that uses this policy
	 */
	protected DiscreteLearner parentLearner;

	public DiscreteLearner getParentLearner() {
		return parentLearner;
	}

	public void setParentLearner(final DiscreteLearner parentLearner) {
		this.parentLearner = parentLearner;
	}

	/**
	 * calculates the probabilities of actions being chosen based on the given
	 * returns.
	 * 
	 * @param returns
	 * 
	 * @return the probabilities of actions being chosen.
	 */
	public abstract double[] getProbabilities(double returns[]);

	// TODO:
	// /**
	// * calculates the probabilities of available actions being chosen based on
	// the
	// * given returns.
	// *
	// * @param returns
	// * @param actions
	// * the set of actions available.
	// *
	// * @return the probabilities of actions being chosen.
	// */
	// public abstract double[] getProbabilities(double returns[],
	// Set<Integer> actions);

	/**
	 * Request that the learner perform an action. Users of the learning algorithm
	 * should invoke this method on the learner when they wish to find out which
	 * action the learner is currently recommending.
	 * 
	 * @return An integer representing the action to be taken.
	 */
	public abstract int act(double returns[]);

	/**
	 * Request that the learner perform an action that is available.
	 * 
	 * @param actions
	 *          specifies which actions are available at the moment; contains the
	 *          indices of actions available
	 * 
	 * @return An integer representing the action to be taken; -1 if no action can
	 *         be chosen.
	 * 
	 * @see #act(double[])
	 */
	public abstract int act(double returns[], Set<Integer> actions);

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}