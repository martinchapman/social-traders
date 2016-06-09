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

package edu.cuny.cat.market;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Resetable;

/**
 * A class representing a policy that
 * {@link edu.cuny.cat.market.GenericDoubleAuctioneer} may use on some aspect.
 * 
 * NOTE: An auctioneer policy is resetable and is reset by the auctioneer
 * automatically after a game is over. Any policy that extends this abstract
 * policy framework should listen to game events only for obtaining information
 * and should not use the events for the purpose of resetting after a game.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 * 
 */
public abstract class AuctioneerPolicy implements AuctionEventListener,
		Parameterizable, Resetable {

	protected Auctioneer auctioneer;

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		// do nothing
	}

	/**
	 * initializes after parameters are set via either setters, constructors, or
	 * parameter files.
	 */
	public void initialize() {
		// do nothing
	}

	/**
	 * resets the state to be the same as the policy is created and initialized.
	 */
	public void reset() {
		// do nothing
	}

	public void eventOccurred(final AuctionEvent event) {
		// do nothing
	}

	public Auctioneer getAuctioneer() {
		return auctioneer;
	}

	public void setAuctioneer(final Auctioneer auctioneer) {
		this.auctioneer = auctioneer;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}