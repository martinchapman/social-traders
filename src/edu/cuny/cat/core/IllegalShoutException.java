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

package edu.cuny.cat.core;

/**
 * This exception is thrown by auctioneers when a shout placed in an auction is
 * illegal under the rules of the auction, or by the game server when it finds a
 * shout request is invalid, e.g. a bid price higher than the private value.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.7 $
 */
public class IllegalShoutException extends AuctionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IllegalShoutException(final String message) {
		super(message);
	}

	public IllegalShoutException() {
		super();
	}

}