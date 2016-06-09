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
/*
 * JASA Java Auction Simulator API
 * Copyright (C) 2001-2005 Steve Phelps
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

package edu.cuny.cat.market;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.core.Transaction;
import edu.cuny.util.CumulativeDistribution;

/**
 * <p>
 * A class calculating efficiency, convergence coefficient, and profit
 * dispersion based on a {@link EquilibriumCalculator} with true shouts from
 * traders and a set of actual transactions.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.11 $
 */

public class EfficiencyCalculator {

	protected List<Shout> matchedShouts;

	/**
	 * The profits of the buyers in theoretical equilibrium.
	 */
	protected double pBCE = 0;

	/**
	 * The profits of the sellers in theoretical equilibrium.
	 */
	protected double pSCE = 0;

	/**
	 * The actual profits of the buyers.
	 */
	protected double pBA = 0;

	/**
	 * The actual profits of the sellers.
	 */
	protected double pSA = 0;

	/**
	 * Market efficiency.
	 */
	protected double eA;

	/**
	 * Market convergence coefficient
	 */
	protected double convergenceCoeff;

	protected double profitDispersion;

	protected double equilibriumPrice;

	protected Collection<Transaction> transactions;

	protected Collection<Trader> traders;

	protected static Logger logger = Logger.getLogger(EfficiencyCalculator.class);

	public EfficiencyCalculator(final EquilibriumCalculator equilCal,
			final Collection<Transaction> transactions,
			final Collection<Trader> traders) {
		matchedShouts = equilCal.getMatchedShouts();
		this.transactions = transactions;
		this.traders = traders;
		equilibriumPrice = equilCal.getMidEquilibriumPrice();

		calculateEfficiency();
		calculateConvergenceCoeff();
		calculateProfitDispersion();
	}

	protected void calculateEfficiency() {
		calculateTheoreticalProfit();
		calculateActualProfit();

		// TODO: possibly divided by 0 (=pBCE+pSCE).
		eA = (pBA + pSA) / (pBCE + pSCE) * 100;
	}

	protected void calculateTheoreticalProfit() {
		pSCE = pBCE = 0;

		if (matchedShouts != null) {
			final Iterator<Shout> i = matchedShouts.iterator();
			while (i.hasNext()) {
				final Shout bid = i.next();
				final Shout ask = i.next();

				pSCE = pSCE + (equilibriumPrice - ask.getTrader().getPrivateValue())
						* ask.getQuantity();
				pBCE = pBCE + (bid.getTrader().getPrivateValue() - equilibriumPrice)
						* bid.getQuantity();
			}
		}
	}

	protected void calculateActualProfit() {
		pSA = pBA = 0;

		if (transactions != null) {
			final Iterator<Transaction> i = transactions.iterator();
			while (i.hasNext()) {
				final Transaction transaction = i.next();
				pSA = pSA
						+ (transaction.getPrice() - transaction.getAsk().getTrader()
								.getPrivateValue()) * transaction.getQuantity();
				pBA = pBA
						+ (transaction.getBid().getTrader().getPrivateValue() - transaction
								.getPrice()) * transaction.getQuantity();
			}
		}
	}

	protected void calculateConvergenceCoeff() {
		convergenceCoeff = Double.NaN;

		if (transactions != null) {
			final CumulativeDistribution dist = new CumulativeDistribution();

			final Iterator<Transaction> i = transactions.iterator();
			while (i.hasNext()) {
				final Transaction transaction = i.next();
				dist.newData(transaction.getPrice());
			}

			// TODO: possibly divided by 0, (transactions.size() == 0, or
			// equilibriumPrice doesn't exist.
			convergenceCoeff = dist.getStdDev(equilibriumPrice) * 100
					/ equilibriumPrice;
		}
	}

	private void updateActualProfit(final Map<String, Double> actualProfits,
			final Trader trader, final Transaction transaction) {
		double profit;
		if (actualProfits.containsKey(trader.getId())) {
			profit = (actualProfits.get(trader.getId())).doubleValue();
		} else {
			profit = 0;
		}

		profit += Math.abs(trader.getPrivateValue() - transaction.getPrice())
				* transaction.getQuantity();
		actualProfits.put(trader.getId(), new Double(profit));
	}

	protected void calculateProfitDispersion() {
		// calculate actual profits of traders
		final Map<String, Double> actualProfits = new HashMap<String, Double>();
		if (transactions != null) {
			final Iterator<Transaction> i = transactions.iterator();
			while (i.hasNext()) {
				final Transaction transaction = i.next();
				updateActualProfit(actualProfits, transaction.getAsk().getTrader(),
						transaction);
				updateActualProfit(actualProfits, transaction.getBid().getTrader(),
						transaction);
			}
		}

		// calculate profit dispersion
		final CumulativeDistribution dist = new CumulativeDistribution();
		final Iterator<Trader> itor = traders.iterator();
		double theoProfit, actualProfit;
		while (itor.hasNext()) {
			final Trader trader = itor.next();
			if ((trader.isSeller() && (trader.getPrivateValue() <= equilibriumPrice))
					|| (!trader.isSeller() && (trader.getPrivateValue() >= equilibriumPrice))) {
				theoProfit = Math.abs(trader.getPrivateValue() - equilibriumPrice)
						* trader.getEntitlement();
			} else {
				theoProfit = 0;
			}

			if (actualProfits.containsKey(trader.getId())) {
				actualProfit = actualProfits.get(trader.getId()).doubleValue();
			} else {
				actualProfit = 0;
			}

			dist.newData(Math.abs(theoProfit - actualProfit));
		}

		profitDispersion = dist.getStdDev(0);
	}

	public double getActualProfit() {
		return pBA + pSA;
	}

	public double getTheoreticalProfit() {
		return pBCE + pSCE;
	}

	public double getEA() {
		return eA;
	}

	public double getConvergenceCoeff() {
		return convergenceCoeff;
	}

	public double getProfitDispersion() {
		return profitDispersion;
	}
}
