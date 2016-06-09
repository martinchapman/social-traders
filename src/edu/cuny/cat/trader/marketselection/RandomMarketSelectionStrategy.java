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

package edu.cuny.cat.trader.marketselection;

import org.apache.log4j.Logger;

import cern.jet.random.Uniform;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;

/**
 * A market selection strategy that randomly chooses a specialist from the
 * active ones in the game every day without considering which one is better.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 * 
 */

public class RandomMarketSelectionStrategy extends
		AbstractMarketSelectionStrategy {

	static Logger logger = Logger.getLogger(RandomMarketSelectionStrategy.class);

	public Uniform distribution;

	public RandomMarketSelectionStrategy() {
		distribution = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	/**
	 * selects an active specialist randomly based on a uniform distribution from
	 * the given list.
	 */
	@Override
	public void selectMarket() {
		final Integer indices[] = activeMarkets.toArray(new Integer[0]);

		currentMarketIndex = indices[distribution.nextIntFromTo(0,
				indices.length - 1)].intValue();

	}
}
