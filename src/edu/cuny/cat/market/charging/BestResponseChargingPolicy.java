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

package edu.cuny.cat.market.charging;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.collections15.bag.TreeBag;
import org.apache.log4j.Logger;

import edu.cuny.ai.learning.ReverseDiscountSlidingLearner;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.ProfitAnnouncedEvent;
import edu.cuny.cat.event.RegisteredTradersAnnouncedEvent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Resetable;
import edu.cuny.util.Utils;

/**
 * <p>
 * TODO: does NOT work well for the moment.
 * 
 * 1. need learn from other specialists as well
 * 
 * 2. need more reasonable esitmated utility of a fee in terms of trader
 * popularity
 * 
 * 3. IndivProfit list never gets full as with TraderNum list - check it out !
 * </p>
 * 
 * <p>
 * An adaptive charging policy that estimates the utilities of charges once
 * imposed and then calculate a charge that maximizes the expected utility.
 * {@link edu.cuny.ai.learning.ReverseDiscountSlidingLearner} is currently
 * supposed to be used to estimate the trader popularity with a certain fee.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.memorysize</tt><br>
 * <font size=-1>int >= 1 (8 by default)</font></td>
 * <td valign=top>(the number of days to observe)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>best_response_charging</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.12 $
 * 
 */
public class BestResponseChargingPolicy extends AdaptiveChargingPolicy {

	static Logger logger = Logger.getLogger(BestResponseChargingPolicy.class);

	public static final String P_DEF_BASE = "best_response_charging";

	public static final String P_MEMORYSIZE = "memorysize";

	public static final int DEFAULT_MEMORY_SIZE = 8;

	/**
	 * the highest fees to be considered
	 */
	protected static final double MAX_FEES[] = { 20, 20, 20, 20, 1 };

	/**
	 * the lowest fees to be considered
	 */
	protected static final double MIN_FEES[] = { 0, 0, 0, 0, 0 };

	protected static final double FEE_STEPS[] = { 0.1, 0.1, 0.1, 0.1, 0.05 };

	/**
	 * actually the learners defined in the parent class and known as
	 * {@link edu.cuny.ai.learning.ReverseDiscountSlidingLearner} here for
	 * convenience.
	 */
	protected ReverseDiscountSlidingLearner rdslearners[];

	protected Map<String, Double> cumulativeProfits;

	protected int memorySize;

	protected DataStorage dataStorages[];

	protected double dailyProfit;

	public BestResponseChargingPolicy() {
		cumulativeProfits = Collections
				.synchronizedMap(new HashMap<String, Double>());
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		memorySize = parameters.getInt(base
				.push(BestResponseChargingPolicy.P_MEMORYSIZE), new Parameter(
				BestResponseChargingPolicy.P_DEF_BASE)
				.push(BestResponseChargingPolicy.P_MEMORYSIZE),
				BestResponseChargingPolicy.DEFAULT_MEMORY_SIZE);

	}

	@Override
	public void initialize() {
		super.initialize();

		rdslearners = new ReverseDiscountSlidingLearner[learners.length];
		dataStorages = new DataStorage[learners.length];
		for (int i = 0; i < learners.length; i++) {
			if (learners[i] != null) {
				rdslearners[i] = (ReverseDiscountSlidingLearner) learners[i];
				dataStorages[i] = new DataStorage();
				dataStorages[i].setSize(memorySize);
			}
		}
	}

	@Override
	public void reset() {
		super.reset();

		// clear previous cumulative profits
		cumulativeProfits.clear();
		for (final DataStorage dataStorage : dataStorages) {
			if (dataStorage != null) {
				dataStorage.reset();
			}
		}
	}

	protected void updateFees() {
		for (int i = 0; i < dataStorages.length; i++) {
			if (dataStorages[i] != null) {

				BestResponseChargingPolicy.logger.info(dataStorages[i].toString()
						+ "\n");

				fees[i] = dataStorages[i].getOptimalFee(
						BestResponseChargingPolicy.MIN_FEES[i],
						BestResponseChargingPolicy.MAX_FEES[i],
						BestResponseChargingPolicy.FEE_STEPS[i]);
				BestResponseChargingPolicy.logger.info("best fee: "
						+ Utils.formatter.format(fees[i]));

				fees[i] += perturbations[i].nextDouble();
				if (fees[i] < 0) {
					fees[i] = 0;
				}

				BestResponseChargingPolicy.logger.info("adjusted fee: "
						+ Utils.formatter.format(fees[i]));
				BestResponseChargingPolicy.logger.info("\n");
			}
		}
	}

	protected void updateSpecialistProfit(final Specialist specialist) {
		if (getAuctioneer().getName().equals(specialist.getId())) {

			double prevCumulativeProfit = 0;
			if (cumulativeProfits.containsKey(specialist.getId())) {
				prevCumulativeProfit = cumulativeProfits.get(specialist.getId())
						.doubleValue();
			}

			dailyProfit = specialist.getAccount().getBalance() - prevCumulativeProfit;
			cumulativeProfits.put(specialist.getId(), new Double(specialist
					.getAccount().getBalance()));
		}
	}

	protected void updateRegisteredTraders(
			final RegisteredTradersAnnouncedEvent event) {
		if (event.getSpecialist().getId().equals(getAuctioneer().getName())) {
			final int numOfTraders = event.getNumOfTraders();
			for (int i = 0; i < rdslearners.length; i++) {
				if (rdslearners[i] != null) {
					rdslearners[i].train(numOfTraders);
					dataStorages[i].addTraderNum(numOfTraders, getFees()[i]);
					dataStorages[i].addIndivProfit(dailyProfit / numOfTraders,
							numOfTraders);
					for (int j = 1; j < rdslearners[i].getWindowSize(); j++) {
						dataStorages[i].updateTraderNum(rdslearners[i].getOutput(j), j);
					}
				}
			}
		}
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof DayClosedEvent) {
			updateFees();
		} else if (event instanceof ProfitAnnouncedEvent) {
			updateSpecialistProfit(((ProfitAnnouncedEvent) event).getSpecialist());
		} else if (event instanceof RegisteredTradersAnnouncedEvent) {
			updateRegisteredTraders((RegisteredTradersAnnouncedEvent) event);
		}
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n" + Utils.indent("memorysize:" + memorySize);

		return s;
	}

	/**
	 * keeps track of numbers of registered trader and average profits made for
	 * each trader
	 */
	class DataStorage implements Resetable {

		protected LinkedList<TraderNumItem> traderNums = new LinkedList<TraderNumItem>();

		protected LinkedList<IndivProfitItem> indivProfits = new LinkedList<IndivProfitItem>();

		protected TreeBag<TraderNumItem> sortedTraderNums = new TreeBag<TraderNumItem>(
				new TraderNumItemComparator());

		protected TreeBag<IndivProfitItem> sortedIndivProfits = new TreeBag<IndivProfitItem>(
				new IndivProfitItemComparator());

		Maximizer maximizer = new Maximizer();

		protected int size;

		public void reset() {
			traderNums.clear();
			indivProfits.clear();
			sortedTraderNums.clear();
			sortedIndivProfits.clear();
		}

		public void setSize(final int size) {
			this.size = size;
		}

		public int getSize() {
			return size;
		}

		public void addIndivProfit(final double profit, final double traderNum) {
			final IndivProfitItem item = new IndivProfitItem(profit, traderNum);

			if (indivProfits.size() >= size) {
				final Object first = indivProfits.removeFirst();
				sortedIndivProfits.remove(first);
			}

			indivProfits.add(item);
			sortedIndivProfits.add(item);
		}

		public void addTraderNum(final double traderNum, final double fee) {
			final TraderNumItem item = new TraderNumItem(traderNum, fee);

			if (traderNums.size() >= size) {
				final Object first = traderNums.removeFirst();
				sortedTraderNums.remove(first);
			}

			traderNums.add(item);
			sortedTraderNums.add(item);
		}

		/**
		 * 
		 * @param traderNum
		 *          the new estimated number of traders with the charge on day
		 *          <code>day</code>
		 * @param day
		 *          day no.
		 */
		public void updateTraderNum(final double traderNum, final int day) {
			if (traderNums.size() - 1 - day >= 0) {
				final TraderNumItem item = traderNums.get(traderNums.size() - 1 - day);
				sortedTraderNums.remove(item);
				item.traderNum = traderNum;
				sortedTraderNums.add(item);
			}
		}

		public double getOptimalFee(final double minFee, final double maxFee,
				final double step) {
			maximizer.reset();

			double curFee = minFee;
			double curN = -1;
			double nextFee = -1;
			double nextN = -1;

			final Iterator<TraderNumItem> iterator = sortedTraderNums.iterator();
			while (iterator.hasNext()) {
				final TraderNumItem item = iterator.next();
				if (item.fee <= curFee) {
					if (curN < item.traderNum) { // choose the maximal estimated value on
						// the left-hand range
						curN = item.traderNum;
					}
				} else {
					if (item.fee >= maxFee) {
						nextFee = maxFee;
					} else {
						nextFee = item.fee;
					}
					nextN = item.traderNum;

					if (curN < 0) {
						curN = nextN;
					}

					maximizer.calculateMax(curFee, curN, nextFee, nextN, step);

					curFee = nextFee;
					curN = nextN;

					if (curFee >= maxFee) {
						break;
					}
				}
			}

			if (curFee < maxFee) {
				maximizer.calculateMax(curFee, curN, maxFee, curN, step);
			}

			return maximizer.bestFee;
		}

		@Override
		public String toString() {
			String s = "trader_num_estimated: ";
			final Iterator<TraderNumItem> nitor = sortedTraderNums.iterator();
			while (nitor.hasNext()) {
				final TraderNumItem item = nitor.next();
				s += "C" + Utils.formatter.format(item.fee) + ", N"
						+ Utils.formatter.format(item.traderNum) + " | ";
			}

			s += "\nprofit_per_trader_estimated: ";

			final Iterator<IndivProfitItem> pitor = sortedIndivProfits.iterator();
			while (pitor.hasNext()) {
				final IndivProfitItem item = pitor.next();
				s += "N" + Utils.formatter.format(item.traderNum) + ", P"
						+ Utils.formatter.format(item.profit) + " | ";
			}

			return s;
		}

		protected double getEstimatedIndivProfit(final double traderNum) {

			double profit = 0;

			if (traderNum <= 0) {
				return profit;
			}

			double currentN = 0;
			double currentProfit = 0;

			double nextN = -1;
			double nextProfit = -1;

			final Iterator<IndivProfitItem> iterator = sortedIndivProfits.iterator();
			while (iterator.hasNext()) {
				final IndivProfitItem item = iterator.next();
				if (item.traderNum <= traderNum) {
					currentN = item.traderNum;
					currentProfit = item.profit;
				} else {
					nextN = item.traderNum;
					nextProfit = item.profit;
					break;
				}
			}

			if (nextN < 0) {
				if (currentN != 0) {
					profit = currentProfit * traderNum / currentN;
				}
			} else if (nextN != currentN) {
				profit = currentProfit
						+ ((nextProfit - currentProfit) * ((traderNum - currentN) / (nextN - currentN)));
			} else {
				profit = (currentProfit + nextProfit) / 2;
			}

			return profit;
		}

		class Maximizer {

			double highestExpectedProfit;

			double bestFee;

			public void reset() {
				highestExpectedProfit = 0;
				bestFee = 0;
			}

			public void calculateMax(final double start, final double curN,
					final double end, final double nextN, double step) {

				if (curN == nextN) {
					step = Math.abs(end - start) + step;
				}

				// logger.info("calculateMax: C" + formatter.format(start) + ", N"
				// + formatter.format(curN) + " -> C" + formatter.format(end) + ", N"
				// + formatter.format(nextN) + "\t step: C" + formatter.format(step));

				double profit = 0;

				double num = 0;

				for (double fee = start; fee < end; fee += step) {
					num = curN + (fee - start) * (nextN - curN) / (end - start);
					profit = getEstimatedIndivProfit(num);

					// logger.info("\t estimate C" + formatter.format(fee) + " -> N"
					// + formatter.format(num) + ": P" + formatter.format(profit));

					profit *= num;

					if (profit > highestExpectedProfit) {
						highestExpectedProfit = profit;
						bestFee = fee;
					}
				}

				// logger.info("\t >>> C" + formatter.format(bestFee) + " >>> TP"
				// + formatter.format(highestExpectedProfit) + "\n");
			}
		}

		class IndivProfitItem {
			double profit;

			double traderNum;

			public IndivProfitItem(final double profit, final double traderNum) {
				this.profit = profit;
				this.traderNum = traderNum;
			}
		}

		class IndivProfitItemComparator implements Comparator<IndivProfitItem> {

			public int compare(final IndivProfitItem item0,
					final IndivProfitItem item1) {
				if (item0.traderNum > item1.traderNum) {
					return 1;
				} else if (item0.traderNum < item1.traderNum) {
					return -1;
				} else {
					return 0;
				}
			}
		}

		class TraderNumItem {
			double traderNum;

			double fee;

			public TraderNumItem(final double traderNum, final double fee) {
				this.traderNum = traderNum;
				this.fee = fee;
			}
		}

		class TraderNumItemComparator implements Comparator<TraderNumItem> {

			public int compare(final TraderNumItem item0, final TraderNumItem item1) {
				if (item0.fee > item1.fee) {
					return 1;
				} else if (item0.fee < item1.fee) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	}
}