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
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.market.AuctioneerPolicy;

/**
 * A shout-accepting policy determines whether a shout should be accepted or
 * not.
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>accepting</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.16 $
 */

public abstract class ShoutAcceptingPolicy extends AuctioneerPolicy {

	public static final String P_DEF_BASE = "accepting";

	/**
	 * checks whether <code>newShout</code> is acceptable or not to replace
	 * <code>oldShout</code>. If not, an IllegalShoutException is thrown.
	 * 
	 * @param oldShout
	 *          the shout to be replaced; if null, no shout is replaced.
	 * @param newShout
	 *          the new shout to replace the old one
	 * @throws IllegalShoutException
	 */
	public abstract void check(Shout oldShout, Shout newShout)
			throws IllegalShoutException;
}