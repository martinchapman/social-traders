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

package edu.cuny.cat.comm;

/**
 * <p>
 * This exception occurs when the message under processing is erroneous.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class CatpMessageErrorException extends CatpMessageException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CatpMessageErrorException(final String s) {
		super(s);
	}

	public CatpMessageErrorException() {
	}

}
