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

import cern.jet.random.AbstractDistribution;

/**
 * A framework of valuation policy in which a valuation is drawn from a
 * distribution.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.12 $
 */

public class AbstractRandomValuer extends ValuationPolicy {

	/**
	 * The probability distribution to use for drawing valuations.
	 */
	protected AbstractDistribution distribution;

	public void setDistribution(final AbstractDistribution distribution) {
		this.distribution = distribution;
	}

	public AbstractDistribution getDistribution() {
		return distribution;
	}

	public void drawRandomValue() {
		setValue(distribution.nextDouble());
	}
}
