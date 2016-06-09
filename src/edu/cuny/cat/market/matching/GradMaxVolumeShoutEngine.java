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

package edu.cuny.cat.market.matching;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.market.DuplicateShoutException;
import edu.cuny.util.SortedTreeList;

/**
 * <p>
 * TODO: has yet to be done !
 * </p>
 * 
 * <p>
 * This class provides a generic auction shout management framework. It differs
 * from {@link FourHeapShoutEngine} in the sense that it out-sources the
 * matching policy and the priority of shouts.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.10 $
 */

public class GradMaxVolumeShoutEngine extends FourHeapShoutEngine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * list of sums of demand and supply at shouted prices sorted by quantities
	 */
	// protected PriorityBuffer demandSupplies = new PriorityBuffer();
	protected SortedTreeList<Shout> shouts = new SortedTreeList<Shout>("shouts",
			ShoutEngine.AscendingOrder);

	protected SortedTreeList<Shout> asks = new SortedTreeList<Shout>("asks",
			ShoutEngine.AscendingOrder);

	protected SortedTreeList<Shout> bids = new SortedTreeList<Shout>("bids",
			ShoutEngine.DescendingOrder);

	// protected Map shout2DemandSupply = new HashMap();

	static Logger logger = Logger.getLogger(GradMaxVolumeShoutEngine.class);

	public GradMaxVolumeShoutEngine() {
	}

	@Override
	public void reset() {
		super.reset();
		shouts.clear();
	}

	/**
	 * TODO: not finished yet !
	 */
	@Override
	@SuppressWarnings("unused")
	protected void newBid(final Shout bid) throws DuplicateShoutException {

		final int bidIndex = bids.indexOfIfAdded(bid);
		final int askIndex = asks.indexOfIfAdded(bid);

		// get bh and al

		Shout bh = null;
		if (bids.isEmpty()) {
			bh = new Shout(0, Double.MIN_VALUE, true); // default highest bid with 0
			// quantity
		} else {
			bh = bids.get(0);
		}

		Shout al = null;
		if (asks.isEmpty()) {
			al = new Shout(0, Double.MAX_VALUE, false); // default lowest ask with 0
			// quantity
		} else {
			al = asks.get(0);
		}

		if (askIndex == 0) {
			// lower than the lowest ask

			// if highest among those lower than the lowest ask
			if (bidIndex == 0) {

			} else if (bidIndex > 0) {
				final Shout higherBid = bids.get(bidIndex - 1);
			}

		} else if (askIndex > 0) {
			// no lower than the lowest ask
		} else {
			GradMaxVolumeShoutEngine.logger
					.fatal("Unexpected index in asks for a new shout !");
		}

		if (!asks.isEmpty() && ((asks.get(0)).getPrice() <= bid.getPrice())) {

			if (askIndex <= 0) {
				GradMaxVolumeShoutEngine.logger.fatal("Unexpected index in asks !");
			}

			if (bidIndex <= 0) {

			}
			// bids[bidindex - 1] and asks[askIndex - 1] are Tl and Th.
			final Shout tl = asks.get(askIndex - 1);
			final Shout th = bids.get(bidIndex - 1);

		} else {
			bids.add(bid);
			shouts.add(bid);
			bOut.add(bid);
		}
	}

	@Override
	protected void newAsk(final Shout ask) throws DuplicateShoutException {
		if (!bids.isEmpty() && (bids.get(0).getPrice() >= ask.getPrice())) {

		} else {
			asks.add(ask);
			shouts.add(ask);
			sOut.add(ask);
		}

	}

	class SumOfDS {
		Shout shout;

		int quantity;
	}
}
