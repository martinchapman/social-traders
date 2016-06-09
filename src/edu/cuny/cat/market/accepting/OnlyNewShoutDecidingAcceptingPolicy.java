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
 * The class of accepting policy that makes decision based on the new shout
 * only, having nothing to do with the old (standing) shout.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public abstract class OnlyNewShoutDecidingAcceptingPolicy extends
		ShoutAcceptingPolicy {

	/**
	 * checks whether a new shout is acceptable or not.
	 * 
	 * @param shout
	 *          the shout being checked.
	 * @throws IllegalShoutException
	 */
	public abstract void check(Shout shout) throws IllegalShoutException;

	/**
	 * calls {@link #check(Shout)} and checks with <code>newShout</code> only.
	 * 
	 * @see edu.cuny.cat.market.accepting.ShoutAcceptingPolicy#check(edu.cuny.cat.core.Shout,
	 *      edu.cuny.cat.core.Shout)
	 */
	@Override
	public void check(final Shout oldShout, final Shout newShout)
			throws IllegalShoutException {
		check(newShout);
	}
}
