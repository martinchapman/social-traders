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

/**
 * <p>
 * A policy framework to choose multiple actions out of a set of discrete
 * actions based on expected returns of these actions.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.1 $
 */

public abstract class MultiActionChoosingPolicy implements LearningPolicy {

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
	 * Request that the learner perform an action. Users of the learning algorithm
	 * should invoke this method on the learner when they wish to find out which
	 * action the learner is currently recommending.
	 * 
	 * @return An integer representing the action to be taken.
	 */
	public abstract int[] act(double returns[], int num);

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}