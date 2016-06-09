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
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayStatPassEvent;
import edu.cuny.util.CumulativeDistribution;

/**
 * <p>
 * This class tracks the values of specified report variables, and calculates
 * and outputs the distribution of each value.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class StatisticalReport extends AbstractReportVariableReport {

	static Logger logger = Logger.getLogger(StatisticalReport.class);

	protected Map<String, CumulativeDistribution> distributions;

	public StatisticalReport() {
		distributions = Collections
				.synchronizedMap(new HashMap<String, CumulativeDistribution>());
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof DayStatPassEvent) {
			if (((DayStatPassEvent) event).getPass() == DayStatPassEvent.THIRD_PASS) {
				dailyUpdate();
			}
		}
	}

	protected void dailyUpdate() {
		final ReportVariableBoard board = ReportVariableBoard.getInstance();
		Object value;
		CumulativeDistribution dist = null;
		for (final String varName : varNames) {
			dist = distributions.get(varName);
			if (dist == null) {
				dist = new CumulativeDistribution(varName);
				distributions.put(varName, dist);
			}

			value = board.getValue(varName);

			if (value instanceof Double) {
				final Double doubleValue = (Double) value;

				// logger.info(varNames[i] + ": " + Utils.format(doubleValue) + "\n");

				if (doubleValue.isNaN() || doubleValue.isInfinite()) {
					// use NaN for all these cases

					// do not report these values
					// dist.newData(Double.NaN);
				} else {
					dist.newData(doubleValue.doubleValue());
				}
			} else {
				// do not report these values
				// dist.newData(Double.NaN);
				StatisticalReport.logger.warn("Report variable " + varName
						+ " doesn't bear double values ! " + value);
			}
		}

	}

	@Override
	public void produceUserOutput() {

		StatisticalReport.logger.info("\n" + getClass().getSimpleName() + "\n");

		CumulativeDistribution dist = null;
		for (final String varName : varNames) {
			dist = distributions.get(varName);
			if (dist == null) {
				StatisticalReport.logger.error("Statistical data unavailable for "
						+ varName + " !");
			} else {
				StatisticalReport.logger.info(varName + ".mean\t"
						+ GameReport.Formatter.format(dist.getMean()));
				StatisticalReport.logger.info(varName + ".stdev\t"
						+ GameReport.Formatter.format(dist.getStdDev()));
				StatisticalReport.logger.info(varName + ".max\t"
						+ GameReport.Formatter.format(dist.getMax()));
				StatisticalReport.logger.info(varName + ".min\t"
						+ GameReport.Formatter.format(dist.getMin()));
				StatisticalReport.logger.info(varName + ".n\t"
						+ GameReport.Formatter.format(dist.getN()));
				StatisticalReport.logger.info("");
			}
		}

		StatisticalReport.logger.info("\n");
	}

	public CumulativeDistribution getDistribution(final String varName) {
		return distributions.get(varName);
	}

	@Override
	public Map<ReportVariable, ?> getVariables() {
		return null;
	}
}