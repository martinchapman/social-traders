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

package edu.cuny.cat.valuation;

import org.apache.log4j.Logger;

/**
 * <p>
 * This valuer generator creates valutions drawn from distributions similar to
 * the situation in {@link RandomValuerGenerator}, but the valuations are
 * redrawn at the end of each game (iteration).
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class GamelyRandomValuerGenerator extends RandomValuerGenerator {

	static Logger logger = Logger.getLogger(GamelyRandomValuerGenerator.class);

	public GamelyRandomValuerGenerator() {
	}

	public GamelyRandomValuerGenerator(final double minValue,
			final double maxValue) {
		super(minValue, maxValue);
	}

	@Override
	public synchronized ValuationPolicy createValuer() {
		final RandomValuer valuer = new GamelyRandomValuer();
		valuer.setGenerator(this);
		valuer.setDistribution(createDistribution());
		valuer.drawRandomValue();
		return valuer;
	}
}
