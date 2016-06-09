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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.server.GameController;
import edu.cuny.util.Utils;

/**
 * A report tracking the cumulative profits of specialists.
 * 
 * <p>
 * <b>Report variables</b>
 * </p>
 * <table cellpadding="5">
 * <tr>
 * <td> <code>&lt;specialist&gt;.profit</code></td>
 * <td>the daily profit made by each specialist.</td>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.21 $
 */

public class ProfitReport implements GameReport {

	static Logger logger = Logger.getLogger(ProfitReport.class);

	/* cumulative profits of specialists so far in the game(current iteration) */
	Map<String, Double> preCurDayProfits;

	public ProfitReport() {
		preCurDayProfits = Collections
				.synchronizedMap(new HashMap<String, Double>());
	}

	public Map<ReportVariable, ?> getVariables() {
		return null;
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof GameStartingEvent) {
			initCumulativeProfitRecords();
		} else if (event instanceof DayClosedEvent) {
			Double profit = null;
			final ReportVariableBoard board = ReportVariableBoard.getInstance();
			final Specialist specialists[] = GameController.getInstance()
					.getRegistry().getSpecialists();
			for (final Specialist specialist2 : specialists) {
				profit = preCurDayProfits.get(specialist2.getId());
				board.reportValue(specialist2.getId() + ReportVariable.SEPARATOR
						+ GameReport.PROFIT, specialist2.getAccount().getBalance()
						- profit.doubleValue());
				preCurDayProfits.put(specialist2.getId(), new Double(specialist2
						.getAccount().getBalance()));
			}
		}
	}

	protected void initCumulativeProfitRecords() {
		preCurDayProfits.clear();

		final String specialistIds[] = GameController.getInstance().getRegistry()
				.getSpecialistIds();
		for (final String specialistId : specialistIds) {
			preCurDayProfits.put(specialistId, new Double(0));
		}
	}

	public void produceUserOutput() {
		final Specialist specialists[] = GameController.getInstance().getRegistry()
				.getSpecialists();
		final TreeSet<Specialist> specialistSet = new TreeSet<Specialist>(
				new ProfitComparator());

		for (final Specialist specialist2 : specialists) {
			specialistSet.add(specialist2);
		}

		ProfitReport.logger.info("\n" + toString() + "\n");

		int ranking = 1;
		for (final Specialist specialist : specialistSet) {
			ProfitReport.logger.info(ranking
					+ ".\t"
					+ Utils.align(GameReport.Formatter.format(specialist.getAccount()
							.getBalance())) + "\t" + specialist.getId());
			ranking++;
		}
		ProfitReport.logger.info("\n");

		specialistSet.clear();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	class ProfitComparator implements Comparator<Specialist> {

		public int compare(final Specialist s0, final Specialist s1) {

			if (s0.getAccount().getBalance() > s1.getAccount().getBalance()) {
				return -1;
			} else if (s0.getAccount().getBalance() < s1.getAccount().getBalance()) {
				return 1;
			} else {
				return s0.getId().compareTo(s1.getId());
			}
		}
	}
}
