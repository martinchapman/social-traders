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

import java.util.Set;

/**
 * A learner that learns a discrete number of different actions.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.10 $
 */

public interface DiscreteLearner extends Learner {

	/**
	 * Request that the learner perform an action. Users of the learning algorithm
	 * should invoke this method on the learner when they wish to find out which
	 * action the learner is currently recommending.
	 * 
	 * @return An integer representing the action to be taken.
	 */
	public int act();

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
	 * @see #act()
	 */
	public int act(Set<Integer> actions);

	/**
	 * Get the number of different possible actions this learner can choose from
	 * when it performs an action.
	 * 
	 * @return An integer value representing the number of actions available.
	 */
	public int getNumberOfActions();

	/**
	 * Set the number of different possible actions this learner can choose from
	 * when it performs an action.
	 * 
	 * @param numActions
	 *          An integer value representing the number of actions available.
	 */
	public void setNumberOfActions(int numActions);
}