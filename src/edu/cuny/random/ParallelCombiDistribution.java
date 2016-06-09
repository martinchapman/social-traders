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
 * JAF - Java Application Framework
 * Copyright (C) 1999-2006 Jinzhong Niu
 */

package edu.cuny.random;

import cern.jet.random.Uniform;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;

/**
 * <p>
 * A distribution whose probability function is the average of those of several
 * sub-distributions. To generate random numbers, the sub-distributions are
 * chosen randomly with equal probability.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class ParallelCombiDistribution extends CombiDistribution {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Uniform distribution;

	public ParallelCombiDistribution() {
		distribution = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	@Override
	public double nextDouble() {
		final int index = distribution.nextIntFromTo(0, distributions.size() - 1);
		return getDistribution(index).nextDouble();
	}

}