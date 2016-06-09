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

package edu.cuny.cat.stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.market.DuplicateShoutException;
import edu.cuny.cat.market.matching.FourHeapShoutEngine;
import edu.cuny.cat.server.GameController;
import edu.cuny.util.Resetable;

/**
 * <p>
 * A report that uses {@link edu.cuny.cat.market.matching.FourHeapShoutEngine}
 * to process shouts at specialists to make theoretical analysis later on (in
 * subclasses).
 * </p>
 * 
 * @author Kai Cai
 * @version $Revision: 1.14 $
 */

public abstract class DirectRevelationReport implements GameReport, Resetable {

	/**
	 * The auction state after forced direct revelation.
	 */
	protected FourHeapShoutEngine globalShoutEngine;

	protected Map<String, FourHeapShoutEngine> shoutEngines;

	Trader[] traders;

	/**
	 * The truthful shouts of all traders in the auction.
	 */
	protected ArrayList<Shout> shouts;

	static Logger logger = Logger.getLogger(DirectRevelationReport.class);

	public DirectRevelationReport() {
		shouts = new ArrayList<Shout>();
		shoutEngines = Collections
				.synchronizedMap(new HashMap<String, FourHeapShoutEngine>());
		globalShoutEngine = new FourHeapShoutEngine();
	}

	public void reset() {
		shouts.clear();

		final String specialistIds[] = GameController.getInstance().getRegistry()
				.getSpecialistIds();
		for (final String specialistId : specialistIds) {
			final FourHeapShoutEngine shoutEngine = shoutEngines.get(specialistId);
			if (shoutEngine != null) {
				shoutEngine.reset();
			}
		}

		globalShoutEngine.reset();
	}

	public void calculate() {
		reset();
		simulateDirectRevelation();
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof GameStartingEvent) {
			final String specialistIds[] = GameController.getInstance().getRegistry()
					.getSpecialistIds();
			for (final String specialistId : specialistIds) {
				shoutEngines.put(specialistId, new FourHeapShoutEngine());
			}
		} else if (event instanceof DayOpenedEvent) {
			traders = GameController.getInstance().getRegistry().getTraders();
		}
	}

	/**
	 * Update the auction state with a truthful shout from each trader.
	 */
	protected void simulateDirectRevelation() {
		for (int i = 0; i < traders.length; i++) {
			final int quantity = traders[i].getEntitlement();
			final double value = traders[i].getPrivateValue();
			final boolean isBid = !traders[i].isSeller();
			final String specialistId = traders[i].getSpecialistId();
			final Shout shout = new Shout(quantity, value, isBid);
			shout.setTrader(traders[i]);
			shouts.add(shout);
			final FourHeapShoutEngine shoutEngine = shoutEngines.get(specialistId);

			// TODO: when traders have different entitlement, strange things may
			// happen !

			try {
				if (shoutEngine != null) {
					shoutEngine.newShout(shout);
					globalShoutEngine.newShout(shout);
				}
			} catch (final DuplicateShoutException e) {
				DirectRevelationReport.logger.error(e.getMessage());
				throw new Error(e);
			}
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
