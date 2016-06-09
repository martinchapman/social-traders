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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.server.GameController;

/**
 * <p>
 * A report tracking how traders are distributed among specialists over days.
 * </p>
 * 
 * <p>
 * <b>Report variables</b>
 * </p>
 * <table cellpadding="5">
 * <tr>
 * <td> <code>&lt;specialist&gt;.trader</code></td>
 * <td>the number of traders registered with each specialist.</td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.20 $
 */

public class TraderDistributionReport implements GameReport {

	static Logger logger = Logger.getLogger(TraderDistributionReport.class);

	protected Map<String, Integer> distributions;

	public TraderDistributionReport() {
		distributions = Collections.synchronizedMap(new HashMap<String, Integer>());
	}

	public Map<ReportVariable, ?> getVariables() {
		return null;
	}

	public void produceUserOutput() {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof DayClosedEvent) {
			distributions.clear();

			final ReportVariableBoard board = ReportVariableBoard.getInstance();
			final Specialist specialists[] = GameController.getInstance()
					.getRegistry().getSpecialists();
			for (final Specialist specialist2 : specialists) {
				board.reportValue(specialist2.getId() + ReportVariable.SEPARATOR
						+ GameReport.TRADER, specialist2.getTraderMap().size());
			}
		}
	}
}
