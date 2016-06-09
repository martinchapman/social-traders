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

package edu.cuny.cat.valuation;

import org.apache.log4j.Logger;

import cern.jet.random.Uniform;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.SimulationStartedEvent;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;

/**
 * <p>
 * A valuation policy in which valuations are drawn from a uniform distribution
 * which remains the same during a game but may change game from game.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.11 $
 */

public class DistinctDistributionValuer extends AbstractRandomValuer {

	/**
	 * The generator instance that created this valuer
	 */
	protected DistinctDistributionValuerGenerator generator;

	/**
	 * The distribution to draw the lower bound of the range for the uniform
	 * distribution.
	 */
	protected Uniform minValueDist;

	/**
	 * The distribution to draw the length of the range for the uniform
	 * distribution.
	 */
	protected Uniform rangeDist;

	static Logger logger = Logger.getLogger(RandomValuer.class);

	public void setGenerator(final DistinctDistributionValuerGenerator generator) {
		this.generator = generator;
	}

	public DistinctDistributionValuerGenerator getGenerator() {
		return generator;
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof SimulationStartedEvent) {
			minValueDist = new Uniform(generator.getMinValueMin(), generator
					.getMinValueMax(), Galaxy.getInstance().getDefaultTyped(
					GlobalPRNG.class).getEngine());
			rangeDist = new Uniform(generator.getRangeMin(), generator.getRangeMax(),
					Galaxy.getInstance().getDefaultTyped(GlobalPRNG.class).getEngine());
		} else if (event instanceof GameStartingEvent) {

			final double minValue = minValueDist.nextDouble();
			final double maxValue = minValue + rangeDist.nextDouble();

			setDistribution(new Uniform(minValue, maxValue, Galaxy.getInstance()
					.getDefaultTyped(GlobalPRNG.class).getEngine()));
			drawRandomValue();
		}
	}
}
