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

package edu.cuny.prng;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

/**
 * <p>
 * Defines a factory that produces random number generators, i.e.
 * <code>RandomEngine</code>s, based on the 64-bit Mersenne Twister algorithm
 * (Matsumoto and Nishimura).
 * </p>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.11 $
 */
public class MT implements PRNGFactory {

	public RandomEngine create() {
		return new MersenneTwister64();
	}

	public RandomEngine create(final long seed) {
		return new MersenneTwister64((int) seed);
	}

	public String getDescription() {
		return "64-bit Mersenne Twister (Matsumoto and Nishimura)";
	}

}
