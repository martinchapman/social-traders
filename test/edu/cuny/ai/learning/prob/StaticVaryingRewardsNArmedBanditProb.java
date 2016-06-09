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

/**
 * implements an n-armed problem with which arms return constant but varying
 * reward.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

public class StaticVaryingRewardsNArmedBanditProb extends NArmedBanditProb {

	static Logger logger = Logger
			.getLogger(StaticVaryingRewardsNArmedBanditProb.class);

	@Override
	public double pull(final int arm) {
		return getExpectedReturn(arm);
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