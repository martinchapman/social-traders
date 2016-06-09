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

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.RoundOpenedEvent;

/**
 * An adaptive version of {@link ThetaShoutEngine} in which <code>theta</code>
 * starts at 0 and increases in the later rounds of a day so as to increase
 * transaction volume and avoid making intra-marginal traders extremely unhappy
 * at the same time.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class AdaptiveThetaShoutEngine extends ThetaShoutEngine {

	static Logger logger = Logger.getLogger(AdaptiveThetaShoutEngine.class);

	public final static int START_DAY = 120;

	protected int dayLen = 10;

	protected int startRound = 5;

	@Override
	public void eventOccurred(AuctionEvent event) {
		if (event instanceof GameStartingEvent) {
			dayLen = ((GameStartingEvent) event).getDayLen();
		} else if (event instanceof DayOpeningEvent) {
			theta = 0;
			if (event.getDay() < AdaptiveThetaShoutEngine.START_DAY) {
				startRound = dayLen - 2;
			} else {
				startRound = Math.min(2 * auctioneer.getRegistry()
						.getTraderEntitlement(), dayLen - 5);
			}
		} else if (event instanceof RoundOpenedEvent) {
			if (event.getRound() < startRound) {
				theta = 0;
			} else {
				theta = 0.3;
			}
		}
	}
}