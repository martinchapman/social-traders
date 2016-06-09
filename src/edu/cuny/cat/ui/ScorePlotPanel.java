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
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import edu.cuny.cat.event.DayStatPassEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.registry.Registry;
import edu.cuny.cat.server.GameController;
import edu.cuny.cat.stat.GameReport;
import edu.cuny.cat.stat.Score;
import edu.cuny.cat.stat.ScoreReport;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * displays the scores of specialists. If the simulation is a multi-game one,
 * the profits accumulate across games and do not get reset.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.12 $
 */
public class ScorePlotPanel extends ViewPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(ScorePlotPanel.class);

	public static final String P_CUMULATIVE = "cumulative";

	public static final String P_SHOWTYPE = "showtype";

	static final String SCORE = "score";

	boolean isCumulative = true;

	boolean showType = false;

	DefaultCategoryDataset dataset;

	JFreeChart chart;

	Registry registry;

	ScoreReport scoreReport;

	public ScorePlotPanel() {

		registry = GameController.getInstance().getRegistry();

		setTitledBorder("Scores");

		dataset = new DefaultCategoryDataset();

		chart = ChartFactory.createStackedBarChart("", "", "", dataset,
				PlotOrientation.HORIZONTAL, true, true, false);
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

		isCumulative = parameters.getBoolean(
				base.push(ScorePlotPanel.P_CUMULATIVE), null, isCumulative);

		showType = parameters.getBoolean(base.push(ScorePlotPanel.P_SHOWTYPE),
				null, showType);

		if (!showType) {
			chart.removeLegend();
		}

		initScoreReport();
	}

	protected void initScoreReport() {
		scoreReport = GameController.getInstance().getReport(ScoreReport.class);
		if (scoreReport == null) {
			ScorePlotPanel.logger.error(ScoreReport.class.getSimpleName()
					+ " must be available to use " + getClass().getSimpleName() + " !");
		}
	}

	@Override
	protected synchronized void processGameStarted(final GameStartedEvent event) {
		if ((dataset.getRowCount() == 0) || !isCumulative) {
			final String specialistIds[] = registry.getSpecialistIds();
			for (final String specialistId : specialistIds) {
				if (showType) {
					dataset.setValue(0.0D, GameReport.MARKETSHARE, specialistId);
					dataset.setValue(0.0D, GameReport.PROFIT, specialistId);
					dataset.setValue(0.0D, GameReport.TRANSACTIONRATE, specialistId);
				} else {
					dataset.setValue(0.0D, GameReport.SCORE, specialistId);
				}
			}
		}
	}

	/**
	 * TODO: bug noticed that on the first day, scores are not shown in this
	 * panel.
	 */
	@Override
	protected void processDayStatPass(final DayStatPassEvent event) {
		if ((scoreReport != null)
				&& scoreReport.getScoreDaysCondition().count(event.getDay())
				&& (event.getPass() == DayStatPassEvent.END_PASS)) {
			String specialistId;
			Score dailyScore;
			for (int i = 0; i < dataset.getColumnCount(); i++) {
				specialistId = (String) dataset.getColumnKey(i);
				dailyScore = scoreReport.getDailyScore(specialistId);

				if (dailyScore == null) {
					continue;
				}

				if (showType) {
					dataset.incrementValue(dailyScore.marketShare,
							GameReport.MARKETSHARE, specialistId);
					dataset.incrementValue(dailyScore.profitShare, GameReport.PROFIT,
							specialistId);
					dataset.incrementValue(dailyScore.transactionRate,
							GameReport.TRANSACTIONRATE, specialistId);
				} else {
					dataset.incrementValue(dailyScore.total, GameReport.SCORE,
							specialistId);
				}
			}
		}
	}
}
