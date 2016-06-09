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
import edu.cuny.util.Galaxy;
import edu.cuny.util.Utils;

/**
 * <p>
 * A policy used by {@link StimuliResponseLearner} to randomly choose an action
 * from the action set.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public class RandomActionChoosingPolicy extends ActionChoosingPolicy {

	static Logger logger = Logger.getLogger(RandomActionChoosingPolicy.class);

	protected Uniform distribution;

	public RandomActionChoosingPolicy() {
		distribution = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	public void initialize() {
		// do nothing
	}

	public void reset() {
		// do nothing
	}

	public Object protoClone() {
		try {
			final RandomActionChoosingPolicy copy = (RandomActionChoosingPolicy) clone();
			copy.distribution = (Uniform) distribution.clone();
			return copy;
		} catch (final CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int act(final double returns[]) {
		return distribution.nextIntFromTo(0, returns.length - 1);
	}

	@Override
	public int act(final double returns[], final Set<Integer> actions) {
		Integer indices[] = new Integer[actions.size()];
		indices = actions.toArray(indices);

		if (indices.length == 0) {
			return -1;
		}

		final int i = distribution.nextIntFromTo(0, indices.length);
		return indices[i].intValue();
	}

	@Override
	public double[] getProbabilities(final double[] returns) {
		double value = 1.0;
		if ((returns != null) && (returns.length > 0)) {
			value = 1.0 / returns.length;
		}

		return Utils.newDuplicateArray(value, returns.length);
	}

	// @Override
	// public double[] getProbabilities(double[] returns, Set<Integer> actions) {
	// double value = 1.0;
	// if (actions != null && actions.size() > 0) {
	// value = 1.0 / actions.size();
	// }
	//
	// return Utils.newDuplicateArray(value, actions.size());
	// }
}