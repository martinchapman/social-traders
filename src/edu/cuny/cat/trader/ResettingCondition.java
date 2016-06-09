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

package edu.cuny.cat.trader;

import java.util.Observable;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Resetable;

/**
 * specifies in which condition a trader should be reset to simulate fresh air
 * in market.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public abstract class ResettingCondition extends Observable implements
		AuctionEventListener, Parameterizable, Prototypeable, Cloneable, Resetable {

	static Logger logger = Logger.getLogger(ResettingCondition.class);

	protected AbstractTradingAgent agent;

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		// do nothing
	}

	public void eventOccurred(final AuctionEvent event) {
		// do nothing
	}

	public void initialize() {
		// do nothing
	}

	public void reset() {
		// do nothing
	}

	public AbstractTradingAgent getAgent() {
		return agent;
	}

	public void setAgent(final AbstractTradingAgent agent) {
		this.agent = agent;
	}

	public Object protoClone() {
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}