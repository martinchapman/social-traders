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

package edu.cuny.cat.market.pricing;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.util.MathUtil;

/**
 * <p>
 * A {@link KPricingPolicy} that adjusts the value of <code>k</code> such that
 * the type of shouts that is outnumbered by the other type is favored. This
 * aims to balance the scheduels of demand and supply so as to avoid low
 * transaction success rate. The more asks than bids, the closer <code>k</code>
 * to 0, or otherwise the closer to 1.
 * </p>
 * 
 * <p>
 * TODO: An exponential function is currently used to generate <code>k</code>.
 * The function however is not symmetric for demand and supply. A new function
 * should be used instead.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class ScheduleBalancingPricingPolicy extends DiscriminatoryPricingPolicy {

	protected int numOfAsks;

	protected int numOfBids;

	static Logger logger = Logger.getLogger(ScheduleBalancingPricingPolicy.class);

	public ScheduleBalancingPricingPolicy() {
	}

	public ScheduleBalancingPricingPolicy(final double k) {
		super(k);
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof DayOpeningEvent) {
			// initialize with 1 so as to avoid divided-by-0 error
			numOfAsks = 1;
			numOfBids = 1;
		} else if (event instanceof ShoutPlacedEvent) {
			// TODO: shout number does not equal demand/supply, need to filter out
			// modifying shouts

			final Shout shout = ((ShoutPlacedEvent) event).getShout();
			if ((shout.getSpecialist() != null)
					&& shout.getSpecialist().getId().equalsIgnoreCase(
							getAuctioneer().getName())) {
				if (shout.isAsk()) {
					numOfAsks++;
				} else {
					numOfBids++;
				}

				// double coef = 1.0;
				// if (numOfAsks > numOfBids) {
				// coef = 4;
				// } else if (numOfAsks < numOfBids) {
				// coef = 0.25;
				// }

				// TODO: 2 here is a parameter, should be parameterized.
				k = Math.pow(2, -(numOfAsks / numOfBids));

				k = MathUtil.round(k, 2);

				ScheduleBalancingPricingPolicy.logger.debug("k: " + k);
			}
		}
	}

}