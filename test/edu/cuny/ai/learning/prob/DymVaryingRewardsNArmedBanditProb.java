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

package edu.cuny.ai.learning.prob;

import org.apache.log4j.Logger;

import edu.cuny.prng.GlobalPRNG;
import edu.cuny.random.Normal;
import edu.cuny.util.Galaxy;

/**
 * implements an n-armed problem with which rewards of different arms are
 * following different normal distribution.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class DymVaryingRewardsNArmedBanditProb extends NArmedBanditProb {

	static Logger logger = Logger
			.getLogger(DymVaryingRewardsNArmedBanditProb.class);

	static double stdev = 0.5;

	protected Normal distributions[];

	public DymVaryingRewardsNArmedBanditProb() {
		distributions = new Normal[numOfArms];
		for (int i = 0; i < distributions.length; i++) {
			distributions[i] = new Normal(i + 1,
					DymVaryingRewardsNArmedBanditProb.stdev, Galaxy.getInstance()
							.getDefaultTyped(GlobalPRNG.class).getEngine());
		}
	}

	@Override
	public double pull(final int arm) {
		if ((arm >= 0) && (arm < numOfArms)) {
			return distributions[arm].nextDouble();
		} else {
			return Double.NaN;
		}
	}

	@Override
	public int getBestArm() {
		return numOfArms - 1;
	}

	@Override
	public double getExpectedReturn(final int arm) {
		if ((arm >= 0) && (arm < numOfArms)) {
			return (double) arm + 1;
		} else {
			return Double.NaN;
		}
	}
}