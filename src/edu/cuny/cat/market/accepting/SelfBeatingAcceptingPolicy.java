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
 * An accepting policy with which a new shout must beat its trader's standing
 * shout to be acceptable.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class SelfBeatingAcceptingPolicy extends ShoutAcceptingPolicy {

	/**
	 * implements the rule that a new shout must improve over its old peer.
	 * 
	 * @see edu.cuny.cat.market.accepting.ShoutAcceptingPolicy#check(edu.cuny.cat.core.Shout,
	 *      edu.cuny.cat.core.Shout)
	 */
	@Override
	public void check(final Shout oldShout, final Shout newShout)
			throws IllegalShoutException {
		if (newShout.isAsk()) {
			if ((oldShout != null) && (oldShout.getPrice() <= newShout.getPrice())) {
				throw new NotAnImprovementOverSelfException("ask");
			}
		} else {
			if ((oldShout != null) && (oldShout.getPrice() >= newShout.getPrice())) {
				throw new NotAnImprovementOverSelfException("bid");
			}
		}

	}
}
