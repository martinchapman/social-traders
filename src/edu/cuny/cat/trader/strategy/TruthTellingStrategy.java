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

package edu.cuny.cat.trader.strategy;

import java.io.Serializable;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.trader.AbstractTradingAgent;

/**
 * A trading strategy that always bids at its private value.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.8 $
 */

public class TruthTellingStrategy extends FixedQuantityStrategyImpl implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TruthTellingStrategy(final AbstractTradingAgent agent) {
		super(agent);
	}

	public TruthTellingStrategy() {
		super();
	}

	@Override
	public boolean modifyShout(final Shout.MutableShout shout) {
		shout.setPrice(agent.getPrivateValue());
		return super.modifyShout(shout);
	}
}