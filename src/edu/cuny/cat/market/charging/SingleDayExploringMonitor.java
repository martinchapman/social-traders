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

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.RegisteredTradersAnnouncedEvent;
import edu.cuny.util.CumulativeDistribution;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * <p>
 * a simple trader exploration monitor that decides based solely on the
 * steepness of the trader distribution among markets on the latest day.
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
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * 
 * <table>
 * <tr>
 * <td valign=top><tt>single_day_exploring_monitor</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 * 
 */

public class SingleDayExploringMonitor extends TraderExploringMonitor {

	public static final String P_DEF_BASE = "single_day_exploring_monitor";

	public static final String P_THRESHOLD = "threshold";

	public static final double DEFAULT_THRESHOLD = 0.6;

	/**
	 * a threshold value to determine whether traders are exploring in general or
	 * not.
	 */
	protected double threshold;

	CumulativeDistribution popularities;

	static Logger logger = Logger.getLogger(SingleDayExploringMonitor.class);

	public SingleDayExploringMonitor() {
		popularities = new CumulativeDistribution();
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {

		final Parameter defBase = new Parameter(
				SingleDayExploringMonitor.P_DEF_BASE);

		threshold = parameters.getDoubleWithDefault(base
				.push(SingleDayExploringMonitor.P_THRESHOLD), defBase
				.push(SingleDayExploringMonitor.P_THRESHOLD),
				SingleDayExploringMonitor.DEFAULT_THRESHOLD);
	}

	protected void dayInitialize() {
		popularities.reset();
	}

	@Override
	public boolean isExploring() {
		return getExploringFactor() >= getExploringThreshold();
	}

	@Override
	public double getExploringFactor() {
		// return popularities.getMean() / popularities.getStdDev();

		// now normalize the value to [0, 1], with 0 being most converging and
		// 1 most exploring

		// the variance varies between 0 and n*mean^2
		double temp = Math.pow(popularities.getMean(), 2) * popularities.getN();
		if (temp != 0) {
			temp = (temp - popularities.getVariance()) / temp;
		}

		return temp;
	}

	public double getExploringThreshold() {
		return threshold;
	}

	public void setExploringThreshold(final double threshold) {
		this.threshold = threshold;
	}

	protected void updateTraderRegistration(final int numOfTraders) {
		popularities.newData(numOfTraders);
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof DayOpeningEvent) {
			dayInitialize();
		} else if (event instanceof RegisteredTradersAnnouncedEvent) {
			updateTraderRegistration(((RegisteredTradersAnnouncedEvent) event)
					.getNumOfTraders());
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " threshold:" + threshold;
	}

}