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

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.util.Resetable;

/**
 * A commodity valuation policy for determining private values for traders.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.10 $
 */

public abstract class ValuationPolicy implements AuctionEventListener,
		Resetable {

	/**
	 * The current valuation.
	 */
	protected double value;

	/**
	 * Determine the current valuation of commodity in the given auction.
	 */
	public double getValue() {
		return value;
	}

	/**
	 * sets the private value.
	 * 
	 * @param value
	 */
	public void setValue(final double value) {
		this.value = value;
	}

	/**
	 * resets the valuation policy so that the private value can be generated if
	 * applicable.
	 */
	public void reset() {
		// Do nothing
	}

	/**
	 * Recalculate valuation(s) in response to an auction event.
	 */

	public void eventOccurred(final AuctionEvent event) {
		// Do nothing
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " value:" + value;
	}

}