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
 * A compound accepting policy that will only accept a shout if and only if all
 * its sub accepting policies accept the shout.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class TighterCombiAcceptingPolicy extends CombiAcceptingPolicy {

	@Override
	public void check(final Shout oldShout, final Shout newShout)
			throws IllegalShoutException {
		for (final ShoutAcceptingPolicy policy : policies) {
			policy.check(oldShout, newShout);
		}
	}
}
