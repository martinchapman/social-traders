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

package edu.cuny.cat.ui;

import org.apache.log4j.Logger;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.RegistrationEvent;
import edu.cuny.cat.event.RoundOpenedEvent;

/**
 * Displays the number of traders registered respectively with specialists.
 * Different from {@link TraderDistributionPanel} that displays all daily plots,
 * this panel shows the average daily trader registration over time and that for
 * the current day.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.13 $
 */
public class CumulativeTraderDistributionPanel extends TraderDistributionPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger
			.getLogger(CumulativeTraderDistributionPanel.class);

	protected static String AVERAGE = "Average";

	CategoryPlot catPlot;

	int totalRegistered[];

	public CumulativeTraderDistributionPanel() {
		catPlot = chart.getCategoryPlot();
		catPlot.setDataset(null);
	}

	@Override
	protected synchronized void processGameStarted(final GameStartedEvent event) {
		super.processGameStarted(event);

		final String specialistIds[] = registry.getSpecialistIds();

		totalRegistered = new int[specialistIds.length];

		for (int i = 0; i < specialistIds.length; i++) {
			totalRegistered[i] = 0;
		}
	}

	@Override
	protected synchronized void processRoundOpened(final RoundOpenedEvent event) {
		final DefaultCategoryDataset subDataset = new DefaultCategoryDataset();
		final String specialistIds[] = registry.getSpecialistIds();
		for (int i = 0; i < specialistIds.length; i++) {
			int registeredToday = 0;
			try {
				registeredToday = (dataset.getValue(getDayText(event.getDay()),
						specialistIds[i])).intValue();
			} catch (final Exception e) {
				// do nothing
			}
			subDataset.setValue(registeredToday, getDayText(event.getDay()),
					specialistIds[i]);
			subDataset.setValue((double) (totalRegistered[i] + registeredToday)
					/ (event.getDay() + 1), CumulativeTraderDistributionPanel.AVERAGE,
					specialistIds[i]);
		}
		catPlot.setDataset(subDataset);

	}

	@Override
	protected synchronized void processDayClosed(final DayClosedEvent event) {
		final String specialistIds[] = registry.getSpecialistIds();
		for (int i = 0; i < specialistIds.length; i++) {
			int registeredToday = 0;
			try {
				registeredToday = (dataset.getValue(getDayText(event.getDay()),
						specialistIds[i])).intValue();
			} catch (final Exception e) {
				// do nothing
			}
			totalRegistered[i] += registeredToday;
		}
	}

	@Override
	protected synchronized void processRegistration(final RegistrationEvent event) {
		super.processRegistration(event);

		if (dataset.getValue(getDayText(event.getDay()),
				TraderDistributionPanel.UNDEDICATED) == null) {
			// traders are fully distributed, so regenerate dataset
		}
	}
}
