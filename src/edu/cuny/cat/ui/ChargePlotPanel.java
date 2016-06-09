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

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.util.SortOrder;

import edu.cuny.cat.core.Account;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.registry.Registry;
import edu.cuny.cat.server.GameController;

/**
 * displays the various types of charges specialists impose on traders over
 * time.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.11 $
 */
public class ChargePlotPanel extends ViewPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(ChargePlotPanel.class);

	DefaultCategoryDataset dataset;

	CategoryPlot categoryplot;

	Registry registry;

	public ChargePlotPanel() {

		registry = GameController.getInstance().getRegistry();

		setTitledBorder("Charges");

		dataset = new DefaultCategoryDataset();

		final JFreeChart chart = ChartFactory.createBarChart("", "", "", dataset,
				PlotOrientation.HORIZONTAL, true, true, false);
		// chart.setAntiAlias(false);
		chart.setBackgroundPaint(getBackground());
		categoryplot = (CategoryPlot) chart.getPlot();
		categoryplot.setBackgroundPaint(Color.lightGray);
		categoryplot.setRangeGridlinePaint(Color.white);
		final BarRenderer barrenderer = (BarRenderer) categoryplot.getRenderer();
		UIUtils.setDefaultBarRendererStyle(barrenderer);
		barrenderer
				.setLegendItemToolTipGenerator(new StandardCategorySeriesLabelGenerator(
						"Tooltip: {0}"));

		categoryplot.setRowRenderingOrder(SortOrder.DESCENDING);
		final ChartPanel chartPanel = new ChartPanel(chart);
		add(chartPanel, BorderLayout.CENTER);
	}

	@Override
	protected synchronized void processDayOpened(final DayOpenedEvent event) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				dataset = new DefaultCategoryDataset();

				final Specialist activeSpecialists[] = registry.getActiveSpecialists();
				for (final Specialist activeSpecialist : activeSpecialists) {
					dataset.setValue(activeSpecialist.getRegistrationFee(),
							Account.REGISTRATION_FEE, activeSpecialist.getId());
					dataset.setValue(activeSpecialist.getInformationFee(),
							Account.INFORMATION_FEE, activeSpecialist.getId());
					dataset.setValue(activeSpecialist.getShoutFee(), Account.SHOUT_FEE,
							activeSpecialist.getId());
					dataset.setValue(activeSpecialist.getTransactionFee(),
							Account.TRANSACTION_FEE, activeSpecialist.getId());
					dataset.setValue(activeSpecialist.getProfitFee(), Account.PROFIT_FEE,
							activeSpecialist.getId());
				}

				categoryplot.setDataset(dataset);
			}
		});
	}
}
