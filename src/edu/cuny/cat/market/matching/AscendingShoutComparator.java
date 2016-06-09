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

package edu.cuny.cat.market.matching;

import java.io.Serializable;
import java.util.Comparator;

import edu.cuny.cat.core.Shout;

/**
 * This class can be used to as a Comparator to rank shouts in ascending order.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.9 $
 * 
 */

public class AscendingShoutComparator implements Comparator<Shout>,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AscendingShoutComparator() {
	}

	public int compare(final Shout shout1, final Shout shout2) {
		return shout1.compareTo(shout2);
	}
}