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

import edu.cuny.ai.learning.MimicryLearner;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.RegisteredTradersAnnouncedEvent;
import edu.cuny.util.CumulativeDistribution;
import edu.cuny.util.FixedLengthQueue;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Resetable;
import edu.cuny.util.Utils;

/**
 * <p>
 * a trader exploration monitor that decides based solely on the trader
 * distribution among markets over a certain number of days.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.threshold</tt><br>
 * <font size=-1>double (0.6 by default)</font></td>
 * <td valign=top>(a threshold value as a filter to decide whether traders are
 * exploring or not)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.threshold</tt><br>
 * <font size=-1>double (0.6 by default)</font></td>
 * <td valign=top>(a threshold value as a filter to decide whether traders are
 * exploring or not)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * 
 * <table>
 * <tr>
 * <td valign=top><tt>multi_day_exploring_monitor</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 * 
 */

public class MultiDayExploringMonitor extends TraderExploringMonitor implements
		Resetable {

	public static final String P_DEF_BASE = "multi_day_exploring_monitor";

	public static final String P_THRESHOLD = "threshold";

	public static final String P_WINDOW_SIZE = "windowsize";

	public static final String P_LEARNER = "learner";

	public static final double DEFAULT_THRESHOLD = 0.6;

	public static final int DEFAULT_WINDOW_SIZE = 5;

	/**
	 * a threshold value to determine whether traders are exploring in general or
	 * not.
	 */
	protected double threshold;

	protected double exploring;

	/**
	 * the number of consecutive days to observe to determine the level of
	 * exploration.
	 */
	protected int windowSize;

	/**
	 * the distribution of traders on current day
	 */
	protected CumulativeDistribution curDailyPopularities;

	/**
	 * estimate the exploring factor based on the history.
	 */
	protected MimicryLearner learner;

	/**
	 * each specialist has a distribution, which tracks its popularity changes
	 * over the sliding window.
	 */
	protected Map<String, FixedLengthQueue> multiDailyPopularities;

	static Logger logger = Logger.getLogger(MultiDayExploringMonitor.class);

	public MultiDayExploringMonitor() {
		curDailyPopularities = new CumulativeDistribution();
		multiDailyPopularities = Collections
				.synchronizedMap(new HashMap<String, FixedLengthQueue>());
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {

		final Parameter defBase = new Parameter(MultiDayExploringMonitor.P_DEF_BASE);

		threshold = parameters.getDoubleWithDefault(base
				.push(MultiDayExploringMonitor.P_THRESHOLD), defBase
				.push(MultiDayExploringMonitor.P_THRESHOLD),
				MultiDayExploringMonitor.DEFAULT_THRESHOLD);

		windowSize = parameters.getIntWithDefault(base
				.push(MultiDayExploringMonitor.P_WINDOW_SIZE), defBase
				.push(MultiDayExploringMonitor.P_WINDOW_SIZE),
				MultiDayExploringMonitor.DEFAULT_WINDOW_SIZE);

		learner = parameters.getInstanceForParameter(base
				.push(MultiDayExploringMonitor.P_LEARNER), defBase
				.push(MultiDayExploringMonitor.P_LEARNER), MimicryLearner.class);
		if (learner instanceof Parameterizable) {
			((Parameterizable) learner).setup(parameters, base
					.push(MultiDayExploringMonitor.P_LEARNER));
		}
		learner.initialize();
	}

	public void reset() {
		multiDailyPopularities.clear();
		learner.reset();
	}

	protected void dayInitialize() {
		curDailyPopularities.reset();
	}

	@Override
	public boolean isExploring() {
		return getExploringFactor() >= getExploringThreshold();
	}

	@Override
	public double getExploringFactor() {
		return exploring;
	}

	public void calculateExploring() {

		exploring = 0;

		final Iterator<String> iterator = multiDailyPopularities.keySet()
				.iterator();
		while (iterator.hasNext()) {
			final String key = iterator.next();
			final FixedLengthQueue queue = multiDailyPopularities.get(key);

			MultiDayExploringMonitor.logger.info(key + ":");
			queue.log();

			if (queue.getN() >= windowSize) {
				double temp = queue.getN() * queue.getMean() * queue.getMean();
				if (temp != 0) {
					temp = queue.getVariance() / temp;
				}

				exploring += temp;

			} else {
				// assume trader exploring much in the beginning periods
				exploring += 1;
			}
		}

		exploring /= multiDailyPopularities.size();

		MultiDayExploringMonitor.logger.info("\nexploring factor: "
				+ Utils.formatter.format(exploring) + "\n");

		// TODO: to count the trader distribution on the current day
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(final int windowSize) {
		this.windowSize = windowSize;
	}

	public double getExploringThreshold() {
		return threshold;
	}

	public void setExploringThreshold(final double threshold) {
		this.threshold = threshold;
	}

	protected void updateTraderRegistration(
			final RegisteredTradersAnnouncedEvent event) {
		curDailyPopularities.newData(event.getNumOfTraders());

		FixedLengthQueue queue = multiDailyPopularities.get(event.getSpecialist()
				.getId());
		if (queue == null) {
			queue = new FixedLengthQueue(windowSize);
			multiDailyPopularities.put(event.getSpecialist().getId(), queue);
		}

		queue.newData(event.getNumOfTraders());
	}

	protected void updateExploringFactor() {
		learner.train(getCurDailyExploringFactor());
		calculateExploring();
	}

	protected double getCurDailyExploringFactor() {
		double temp = Math.pow(curDailyPopularities.getMean(), 2)
				* curDailyPopularities.getN();
		if (temp != 0) {
			temp = (temp - curDailyPopularities.getVariance()) / temp;
		}

		return temp;
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof DayOpeningEvent) {
			dayInitialize();
		} else if (event instanceof RegisteredTradersAnnouncedEvent) {
			updateTraderRegistration(((RegisteredTradersAnnouncedEvent) event));
		} else if (event instanceof DayClosedEvent) {
			updateExploringFactor();
		}
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();
		s += "\n" + Utils.indent("threshold:" + threshold);
		s += "\n" + Utils.indent(" windowsize:" + windowSize);

		return s;
	}
}