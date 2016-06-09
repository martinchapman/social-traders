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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.market.EfficiencyCalculator;
import edu.cuny.cat.market.EquilibriumCalculator;
import edu.cuny.cat.market.matching.FourHeapShoutEngine;
import edu.cuny.cat.registry.Registry;
import edu.cuny.cat.server.GameController;

/**
 * A report calculating actual efficiency, actual convergence coefficient, and
 * equilibrium price, quantitiy, and profit of specialists.
 * 
 * <p>
 * <b>Report variables</b>
 * </p>
 * <table cellpadding="5">
 * <tr>
 * <td> <code>global.efficiency</code></td>
 * <td>the efficiency of the global market.</td>
 * </tr>
 * <tr>
 * <td> <code>global.convergence_coefficient</code></td>
 * <td>the convergence coefficient of the global market.</td>
 * </tr>
 * <tr>
 * <td> <code>global.profit</code></td>
 * <td>the profit dispersion of the global market.</td>
 * </tr>
 * <tr>
 * <td> <code>global.equilibrium.price</code></td>
 * <td>the equilibrium price of the global market.</td>
 * </tr>
 * <tr>
 * <td> <code>global.equilibrium.quantity</code></td>
 * <td>the equilibrium quantity of the global market.</td>
 * </tr>
 * <tr>
 * <td> <code>global.equilibrium.profit</code></td>
 * <td>the equilibrium profit of the global market.</td>
 * </tr>
 * </tr>
 * <tr>
 * <td> <code>global.profit_dispersion</code></td>
 * <td>the profit dispersion of the global market.</td>
 * </tr>
 * <tr>
 * <td> <code>global.equilibrium.&lt;shout&gt;.quantity</code></td>
 * <td>the total quantity of goods in shouts of certain types at the global
 * market.</td>
 * </tr>
 * <tr>
 * <td> <code>global.equilibrium.&lt;shout&gt;.price.min</code></td>
 * <td>the min price of shouts of certain type at the global market.</td>
 * </tr>
 * <tr>
 * <td> <code>global.equilibrium.&lt;shout&gt;.price.max</code></td>
 * <td>the max price of shouts of certain type at the global market.</td>
 * </tr>
 * <tr>
 * <td> <code>global.equilibrium.&lt;shout&gt;.price.mean</code></td>
 * <td>the mean price of shouts of certain type at the global market.</td>
 * </tr>
 * <tr>
 * <td> <code>global.equilibrium.&lt;shout&gt;.price.stdev</code></td>
 * <td>the stdev of prices of shouts of certain type at the global market.</td>
 * </tr>
 * <td> <code>&lt;specialist&gt;.efficiency</code></td>
 * <td>the efficiency of a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.convergence_coefficient</code></td>
 * <td>the convergence coefficient of a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.profit_dispersion</code></td>
 * <td>the profit dispersion of a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.equilibrium.price</code></td>
 * <td>the equilibrium price of a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.equilibrium.quantity</code></td>
 * <td>the equilibrium quantity of a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.equilibrium.profit</code></td>
 * <td>the equilibrium profit of a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.equilibrium.&lt;shout&gt;.quantity</code></td>
 * <td>the total quantity of goods in shouts of certain type at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.equilibrium.&lt;shout&gt;.price.min</code></td>
 * <td>the min price of shouts of certain type at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.equilibrium.&lt;shout&gt;.price.max</code></td>
 * <td>the max price of shouts of certain type at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.equilibrium.&lt;shout&gt;.price.mean</code></td>
 * <td>the mean price of shouts of certain type at a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.equilibrium.&lt;shout&gt;.price.stdev</code></td>
 * <td>the stdev of prices of shouts of certain type at a specialist.</td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.13 $
 */

public class MarketMetricsReport extends DirectRevelationReport {
	static Logger logger = Logger.getLogger(MarketMetricsReport.class);

	/**
	 * all transactions made today grouped based on specialists
	 */
	protected Map<String, Set<Transaction>> dailyTransactions;

	protected Registry registry;

	public MarketMetricsReport() {
		registry = GameController.getInstance().getRegistry();
		dailyTransactions = Collections
				.synchronizedMap(new HashMap<String, Set<Transaction>>());
	}

	public Map<ReportVariable, ?> getVariables() {
		return null;
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof TransactionExecutedEvent) {
			updateCurrentDayTransactions((TransactionExecutedEvent) event);
		} else if (event instanceof DayClosedEvent) {
			calculate();
		} else if (event instanceof DayOpeningEvent) {
			dailyTransactions.clear();
		}
	}

	public void updateCurrentDayTransactions(final TransactionExecutedEvent event) {
		final Transaction transaction = event.getTransaction();
		Set<Transaction> transactionSet = dailyTransactions.get(transaction
				.getSpecialist().getId());
		if (transactionSet == null) {
			transactionSet = Collections.synchronizedSet(new HashSet<Transaction>());
			dailyTransactions
					.put(transaction.getSpecialist().getId(), transactionSet);
		}

		transactionSet.add(transaction);
	}

	public void produceUserOutput() {
	}

	@Override
	public void calculate() {
		super.calculate();

		calculateIndividually();
		calculateGlobally();
	}

	/**
	 * calculates each individual specialist
	 */
	protected void calculateIndividually() {
		final Specialist specialists[] = registry.getSpecialists();

		for (final Specialist specialist2 : specialists) {

			final FourHeapShoutEngine shoutEngine = shoutEngines.get(specialist2
					.getId());
			final EquilibriumCalculator equilCal = new EquilibriumCalculator(
					shoutEngine);

			final Set<Transaction> transactionSet = dailyTransactions.get(specialist2
					.getId());

			final EfficiencyCalculator effCal = new EfficiencyCalculator(equilCal,
					transactionSet, specialist2.getTraderMap().values());

			reportVariables(specialist2.getId(), equilCal, effCal);
		}
	}

	/**
	 * calculates the global market including all the specialists
	 */
	protected void calculateGlobally() {

		final EquilibriumCalculator equilCal = new EquilibriumCalculator(
				globalShoutEngine);

		final Set<Transaction> globalTransactions = Collections
				.synchronizedSet(new HashSet<Transaction>());

		final Iterator<Set<Transaction>> iter = dailyTransactions.values()
				.iterator();
		while (iter.hasNext()) {
			globalTransactions.addAll(iter.next());
		}

		final HashSet<Trader> traders = new HashSet<Trader>();
		final Specialist specialists[] = registry.getSpecialists();

		for (final Specialist specialist2 : specialists) {
			traders.addAll(specialist2.getTraderMap().values());
		}

		final EfficiencyCalculator effCal = new EfficiencyCalculator(equilCal,
				globalTransactions, traders);

		reportVariables(GameReport.GLOBAL, equilCal, effCal);
	}

	private void reportVariables(final String name,
			final EquilibriumCalculator equilCal, final EfficiencyCalculator effCal) {
		final ReportVariableBoard board = ReportVariableBoard.getInstance();
		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EQUILIBRIUM
				+ ReportVariable.SEPARATOR + GameReport.QUANTITY, equilCal
				.getEquilibriumQuantity());
		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EQUILIBRIUM
				+ ReportVariable.SEPARATOR + GameReport.PRICE, equilCal
				.getMidEquilibriumPrice());
		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EQUILIBRIUM
				+ ReportVariable.SEPARATOR + GameReport.PROFIT, effCal
				.getTheoreticalProfit());

		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EQUILIBRIUM
				+ ReportVariable.SEPARATOR + GameReport.ASK + ReportVariable.SEPARATOR
				+ GameReport.QUANTITY, equilCal.getAskPriceDistribution().getN());
		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EQUILIBRIUM
				+ ReportVariable.SEPARATOR + GameReport.ASK + ReportVariable.SEPARATOR
				+ GameReport.PRICE + ReportVariable.SEPARATOR + GameReport.MEAN,
				equilCal.getAskPriceDistribution().getMean());
		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EQUILIBRIUM
				+ ReportVariable.SEPARATOR + GameReport.ASK + ReportVariable.SEPARATOR
				+ GameReport.PRICE + ReportVariable.SEPARATOR + GameReport.STDEV,
				equilCal.getAskPriceDistribution().getStdDev());
		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EQUILIBRIUM
				+ ReportVariable.SEPARATOR + GameReport.ASK + ReportVariable.SEPARATOR
				+ GameReport.PRICE + ReportVariable.SEPARATOR + GameReport.MAX,
				equilCal.getAskPriceDistribution().getMax());
		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EQUILIBRIUM
				+ ReportVariable.SEPARATOR + GameReport.ASK + ReportVariable.SEPARATOR
				+ GameReport.PRICE + ReportVariable.SEPARATOR + GameReport.MIN,
				equilCal.getAskPriceDistribution().getMin());

		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EQUILIBRIUM
				+ ReportVariable.SEPARATOR + GameReport.BID + ReportVariable.SEPARATOR
				+ GameReport.QUANTITY, equilCal.getBidPriceDistribution().getN());
		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EQUILIBRIUM
				+ ReportVariable.SEPARATOR + GameReport.BID + ReportVariable.SEPARATOR
				+ GameReport.PRICE + ReportVariable.SEPARATOR + GameReport.MEAN,
				equilCal.getBidPriceDistribution().getMean());
		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EQUILIBRIUM
				+ ReportVariable.SEPARATOR + GameReport.BID + ReportVariable.SEPARATOR
				+ GameReport.PRICE + ReportVariable.SEPARATOR + GameReport.STDEV,
				equilCal.getBidPriceDistribution().getStdDev());
		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EQUILIBRIUM
				+ ReportVariable.SEPARATOR + GameReport.BID + ReportVariable.SEPARATOR
				+ GameReport.PRICE + ReportVariable.SEPARATOR + GameReport.MAX,
				equilCal.getBidPriceDistribution().getMax());
		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EQUILIBRIUM
				+ ReportVariable.SEPARATOR + GameReport.BID + ReportVariable.SEPARATOR
				+ GameReport.PRICE + ReportVariable.SEPARATOR + GameReport.MIN,
				equilCal.getBidPriceDistribution().getMin());

		board.reportValue(name + ReportVariable.SEPARATOR + GameReport.EFFICIENCY,
				effCal.getEA());
		board.reportValue(name + ReportVariable.SEPARATOR
				+ GameReport.CONVERGENCE_COEFFICIENT, effCal.getConvergenceCoeff());
		board.reportValue(name + ReportVariable.SEPARATOR
				+ GameReport.PROFIT_DISPERSION, effCal.getProfitDispersion());
	}
}
