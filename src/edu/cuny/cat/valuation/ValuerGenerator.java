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

package edu.cuny.cat.valuation;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Resetable;

/**
 * <p>
 * A valuer generator can create instances of {@link ValuationPolicy} with each
 * for a trader. A certain valuer generator is useful to represent a group of
 * valuers that have some common features, for example all from an independent
 * identical distribution, or collectively forming a schedule in a certain
 * shape.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public interface ValuerGenerator extends Parameterizable, AuctionEventListener,
		Resetable {

	/**
	 * creates a new valuer.
	 */
	public ValuationPolicy createValuer();

	/**
	 * Recalculate valuation(s) in response to an auction event.
	 */
	public void eventOccurred(AuctionEvent event);

}