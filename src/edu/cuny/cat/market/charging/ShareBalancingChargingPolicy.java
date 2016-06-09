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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.ProfitAnnouncedEvent;
import edu.cuny.cat.event.RegisteredTradersAnnouncedEvent;
import edu.cuny.cat.stat.ReportVariable;
import edu.cuny.event.Event;
import edu.cuny.util.Utils;

/**
 * <p>
 * This charging policy aims to maintain a balance between its profit share and
 * market share by adjusting its charges, since generally higher charges tend to
 * bring more profit but lower market share, and lower charges do the opposite.
 * </p>
 * 
 * <p>
 * When the profit share score is higher than market share score, it learns from
 * the daily winner on market share; while when the market share score is
 * higher, it learns from the daily winner on profit share. To allow it to learn
 * from itself, a perturbation is introduced. When the goal is to increase
 * market share, the perturbation is negative, and when the goal is to increase
 * profit share, the perturbation is positive.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 * 
 */
public class ShareBalancingChargingPolicy extends AdaptiveChargingPolicy {

	static Logger logger = Logger.getLogger(ShareBalancingChargingPolicy.class);

	protected final Map<String, Double> cumulativeProfits;

	protected Map<String, Double> dailyProfits;

	protected Specialist dailyProfitLeader;

	protected double maxDailyProfit;

	protected Map<String, Integer> dailyTraderDistributions;

	protected Specialist dailyPopularityLeader;

	protected final Event event;

	public ShareBalancingChargingPolicy() {
		cumulativeProfits = Collections
				.synchronizedMap(new HashMap<String, Double>());
		dailyProfits = Collections.synchronizedMap(new HashMap<String, Double>());
		dailyTraderDistributions = Collections
				.synchronizedMap(new HashMap<String, Integer>());
		event = new Event(this);
	}

	@Override
	public void reset() {
		super.reset();

		// clear previous cumulative profits
		cumulativeProfits.clear();
	}

	protected void dayInitialize() {
		dailyProfitLeader = null;
		maxDailyProfit = Double.NEGATIVE_INFINITY;
		dailyProfits.clear();

		dailyPopularityLeader = null;
		dailyTraderDistributions.clear();

		event.clearValues();
	}

	protected void updateSpecialistProfit(final Specialist specialist) {
		double prevCumulativeProfit = 0;
		if (cumulativeProfits.containsKey(specialist.getId())) {
			prevCumulativeProfit = cumulativeProfits.get(specialist.getId())
					.doubleValue();
		}

		final double dailyProfit = specialist.getAccount().getBalance()
				- prevCumulativeProfit;
		dailyProfits.put(specialist.getId(), new Double(dailyProfit));
		cumulativeProfits.put(specialist.getId(), new Double(specialist
				.getAccount().getBalance()));

		// logger.info("\t" + specialist.getId() + " : " + dailyProfit);

		if (maxDailyProfit < dailyProfit) {
			dailyProfitLeader = specialist;
			maxDailyProfit = dailyProfit;
		}

		// logger.info("\t winner \t" + dailyWinner.getId() + " : " +
		// maxDailyProfit);
	}

	protected void updateSpecialistPopularity(final Specialist specialist,
			final int numOfTraders) {
		dailyTraderDistributions.put(specialist.getId(), new Integer(numOfTraders));

		if ((dailyPopularityLeader == null)
				|| (dailyTraderDistributions.get(dailyPopularityLeader.getId())
						.intValue() < numOfTraders)) {
			dailyPopularityLeader = specialist;
		}
	}

	protected double calculateProfitShare() {
		double num = Double.NaN;
		double total = 0.0;
		final Iterator<Double> itor = dailyProfits.values().iterator();
		while (itor.hasNext()) {
			total += (itor.next()).doubleValue();
		}

		if (dailyProfits.containsKey(getAuctioneer().getName())) {
			num = dailyProfits.get(getAuctioneer().getName()).doubleValue();
		}
		return num / total;
	}

	protected double calculateMarketShare() {
		double num = Double.NaN;
		double total = 0.0;
		final Iterator<Integer> itor = dailyTraderDistributions.values().iterator();
		while (itor.hasNext()) {
			total += itor.next().doubleValue();
		}

		if (dailyTraderDistributions.containsKey(getAuctioneer().getName())) {
			num = dailyTraderDistributions.get(getAuctioneer().getName())
					.doubleValue();
		}
		return num / total;
	}

	/**
	 * 
	 * @param leader
	 * @param lower
	 *          whether to learn to lower charges or not
	 */
	protected void learnCharges(final Specialist leader, final boolean lower) {

		if (leader != null) {

			for (int i = 0; i < learners.length; i++) {
				if (learners[i] != null) {
					final double fee = leader.getFees()[i] - (lower ? 1 : -1)
							* perturbations[i].nextDouble();
					learners[i].train(fee);
					event.setValue(getAuctioneer().getName() + ReportVariable.SEPARATOR
							+ AdaptiveChargingPolicy.P_DEF_BASE + ReportVariable.SEPARATOR
							+ ChargingPolicy.P_FEES[i] + ReportVariable.SEPARATOR
							+ AdaptiveChargingPolicy.P_LEARNER, new Double(fee));

					ShareBalancingChargingPolicy.logger.info("training "
							+ ChargingPolicy.P_FEES[i] + " learner with fee "
							+ Utils.formatter.format(fee));

					fees[i] = learners[i].act();
					if (fees[i] < 0) {
						fees[i] = 0;
					}

					if ((ChargingPolicy.FEE_TYPES[i] == ChargingPolicy.FRACTIONAL)
							&& (fees[i] > 0.99)) {
						ShareBalancingChargingPolicy.logger
								.info("adjusted fractional fee to 0.99 from " + fees[i]);
						fees[i] = 0.99;
					} else if ((ChargingPolicy.FEE_TYPES[i] == ChargingPolicy.FLAT)
							&& (fees[i] > 1000)) {
						fees[i] = 1000;
					}

					// logger.info("fee for next day: " +
					// Utils.formatter.format(fees[i]));
				}
			}
		} else {
			ShareBalancingChargingPolicy.logger
					.error("daily leader is null ! This may be a bug in jcat.");
		}
	}

	protected void updateFees() {

		final double marketShare = calculateMarketShare();
		final double profitShare = calculateProfitShare();

		if (marketShare <= profitShare) {
			ShareBalancingChargingPolicy.logger
					.info("learning from popularity leader - "
							+ dailyPopularityLeader.getId() + " ...");
			learnCharges(dailyPopularityLeader, true);
		} else {
			ShareBalancingChargingPolicy.logger
					.info("learning from profiting leader - " + dailyProfitLeader.getId()
							+ " ...");
			learnCharges(dailyProfitLeader, false);
		}

		ShareBalancingChargingPolicy.logger.info("\n");

		// TODO: need to update the content of the event
		//
		// Galaxy.getInstance().getTyped(Game.P_CAT,
		// EventEngine.class).dispatchEvent(
		// ReportVariable.class, event);
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof DayOpeningEvent) {
			dayInitialize();
		} else if (event instanceof DayClosedEvent) {
			updateFees();
		} else if (event instanceof ProfitAnnouncedEvent) {
			updateSpecialistProfit(((ProfitAnnouncedEvent) event).getSpecialist());
		} else if (event instanceof RegisteredTradersAnnouncedEvent) {
			updateSpecialistPopularity(((RegisteredTradersAnnouncedEvent) event)
					.getSpecialist(), ((RegisteredTradersAnnouncedEvent) event)
					.getNumOfTraders());
		}
	}
}