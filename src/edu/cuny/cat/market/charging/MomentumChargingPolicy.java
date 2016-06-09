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
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.ProfitAnnouncedEvent;
import edu.cuny.cat.stat.ReportVariable;
import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Resetable;
import edu.cuny.util.Utils;

/**
 * <p>
 * An adaptive charging policy that learns from charges of the market that made
 * the most profit each day. It can also be viewed as a more complicated version
 * of {@link BaitAndSwitchChargingPolicy}.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.exploring</tt><br>
 * <font size=-1>name of class, implementing {@link TraderExploringMonitor}
 * </font></td>
 * <td valign=top>(exploring monitor that decides whether traders are still
 * exploring or have converged)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>momentum_charging</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.31 $
 * 
 */
public class MomentumChargingPolicy extends AdaptiveChargingPolicy {

	static Logger logger = Logger.getLogger(MomentumChargingPolicy.class);

	public static final String P_DEF_BASE = "momentum_charging";

	public static final String P_EXPLORING = "exploring";

	protected TraderExploringMonitor exploringMonitor;

	protected final Map<String, Double> cumulativeProfits;

	protected Specialist dailyWinner;

	protected double maxDailyProfit;

	protected final Event event;

	public MomentumChargingPolicy() {
		cumulativeProfits = Collections
				.synchronizedMap(new HashMap<String, Double>());
		event = new Event(this);
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(MomentumChargingPolicy.P_DEF_BASE);

		exploringMonitor = parameters
				.getInstanceForParameterEq(base
						.push(MomentumChargingPolicy.P_EXPLORING), defBase
						.push(MomentumChargingPolicy.P_EXPLORING),
						TraderExploringMonitor.class);
		exploringMonitor.setup(parameters, base
				.push(MomentumChargingPolicy.P_EXPLORING));
	}

	@Override
	public void reset() {
		super.reset();

		// clear previous cumulative profits
		cumulativeProfits.clear();
		if (exploringMonitor instanceof Resetable) {
			((Resetable) exploringMonitor).reset();
		}
	}

	protected void dayInitialize() {
		dailyWinner = null;
		maxDailyProfit = Double.NEGATIVE_INFINITY;

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
		cumulativeProfits.put(specialist.getId(), new Double(specialist
				.getAccount().getBalance()));

		// logger.info("\t" + specialist.getId() + " : " + dailyProfit);

		if (maxDailyProfit < dailyProfit) {
			dailyWinner = specialist;
			maxDailyProfit = dailyProfit;
		}

		// logger.info("\t winner \t" + dailyWinner.getId() + " : " +
		// maxDailyProfit);
	}

	protected void updateFees() {

		event.setValue(getAuctioneer().getName() + ReportVariable.SEPARATOR
				+ MomentumChargingPolicy.P_DEF_BASE + ReportVariable.SEPARATOR
				+ MomentumChargingPolicy.P_EXPLORING, new Double(exploringMonitor
				.getExploringFactor()));

		// logger.info("exploring factor: " +
		// exploringMonitor.getExploringFactor());

		if (exploringMonitor.isExploring()) {
			MomentumChargingPolicy.logger.info("trader exploring ...");

			for (int i = 0; i < learners.length; i++) {
				if (learners[i] != null) {
					learners[i].train(0);

					event.setValue(getAuctioneer().getName() + ReportVariable.SEPARATOR
							+ MomentumChargingPolicy.P_DEF_BASE + ReportVariable.SEPARATOR
							+ ChargingPolicy.P_FEES[i] + ReportVariable.SEPARATOR
							+ AdaptiveChargingPolicy.P_LEARNER, new Double(0));

					fees[i] = learners[i].act();
					if (fees[i] < 0) {
						fees[i] = 0;
					}
					MomentumChargingPolicy.logger.info("fee for next day: " + fees[i]);
				} else {
					MomentumChargingPolicy.logger
							.error("learner is null in luring exploring traders !");
				}
			}
		} else {
			MomentumChargingPolicy.logger.info("learning from winner ...");

			if (dailyWinner != null) {

				if (dailyWinner.getId().equalsIgnoreCase(getAuctioneer().getName())) {

					// myself is the winner, then lowers the current charge a little
					// bit

					for (int i = 0; i < learners.length; i++) {
						if (learners[i] != null) {
							final double margin = perturbations[i].nextDouble();
							learners[i].train(dailyWinner.getFees()[i] - margin);
							event.setValue(
									getAuctioneer().getName() + ReportVariable.SEPARATOR
											+ MomentumChargingPolicy.P_DEF_BASE
											+ ReportVariable.SEPARATOR + ChargingPolicy.P_FEES[i]
											+ ReportVariable.SEPARATOR
											+ AdaptiveChargingPolicy.P_LEARNER, new Double(
											dailyWinner.getFees()[i] - margin));

							fees[i] = learners[i].act();
							if (fees[i] < 0) {
								fees[i] = 0;
							}

							if ((ChargingPolicy.FEE_TYPES[i] == ChargingPolicy.FRACTIONAL)
									&& (fees[i] > 0.99)) {
								MomentumChargingPolicy.logger
										.info("adjusted fractional fee to 0.99 from " + fees[i]);
								fees[i] = 0.99;
							}

							MomentumChargingPolicy.logger.info("training "
									+ ChargingPolicy.P_FEES[i] + " learner with fee "
									+ (dailyWinner.getFees()[i]) + " (self)");

							MomentumChargingPolicy.logger
									.info("fee for next day: " + fees[i]);
						} else {
							MomentumChargingPolicy.logger
									.error("learner is null in learning from a winning self !");
						}
					}
				} else {

					// learn from the winner and train by charge slightly lower than the
					// winner's charge

					MomentumChargingPolicy.logger.info("learning from : "
							+ dailyWinner.getId());

					for (int i = 0; i < learners.length; i++) {
						if (learners[i] != null) {
							final double margin = perturbations[i].nextDouble();
							learners[i].train(dailyWinner.getFees()[i] - margin);
							event.setValue(
									getAuctioneer().getName() + ReportVariable.SEPARATOR
											+ MomentumChargingPolicy.P_DEF_BASE
											+ ReportVariable.SEPARATOR + ChargingPolicy.P_FEES[i]
											+ ReportVariable.SEPARATOR
											+ AdaptiveChargingPolicy.P_LEARNER, new Double(
											dailyWinner.getFees()[i] - margin));

							MomentumChargingPolicy.logger.info("training "
									+ ChargingPolicy.P_FEES[i] + " learner with fee "
									+ (dailyWinner.getFees()[i] - margin) + " (others)");

							fees[i] = learners[i].act();
							if (fees[i] < 0) {
								fees[i] = 0;
							}

							if ((ChargingPolicy.FEE_TYPES[i] == ChargingPolicy.FRACTIONAL)
									&& (fees[i] > 0.99)) {
								MomentumChargingPolicy.logger
										.info("adjusted fractional fee to 0.99 from " + fees[i]);
								fees[i] = 0.99;
							}

							MomentumChargingPolicy.logger
									.info("fee for next day: " + fees[i]);
						} else {
							MomentumChargingPolicy.logger
									.error("learner is null in learning from a winning market !");
						}
					}
				}
			} else {
				MomentumChargingPolicy.logger
						.error("dailyWinner is null ! This may be a bug in jcat.");
			}
		}

		MomentumChargingPolicy.logger.info("\n");

		Galaxy.getInstance().getDefaultTyped(EventEngine.class).dispatchEvent(
				ReportVariable.class, event);
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		exploringMonitor.eventOccurred(event);

		if (event instanceof DayOpeningEvent) {
			dayInitialize();
		} else if (event instanceof DayClosedEvent) {
			updateFees();
		} else if (event instanceof ProfitAnnouncedEvent) {
			updateSpecialistProfit(((ProfitAnnouncedEvent) event).getSpecialist());
		}
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n" + Utils.indent(exploringMonitor.toString());
		return s;
	}

}