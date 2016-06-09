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

package edu.cuny.cat.market.accepting;

import edu.cuny.cat.core.IllegalShoutException;

/**
 * This exception is thrown by an auctioneer implementing the NYSE rule if a
 * trader agent attempts to place a shout that is not an improvement over the
 * current best bid.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.9 $
 */
public class NotAnImprovementOverQuoteException extends IllegalShoutException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotAnImprovementOverQuoteException(final String message) {
		super(message);
	}

	public NotAnImprovementOverQuoteException() {
		super();
	}

}
