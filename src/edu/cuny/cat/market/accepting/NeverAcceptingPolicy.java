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

package edu.cuny.cat.market.accepting;

import edu.cuny.cat.core.IllegalShoutException;
import edu.cuny.cat.core.Shout;

/**
 * the tightest accepting policy under which all shouts are rejected.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class NeverAcceptingPolicy extends OnlyNewShoutDecidingAcceptingPolicy {

	/**
	 * rejects all shouts and {@link IllegalShoutException} is always thrown.
	 * 
	 * @see edu.cuny.cat.market.accepting.OnlyNewShoutDecidingAcceptingPolicy#check(edu.cuny.cat.core.Shout)
	 */
	@Override
	public void check(final Shout shout) throws IllegalShoutException {
		throw new IllegalShoutException();
	}
}
