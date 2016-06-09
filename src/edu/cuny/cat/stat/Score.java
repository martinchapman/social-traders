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

/**
 * <p>
 * a record of the score of a specialist.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class Score {

	public static final String P_SCORES[] = new String[] { GameReport.TOTAL,
			GameReport.MARKETSHARE, GameReport.PROFIT, GameReport.TRANSACTIONRATE };

	/**
	 * index of total score in {@link #P_SCORES}.
	 */
	public static final int TOTAL_INDEX = 0;

	/**
	 * index of market share score in {@link #P_SCORES}.
	 */
	public static final int MARKETSHARE_INDEX = 1;

	/**
	 * index of profit share score in {@link #P_SCORES}.
	 */
	public static final int PROFITSHARE_INDEX = 2;

	/**
	 * index of transaction success rate score in {@link #P_SCORES}.
	 */
	public static final int TRANSACTIONRATE_INDEX = 3;

	/**
	 * total score.
	 */
	public double total = Double.NaN;

	/**
	 * market share score.
	 */
	public double marketShare = Double.NaN;

	/**
	 * profit share score.
	 */
	public double profitShare = Double.NaN;

	/**
	 * transaction success rate score.
	 */
	public double transactionRate = Double.NaN;

	public void reset() {
		total = Double.NaN;
		marketShare = Double.NaN;
		profitShare = Double.NaN;
		transactionRate = Double.NaN;
	}

	public void updateTotal() {
		total = (marketShare + profitShare + transactionRate) / 3;
	}

	public double[] getScores() {
		final double scores[] = new double[Score.P_SCORES.length];

		scores[Score.MARKETSHARE_INDEX] = marketShare;
		scores[Score.PROFITSHARE_INDEX] = profitShare;
		scores[Score.TRANSACTIONRATE_INDEX] = transactionRate;
		scores[Score.TOTAL_INDEX] = total;

		return scores;
	}

	@Override
	public String toString() {
		return GameReport.Formatter.format(total) + " (" + "MS:"
				+ GameReport.Formatter.format(marketShare) + ", " + "P:"
				+ GameReport.Formatter.format(profitShare) + ", " + "TSR:"
				+ GameReport.Formatter.format(transactionRate) + ")";
	}
}
