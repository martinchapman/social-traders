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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayStatPassEvent;
import edu.cuny.cat.server.GameController;
import edu.cuny.util.CumulativeDistribution;

/**
 * <p>
 * A report tracking shouts and transactions that have been made at specialists
 * day by day.
 * </p>
 * 
 * <p>
 * <b>Report variables</b>
 * </p>
 * <table cellpadding="5">
 * <tr>
 * <td> <code>&lt;specialist&gt;.&lt;shout&gt;.placed</code></td>
 * <td>the number of shouts of certain type placed at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.&lt;shout&gt;.placed.quantity</code></td>
 * <td>the total quantity of goods in the shouts of certain type placed at a
 * specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.&lt;shout&gt;.placed.price.min</code></td>
 * <td>the min price of shouts of certain type placed at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.&lt;shout&gt;.placed.price.max</code></td>
 * <td>the max price of shouts of certain type placed at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.&lt;shout&gt;.placed.price.mean</code></td>
 * <td>the mean price of shouts of certain type placed at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.&lt;shout&gt;.placed.price.stdev</code></td>
 * <td>the stdev of prices of shouts of certain type placed at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.&lt;shout&gt;.accepted</code></td>
 * <td>the number of shouts of certain type accepted at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.&lt;shout&gt;.accepted.quantity</code></td>
 * <td>the total quantity of goods in the shouts of certain type accepted at a
 * specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.&lt;shout&gt;.accepted.price.min</code></td>
 * <td>the min price of shouts of certain type accepted at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.&lt;shout&gt;.accepted.price.max</code></td>
 * <td>the max price of shouts of certain type accepted at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.&lt;shout&gt;.accepted.price.mean</code></td>
 * <td>the mean price of shouts of certain type accepted at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.&lt;shout&gt;.accepted.price.stdev</code></td>
 * <td>the stdev of prices of shouts of certain type accepted at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.transaction</code></td>
 * <td>the number of transactions made at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.transaction.quantity</code></td>
 * <td>the total quantity of goods involved in the transactions made at a
 * specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.transaction.price.min</code></td>
 * <td>the min price of transactions made at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.transaction.price.max</code></td>
 * <td>the max price of transactions made at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.transaction.price.mean</code></td>
 * <td>the mean price of transactions made at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.transaction.price.stdev</code></td>
 * <td>the stdev of prices of transactions made at a specialist.</td>
 * </tr>
 * 
 * </table>
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.19 $
 */

public class ShoutReport implements GameReport {

	static Logger logger = Logger.getLogger(ShoutReport.class);

	public ShoutReport() {
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
		if (event instanceof DayStatPassEvent) {
			if (((DayStatPassEvent) event).getPass() == DayStatPassEvent.FIRST_PASS) {
				generateStat();
			}
		}
	}

	protected void generateStat() {
		final Specialist specialists[] = GameController.getInstance().getRegistry()
				.getSpecialists();
		final Map<String, Integer> specialistIndices = new HashMap<String, Integer>();
		for (int i = 0; i < specialists.length; i++) {
			specialistIndices.put(specialists[i].getId(), new Integer(i));
		}

		// calculate shouts
		final int askPlacedNums[] = new int[specialists.length];
		final int askAcceptedNums[] = new int[specialists.length];
		final int bidPlacedNums[] = new int[specialists.length];
		final int bidAcceptedNums[] = new int[specialists.length];
		final CumulativeDistribution pricesOfAskPlaced[] = ShoutReport
				.createDistArray(specialists.length);
		final CumulativeDistribution pricesOfAskAccepted[] = ShoutReport
				.createDistArray(specialists.length);
		final CumulativeDistribution pricesOfBidPlaced[] = ShoutReport
				.createDistArray(specialists.length);
		final CumulativeDistribution pricesOfBidAccepted[] = ShoutReport
				.createDistArray(specialists.length);

		final Shout shouts[] = GameController.getInstance().getRegistry()
				.getShouts();
		int index;
		for (final Shout shout2 : shouts) {
			if ((shout2.getSpecialist() != null)
					&& specialistIndices.containsKey((shout2.getSpecialist().getId()))) {
				index = specialistIndices.get(shout2.getSpecialist().getId())
						.intValue();
				if ((shout2.getState() == Shout.PLACED)
						|| (shout2.getState() == Shout.MATCHED)) {
					if (shout2.isAsk()) {
						askPlacedNums[index]++;
						pricesOfAskPlaced[index].newData(shout2.getPrice(), shout2
								.getQuantity());
					} else {
						bidPlacedNums[index]++;
						pricesOfBidPlaced[index].newData(shout2.getPrice(), shout2
								.getQuantity());
					}

					if (shout2.getState() == Shout.MATCHED) {
						if (shout2.isAsk()) {
							askAcceptedNums[index]++;
							pricesOfAskAccepted[index].newData(shout2.getPrice(), shout2
									.getQuantity());
						} else {
							bidAcceptedNums[index]++;
							pricesOfBidAccepted[index].newData(shout2.getPrice(), shout2
									.getQuantity());
						}
					}
				}
			}
		}

		for (int i = 0; i < specialists.length; i++) {

			if (pricesOfAskAccepted[i].getN() != pricesOfBidAccepted[i].getN()) {
				ShoutReport.logger
						.error("The accepted supply is not equal to the accepted demand as expected at specialist "
								+ specialists[i].getId());
			}

			ShoutReport.reportVariables(specialists[i].getId(), GameReport.ASK
					+ ReportVariable.SEPARATOR + GameReport.PLACED, askPlacedNums[i],
					pricesOfAskPlaced[i]);
			ShoutReport.reportVariables(specialists[i].getId(), GameReport.ASK
					+ ReportVariable.SEPARATOR + GameReport.ACCEPTED, askAcceptedNums[i],
					pricesOfAskAccepted[i]);
			ShoutReport.reportVariables(specialists[i].getId(), GameReport.BID
					+ ReportVariable.SEPARATOR + GameReport.PLACED, bidPlacedNums[i],
					pricesOfBidPlaced[i]);
			ShoutReport.reportVariables(specialists[i].getId(), GameReport.BID
					+ ReportVariable.SEPARATOR + GameReport.ACCEPTED, bidAcceptedNums[i],
					pricesOfBidAccepted[i]);

		}

		// calculate transactions
		final int transactionNums[] = new int[specialists.length];
		final CumulativeDistribution pricesOfTransaction[] = ShoutReport
				.createDistArray(specialists.length);

		final Transaction transactions[] = GameController.getInstance()
				.getRegistry().getTransactions();
		for (final Transaction transaction2 : transactions) {
			if ((transaction2.getSpecialist() != null)
					&& specialistIndices.containsKey((transaction2.getSpecialist()
							.getId()))) {
				index = specialistIndices.get(transaction2.getSpecialist().getId())
						.intValue();
				transactionNums[index]++;
				pricesOfTransaction[index].newData(transaction2.getPrice(),
						transaction2.getQuantity());
			}
		}

		for (int i = 0; i < specialists.length; i++) {
			if (pricesOfTransaction[i].getN() != pricesOfAskAccepted[i].getN()) {
				ShoutReport.logger
						.error("Transacted quantity is not the accepted supply or demand as expected at "
								+ specialists[i].getId() + " !");
			}

			ShoutReport.reportVariables(specialists[i].getId(),
					GameReport.TRANSACTION, transactionNums[i], pricesOfTransaction[i]);
		}
	}

	/**
	 * creates an array of {@link edu.cuny.util.CumulativeDistribution}s.
	 * 
	 * @param num
	 *          the length of the array
	 * @return the array
	 */
	private static CumulativeDistribution[] createDistArray(final int num) {
		final CumulativeDistribution distributions[] = new CumulativeDistribution[num];
		for (int i = 0; i < distributions.length; i++) {
			distributions[i] = new CumulativeDistribution();
		}

		return distributions;
	}

	private static void reportVariables(final String name, final String type,
			final int num, final CumulativeDistribution priceDistribution) {
		final ReportVariableBoard board = ReportVariableBoard.getInstance();

		board.reportValue(name + ReportVariable.SEPARATOR + type, num);

		board.reportValue(name + ReportVariable.SEPARATOR + type
				+ ReportVariable.SEPARATOR + GameReport.QUANTITY, priceDistribution
				.getN());

		board.reportValue(name + ReportVariable.SEPARATOR + type
				+ ReportVariable.SEPARATOR + GameReport.PRICE
				+ ReportVariable.SEPARATOR + GameReport.MEAN, priceDistribution
				.getMean());
		board.reportValue(name + ReportVariable.SEPARATOR + type
				+ ReportVariable.SEPARATOR + GameReport.PRICE
				+ ReportVariable.SEPARATOR + GameReport.STDEV, priceDistribution
				.getStdDev());
		board
				.reportValue(name + ReportVariable.SEPARATOR + type
						+ ReportVariable.SEPARATOR + GameReport.PRICE
						+ ReportVariable.SEPARATOR + GameReport.MAX, priceDistribution
						.getMax());
		board
				.reportValue(name + ReportVariable.SEPARATOR + type
						+ ReportVariable.SEPARATOR + GameReport.PRICE
						+ ReportVariable.SEPARATOR + GameReport.MIN, priceDistribution
						.getMin());
	}
}
