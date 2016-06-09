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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.DayStatPassEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.server.GameController;
import edu.cuny.util.Utils;

/**
 * <p>
 * A report executing daily assessment on specialists according to the TAC
 * market design tournament assessment process document.
 * </p>
 * 
 * <p>
 * <b>Report variables</b>
 * </p>
 * <table cellpadding="5">
 * <tr>
 * <td> <code>&lt;specialist&gt;.score</code></td>
 * <td>the daily score of a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.score.marketshare</code></td>
 * <td>the daily market share score of a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.score.profit</code></td>
 * <td>the daily profit score of a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.score.transactionrate</code></td>
 * <td>the daily transaction success rate score of a specialist.</td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.19 $
 */

public class DailyAssessmentReport implements GameReport {

	static Logger logger = Logger.getLogger(DailyAssessmentReport.class);

	protected Map<String, Double> preCurDayProfits;

	protected Map<String, Score> dailyScores;

	public DailyAssessmentReport() {
		dailyScores = new HashMap<String, Score>();
		preCurDayProfits = Collections
				.synchronizedMap(new HashMap<String, Double>());
	}

	public Map<ReportVariable, ?> getVariables() {
		return null;
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof GameStartingEvent) {
			initCumulativeProfitRecords();
		} else if (event instanceof DayOpeningEvent) {
			resetDailyScores();
		} else if (event instanceof DayClosedEvent) {
			initDailyScores();
		} else if (event instanceof DayStatPassEvent) {
			switch (((DayStatPassEvent) event).getPass()) {
			case DayStatPassEvent.FIRST_PASS:
				calculateMarketShares();
				calculateProfits();
				calculateTransactionRates();
				calculateDailyScores();
				break;
			case DayStatPassEvent.SECOND_PASS:
				reportDailyScores();
				break;
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

	protected void resetDailyScores() {
		dailyScores.clear();
	}

	protected void initDailyScores() {
		final Specialist specialists[] = GameController.getInstance().getRegistry()
				.getSpecialists();

		for (final Specialist specialist2 : specialists) {
			dailyScores.put(specialist2.getId(), new Score());
		}
	}

	protected void calculateMarketShares() {
		final Specialist specialists[] = GameController.getInstance().getRegistry()
				.getSpecialists();
		final int shares[] = new int[specialists.length];
		int total = 0;
		for (int i = 0; i < specialists.length; i++) {
			shares[i] = specialists[i].getTraderMap().size();
			total += shares[i];
		}

		if (total == 0) {
			DailyAssessmentReport.logger
					.debug("No trader registered with any specialist !");
		}

		for (int i = 0; i < specialists.length; i++) {
			final Score score = dailyScores.get(specialists[i].getId());
			if (score != null) {
				if (total == 0) {
					score.marketShare = 0;
				} else {
					score.marketShare = (double) shares[i] / total;
				}
			} else {
				DailyAssessmentReport.logger.error("Score record on market share for "
						+ specialists[i].getId() + " not available !");
			}
		}
	}

	protected void calculateProfits() {
		Double profit = null;

		final Specialist specialists[] = GameController.getInstance().getRegistry()
				.getSpecialists();
		double total = 0;
		final double dailyProfits[] = new double[specialists.length];
		for (int i = 0; i < specialists.length; i++) {
			profit = preCurDayProfits.get(specialists[i].getId());
			dailyProfits[i] = specialists[i].getAccount().getBalance()
					- profit.doubleValue();
			total += dailyProfits[i];
			preCurDayProfits.put(specialists[i].getId(), new Double(specialists[i]
					.getAccount().getBalance()));
		}

		if (total == 0) {
			DailyAssessmentReport.logger
					.debug("No profit made by any specialist today !");
		}

		for (int i = 0; i < specialists.length; i++) {
			final Score score = dailyScores.get(specialists[i].getId());
			if (score != null) {
				if (total == 0) {
					score.profitShare = 0;
				} else {
					score.profitShare = dailyProfits[i] / total;
				}
			} else {
				DailyAssessmentReport.logger.error("Score record on profit for "
						+ specialists[i].getId() + " not available !");
			}
		}
	}

	protected void calculateTransactionRates() {
		final Specialist specialists[] = GameController.getInstance().getRegistry()
				.getSpecialists();

		final Map<String, Integer> specialistIndices = new HashMap<String, Integer>();
		for (int i = 0; i < specialists.length; i++) {
			specialistIndices.put(specialists[i].getId(), new Integer(i));
		}

		final int shoutPlacedNums[] = new int[specialists.length];
		final int shoutAcceptedNums[] = new int[specialists.length];

		final Shout shouts[] = GameController.getInstance().getRegistry()
				.getShouts();
		int index;
		for (final Shout shout2 : shouts) {
			if ((shout2.getSpecialist() != null)
					&& specialistIndices.containsKey(shout2.getSpecialist().getId())) {
				index = specialistIndices.get(shout2.getSpecialist().getId())
						.intValue();
			} else {
				DailyAssessmentReport.logger
						.debug("Shout placed at a failed specialist !");
				continue;
			}

			if ((shout2.getState() == Shout.PLACED)
					|| (shout2.getState() == Shout.MATCHED)) {
				shoutPlacedNums[index]++;
				if (shout2.getState() == Shout.MATCHED) {
					shoutAcceptedNums[index]++;
				}
			}
		}

		double value = Double.NaN;
		for (int i = 0; i < specialists.length; i++) {
			if (shoutAcceptedNums[i] == 0) {
				DailyAssessmentReport.logger.debug("No shout accepted at "
						+ specialists[i].getId() + " !");
				value = 0;
			} else if (shoutPlacedNums[i] == 0) {
				DailyAssessmentReport.logger.debug("No shout placed at "
						+ specialists[i].getId() + " !");
				value = 0;
			} else {
				value = (double) shoutAcceptedNums[i] / shoutPlacedNums[i];
			}

			final Score score = dailyScores.get(specialists[i].getId());
			if (score != null) {
				score.transactionRate = value;
			} else {
				DailyAssessmentReport.logger
						.error("Score record on shout success rate for "
								+ specialists[i].getId() + " not available !");
			}
		}
	}

	protected void calculateDailyScores() {
		final Iterator<Score> iterator = dailyScores.values().iterator();
		Score dailyScore = null;
		while (iterator.hasNext()) {
			dailyScore = iterator.next();
			dailyScore.updateTotal();
		}
	}

	protected void reportDailyScores() {
		final ReportVariableBoard board = ReportVariableBoard.getInstance();

		DailyAssessmentReport.logger.info(getClass().getSimpleName() + "\n");

		Iterator<String> iterator = dailyScores.keySet().iterator();
		final TreeSet<String> specialistIdSet = new TreeSet<String>();

		while (iterator.hasNext()) {
			specialistIdSet.add(iterator.next());
		}

		iterator = specialistIdSet.iterator();
		while (iterator.hasNext()) {

			final String specialistId = iterator.next();
			final Score score = dailyScores.get(specialistId);

			DailyAssessmentReport.logger.info(Utils.indent(score + "  \t"
					+ specialistId));

			board.reportValue(specialistId + ReportVariable.SEPARATOR
					+ GameReport.SCORE, score.total);
			board.reportValue(specialistId + ReportVariable.SEPARATOR
					+ GameReport.SCORE + ReportVariable.SEPARATOR
					+ GameReport.MARKETSHARE, score.marketShare);
			board.reportValue(specialistId + ReportVariable.SEPARATOR
					+ GameReport.SCORE + ReportVariable.SEPARATOR + GameReport.PROFIT,
					score.profitShare);
			board.reportValue(specialistId + ReportVariable.SEPARATOR
					+ GameReport.SCORE + ReportVariable.SEPARATOR
					+ GameReport.TRANSACTIONRATE, score.transactionRate);
		}

		DailyAssessmentReport.logger.info("\n");
	}

	public Score getDailyScore(final String specialistId) {
		return dailyScores.get(specialistId);
	}

	public void produceUserOutput() {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
