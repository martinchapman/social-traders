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

package edu.cuny.cat.core;

/**
 * This exception is thrown by the game server when it finds a transaction
 * request has an invalid transaction price.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */
public class IllegalTransactionPriceException extends
		IllegalTransactionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IllegalTransactionPriceException(final String message) {
		super(message);
	}

	public IllegalTransactionPriceException() {
		super();
	}

}