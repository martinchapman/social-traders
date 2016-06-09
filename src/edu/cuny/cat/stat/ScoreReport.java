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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.cat.event.DayStatPassEvent;
import edu.cuny.cat.event.GameOverEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.SimulationStartedEvent;
import edu.cuny.cat.server.GameController;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * A report on scores of specialists with a {@link ScoreDaysCondition}
 * specifying scoring game days.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.condition</tt><br>
 * <font size=-1>name of class, implementing {@link ScoreDaysCondition}</font></td>
 * <td valign=top>(determines which days are scoring ones)</td>
 * 
 * </table>
 * 
 * <p>
 * <b>Report variables</b>
 * </p>
 * <table cellpadding="5">
 * <tr>
 * <td> <code>score.counted</code></td>
 * <td>whether the current day is a scoring day or not.</td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.19 $
 */

public class ScoreReport extends DailyAssessmentReport implements
		Parameterizable {

	static Logger logger = Logger.getLogger(ScoreReport.class);

	static final String P_CONDITION = "condition";

	static final String COUNTED = "counted";

	protected ScoreDaysCondition scoreDaysCondition;

	/* cumulative scores of specialists so far (over games) */
	protected Map<String, Score> alltimeScores;

	/* scoring days in the current game */
	protected Set<Integer> scoreDaysCurGame;

	protected int totalDays;

	public ScoreReport() {
		alltimeScores = new HashMap<String, Score>();
		scoreDaysCurGame = new TreeSet<Integer>();
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		scoreDaysCondition = parameters.getInstanceForParameter(base
				.push(ScoreReport.P_CONDITION), null, ScoreDaysCondition.class);

		if (scoreDaysCondition instanceof Parameterizable) {
			((Parameterizable) scoreDaysCondition).setup(parameters, base
					.push(ScoreReport.P_CONDITION));
		}
	}

	public ScoreDaysCondition getScoreDaysCondition() {
		return scoreDaysCondition;
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (scoreDaysCondition instanceof AuctionEventListener) {
			((AuctionEventListener) scoreDaysCondition).eventOccurred(event);
		}

		if (event instanceof SimulationStartedEvent) {
			initScoreRecords();
		} else if (event instanceof DayStatPassEvent) {
			switch (((DayStatPassEvent) event).getPass()) {
			case DayStatPassEvent.FIRST_PASS:
				updateScoreDaysConditionVar(event.getDay());
				break;

			case DayStatPassEvent.THIRD_PASS:
				updateScores(event.getDay());
				break;
			}
		} else if (event instanceof GameStartedEvent) {
			scoreDaysCurGame.clear();
		} else if (event instanceof GameOverEvent) {
			totalDays += scoreDaysCurGame.size();
			ScoreReport.logger.info("* " + scoreDaysCurGame.size()
					+ " scoring day(s): " + scoreDaysCurGame);
		}
	}

	protected void initScoreRecords() {
		totalDays = 0;

		final String specialistIds[] = GameController.getInstance().getRegistry()
				.getSpecialistIds();
		Score score;
		for (final String specialistId : specialistIds) {
			score = new Score();
			score.total = 0;
			score.marketShare = 0;
			score.profitShare = 0;
			score.transactionRate = 0;
			alltimeScores.put(specialistId, score);
		}
	}

	protected void updateScoreDaysConditionVar(final int day) {
		final ReportVariableBoard board = ReportVariableBoard.getInstance();
		board.reportValue(GameReport.SCORE + ReportVariable.SEPARATOR
				+ ScoreReport.COUNTED, new Boolean(scoreDaysCondition.count(day)));
	}

	/**
	 * updates the scores of specialists if the specified day is a scoring day.
	 * 
	 * @param day
	 *          the day to be considered
	 */
	protected void updateScores(final int day) {

		if (scoreDaysCondition.count(day)) {
			scoreDaysCurGame.add(new Integer(day));

			for (final String specialistId : dailyScores.keySet()) {
				final Score dailyScore = dailyScores.get(specialistId);
				final Score alltimeScore = alltimeScores.get(specialistId);

				alltimeScore.total += dailyScore.total;
				alltimeScore.profitShare += dailyScore.profitShare;
				alltimeScore.marketShare += dailyScore.marketShare;
				alltimeScore.transactionRate += dailyScore.transactionRate;
			}
		}
	}

	@Override
	public void produceUserOutput() {
		final TreeSet<String> specialistSet = new TreeSet<String>(
				new ScoreComparator());

		for (final String specialistId : dailyScores.keySet()) {
			specialistSet.add(specialistId);
		}

		ScoreReport.logger.info("\n" + getClass().getSimpleName() + "\n");

		if (totalDays > 0) {
			int ranking = 1;
			for (final String specialistId : specialistSet) {
				ScoreReport.logger.info(ranking
						+ ".\t"
						+ Utils.align(GameReport.Formatter.format((alltimeScores
								.get(specialistId)).total)) + "\t" + specialistId);
				ranking++;
			}
			ScoreReport.logger.info("\n");
		}
	}

	public Score getScore(final String specialistId) {
		return alltimeScores.get(specialistId);
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();
		s += "\n" + Utils.indent(scoreDaysCondition.toString());
		return s;
	}

	class ScoreComparator implements Comparator<String> {

		public int compare(final String specialistId0, final String specialistId1) {
			final Score s0 = alltimeScores.get(specialistId0);
			final Score s1 = alltimeScores.get(specialistId1);

			if (s0.total > s1.total) {
				return -1;
			} else if (s0.total < s1.total) {
				return 1;
			} else {
				return specialistId0.compareTo(specialistId1);
			}
		}
	}
}
