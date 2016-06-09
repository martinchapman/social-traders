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

/**
 * <p>
 * A modification of RothErev to address parameter degeneracy, and modified
 * learning with 0-reward. These modifications are made in the context of using
 * the RE algorithm for trader agents in a double auction. See:
 * </p>
 * <p>
 *"Market Power and Efficiency in a Computational Electricity Market with
 * Discriminatory Double-Auction Pricing" Nicolaisen, Petrov & Tesfatsion<br>
 * in IEEE Transactions on Evolutionary Computation Vol. 5, No. 5, p 504.
 * </p>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.10 $
 */

public class NPTRothErevLearner extends RothErevLearner {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NPTRothErevLearner(final int k, final double r, final double e,
			final double s1) {
		super(k, r, e, s1);
	}

	public NPTRothErevLearner() {
		super();
	}

	/**
	 * The modified update function.
	 */
	@Override
	public double experience(final int i, final int action, final double reward) {
		if (i == action) {
			return reward * (1 - experimentation);
		} else {
			return propensities[i] * (experimentation / (k - 1));
		}
	}

}