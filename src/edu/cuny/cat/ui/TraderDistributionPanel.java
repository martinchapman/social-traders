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

import java.awt.BorderLayout;
import java.awt.Color;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.RegistrationEvent;
import edu.cuny.cat.registry.Registry;
import edu.cuny.cat.server.GameClock;
import edu.cuny.cat.server.GameController;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * displays the number of agents registered daily with each specialist.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.legend</tt><br>
 * <font size=-1>boolean (<code>true</code> by default)</font></td>
 * <td valign=top>(whether or not the legend of plot showing trading agent
 * distribution is used)</td>
 * <tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.18 $
 */
public class TraderDistributionPanel extends ViewPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(TraderDistributionPanel.class);

	protected static final String P_LEGEND = "legend";

	protected static String UNDEDICATED = "UNDEDICATED";

	JFreeChart chart;

	DefaultCategoryDataset dataset;

	GameClock clock;

	Registry registry;

	public TraderDistributionPanel() {

		registry = GameController.getInstance().getRegistry();
		clock = GameController.getInstance().getClock();

		dataset = new DefaultCategoryDataset();

		setTitledBorder("Trader Distribution");

		chart = ChartFactory.createLineChart("", "", "", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		chart.setBackgroundPaint(getBackground());
		final CategoryPlot categoryplot = chart.getCategoryPlot();
		categoryplot.setOrientation(PlotOrientation.HORIZONTAL);
		categoryplot.setBackgroundPaint(Color.lightGray);
		categoryplot.setRangeGridlinePaint(Color.white);
		final LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) categoryplot
				.getRenderer();
		UIUtils.setDefaultLineAndShapeRendererStyle(lineandshaperenderer);
		lineandshaperenderer
				.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		final NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
		numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		numberaxis.setAutoRangeIncludesZero(false);
		numberaxis.setUpperMargin(0.12D);

		final ChartPanel chartPanel = new ChartPanel(chart);
		add(chartPanel, BorderLayout.CENTER);
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		boolean showLegend = true;
		showLegend = parameters.getBoolean(base
				.push(TraderDistributionPanel.P_LEGEND), null, showLegend);
		if (!showLegend) {
			chart.removeLegend();
		}
	}

	@Override
	protected synchronized void processGameStarted(final GameStartedEvent event) {
		final String traderIds[] = registry.getTraderIds();

		for (int i = 0; i < clock.getGameLen(); i++) {
			dataset.setValue(new Integer(traderIds.length), getDayText(i),
					TraderDistributionPanel.UNDEDICATED);
		}
	}

	@Override
	protected synchronized void processDayOpening(final DayOpeningEvent event) {
		final String specialistIds[] = registry.getSpecialistIds();
		for (final String specialistId : specialistIds) {
			dataset
					.setValue(new Integer(0), getDayText(event.getDay()), specialistId);
		}
	}

	@Override
	protected synchronized void processRegistration(final RegistrationEvent event) {
		dataset.incrementValue(1, getDayText(event.getDay()), event
				.getSpecialistId());
		dataset.incrementValue(-1, getDayText(event.getDay()),
				TraderDistributionPanel.UNDEDICATED);
		if (dataset.getValue(getDayText(event.getDay()),
				TraderDistributionPanel.UNDEDICATED).intValue() == 0) {
			// trader has been fully distributed
			dataset.setValue(null, getDayText(event.getDay()),
					TraderDistributionPanel.UNDEDICATED);
		}
	}

	protected String getDayText(final int day) {
		return String.valueOf(day);
	}
}
