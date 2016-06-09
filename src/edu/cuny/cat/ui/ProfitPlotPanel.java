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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import edu.cuny.cat.core.Account;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.event.FundTransferEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.registry.Registry;
import edu.cuny.cat.server.GameController;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * displays the various types of profit specialists have made so far. If the
 * simulation is a multi-game one, the profits accumulate across games and do
 * not get reset.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.17 $
 */
public class ProfitPlotPanel extends ViewPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(ProfitPlotPanel.class);

	public static final String P_CUMULATIVE = "cumulative";

	public static final String P_SHOWTYPE = "showtype";

	Set<Shout> shoutSet;

	boolean showType = false;

	boolean isCumulative = true;

	DefaultCategoryDataset dataset;

	Registry registry;

	public ProfitPlotPanel() {

		shoutSet = Collections.synchronizedSet(new HashSet<Shout>());

		registry = GameController.getInstance().getRegistry();

		setTitledBorder("Income and Expenses");

		dataset = new DefaultCategoryDataset();

		final JFreeChart chart = ChartFactory.createStackedBarChart("", "", "",
				dataset, PlotOrientation.HORIZONTAL, true, true, false);
		// chart.setAntiAlias(false);
		chart.setBackgroundPaint(getBackground());
		final CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();
		categoryplot.setBackgroundPaint(Color.lightGray);
		categoryplot.setRangeGridlinePaint(Color.white);
		final StackedBarRenderer stackedbarrenderer = (StackedBarRenderer) categoryplot
				.getRenderer();
		UIUtils.setDefaultBarRendererStyle(stackedbarrenderer);
		stackedbarrenderer
				.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		stackedbarrenderer.setBaseItemLabelsVisible(true);
		final ChartPanel chartPanel = new ChartPanel(chart);
		add(chartPanel, BorderLayout.CENTER);
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		isCumulative = parameters.getBoolean(base
				.push(ProfitPlotPanel.P_CUMULATIVE), null, isCumulative);

		showType = parameters.getBoolean(base.push(ProfitPlotPanel.P_SHOWTYPE),
				null, showType);
	}

	@Override
	protected synchronized void processGameStarted(final GameStartedEvent event) {
		shoutSet.clear();

		if ((dataset.getRowCount() == 0) || !isCumulative) {
			final String specialistIds[] = registry.getSpecialistIds();
			for (final String specialistId : specialistIds) {
				if (showType) {
					dataset.setValue(0.0D, Account.REGISTRATION_FEE, specialistId);
					dataset.setValue(0.0D, Account.INFORMATION_FEE, specialistId);
					dataset.setValue(0.0D, Account.SHOUT_FEE, specialistId);
					dataset.setValue(0.0D, Account.TRANSACTION_FEE, specialistId);
					dataset.setValue(0.0D, Account.PROFIT_FEE, specialistId);
				} else {
					dataset.setValue(0.0D, Account.INCOME, specialistId);
				}

				dataset.setValue(0.0D, Account.EXPENSE, specialistId);
			}
		}
	}

	@Override
	protected void processFundTransfer(final FundTransferEvent event) {
		if (event.getPayer() instanceof Specialist) {
			expense(event);
		}

		if (event.getPayee() instanceof Specialist) {
			income(event);
		}
	}

	protected void expense(final FundTransferEvent event) {
		dataset.incrementValue(-event.getAmount(), Account.EXPENSE, event
				.getPayer().getId());
	}

	protected void income(final FundTransferEvent event) {
		if (showType) {
			dataset.incrementValue(event.getAmount(), event.getType(), event
					.getPayee().getId());
		} else {
			dataset.incrementValue(event.getAmount(), Account.INCOME, event
					.getPayee().getId());
		}
	}
}
