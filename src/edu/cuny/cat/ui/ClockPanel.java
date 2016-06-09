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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.text.TextUtilities;
import org.jfree.ui.TextAnchor;

import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.server.GameClock;
import edu.cuny.cat.server.GameController;
import edu.cuny.cat.stat.ScoreReport;
import edu.cuny.jfree.chart.plot.MyMeterPlot;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * displays a graphical game clock.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.needle</tt><br>
 * <font size=-1>string (a name recognizable by
 * {@link edu.cuny.util.ColorNameTable} or in the format of <code>0x??????</code>)</font></td>
 * <td valign=top>(the color of clock needle)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.angle</tt><br>
 * <font size=-1>int > 0 (260 by default)</font></td>
 * <td valign=top>(the fraction of an entire circle to show the clock meter)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.width</tt><br>
 * <font size=-1>int > 0 (200 by default)</font></td>
 * <td valign=top>(the width of the clock meter plot)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.height</tt><br>
 * <font size=-1>int > 0 (200 by default)</font></td>
 * <td valign=top>(the height of the clock meter plot)</td>
 * <tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.13 $
 */
public class ClockPanel extends ViewPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(ClockPanel.class);

	public static String P_NEEDLE = "needle";

	public static String P_ANGLE = "angle";

	public static String P_WIDTH = "width";

	public static String P_HEIGHT = "height";

	protected DefaultValueDataset dataset;

	protected int iteration;

	protected MeterPlot meterplot;

	protected JLabel iterationLabel;

	protected GameClock clock;

	protected ScoreReport scoreReport;

	public ClockPanel() {
		setTitledBorder("Clock");
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		clock = GameController.getInstance().getClock();

		dataset = new DefaultValueDataset();

		meterplot = new MyMeterPlot(dataset) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void drawValueLabel(final Graphics2D g2, final Rectangle2D area) {
				g2.setFont(getValueFont());
				g2.setPaint(getValuePaint());
				String valueStr = "No value";
				if (dataset != null) {
					final Number n = dataset.getValue();
					if (n != null) {
						if (n.intValue() == 0) {
							valueStr = "to start";
						} else if (n.intValue() == clock.getGameLen() * clock.getDayLen()) {
							valueStr = "done";
						} else {
							valueStr = "day " + (n.intValue() / clock.getDayLen())
									+ ", round " + (n.intValue() % clock.getDayLen());
						}
					}
				}
				final float x = (float) area.getCenterX();
				final float y = (float) area.getCenterY()
						+ MeterPlot.DEFAULT_CIRCLE_SIZE;
				TextUtilities.drawAlignedString(valueStr, g2, x, y,
						TextAnchor.TOP_CENTER);
			}

		};
		meterplot.setRange(new Range(0, clock.getDayLen() * clock.getGameLen()));

		meterplot.setNeedlePaint(parameters.getColorWithDefault(base
				.push(ClockPanel.P_NEEDLE), null, Color.darkGray));

		meterplot.setDialBackgroundPaint(new Color(0, 255, 0, 64));
		meterplot.setDialOutlinePaint(Color.gray);
		meterplot.setDialShape(DialShape.CHORD);

		meterplot.setMeterAngle(parameters.getIntWithDefault(base
				.push(ClockPanel.P_ANGLE), null, 260));

		meterplot.setTickLabelsVisible(true);
		meterplot.setTickLabelFont(new Font("Dialog", 1, 10));
		meterplot.setTickLabelPaint(Color.darkGray);
		meterplot.setTickSize(clock.getDayLen());
		meterplot.setTickPaint(Color.lightGray);

		meterplot.setValuePaint(Color.black);
		meterplot.setValueFont(new Font("Dialog", 1, 14));
		meterplot.setUnits("");
		meterplot.setTickLabelFormat(new NumberFormat() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public StringBuffer format(final double number,
					final StringBuffer toAppendTo, final FieldPosition pos) {
				return format((long) number, toAppendTo, pos);
			}

			@Override
			public StringBuffer format(final long number,
					final StringBuffer toAppendTo, final FieldPosition pos) {

				if (number % clock.getDayLen() == 0) {
					toAppendTo.append(String.valueOf(number / clock.getDayLen()));
				}
				return toAppendTo;
			}

			@Override
			public Number parse(final String source, final ParsePosition parsePosition) {
				return null;
			}

		});

		final JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT,
				meterplot, false);

		final ChartPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new Dimension(parameters.getIntWithDefault(base
				.push(ClockPanel.P_WIDTH), null, 200), parameters.getIntWithDefault(
				base.push(ClockPanel.P_HEIGHT), null, 200)));

		add(panel, BorderLayout.CENTER);

		initIterationLabel();

		initScoreReport();
	}

	private void initIterationLabel() {
		iteration = -1;
		iterationLabel = new JLabel();
		iterationLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(iterationLabel, BorderLayout.SOUTH);
	}

	protected void initScoreReport() {
		scoreReport = GameController.getInstance().getReport(ScoreReport.class);
	}

	private void updateIterationLabel() {
		iterationLabel.setText("Iteration " + iteration);
	}

	@Override
	protected void processGameStarting(final GameStartingEvent event) {
		meterplot.clearIntervals();
	}

	@Override
	protected void processGameStarted(final GameStartedEvent event) {
		dataset.setValue(new Integer(0));
		iteration++;
		updateIterationLabel();
	}

	@Override
	protected void processRoundClosed(final RoundClosedEvent event) {
		dataset.setValue(new Integer(dataset.getValue().intValue() + 1));
	}

	@Override
	protected void processDayOpening(final DayOpeningEvent event) {
		final int day = event.getDay();
		final Runnable thread = new AddingIntervalThread(clock, scoreReport,
				meterplot, day);
		try {
			javax.swing.SwingUtilities.invokeAndWait(thread);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	static class AddingIntervalThread implements Runnable {

		protected MeterPlot meterplot;

		protected GameClock clock;

		protected ScoreReport scoreReport;

		protected int day;

		public AddingIntervalThread(final GameClock clock,
				final ScoreReport scoreReport, final MeterPlot meterplot, final int day) {
			this.clock = clock;
			this.scoreReport = scoreReport;
			this.meterplot = meterplot;
			this.day = day;
		}

		public void run() {
			if ((scoreReport != null)
					&& scoreReport.getScoreDaysCondition().count(day)) {
				final MeterInterval interval = new MeterInterval("", new Range(day
						* clock.getDayLen(), (day + 1) * clock.getDayLen()));
				meterplot.addInterval(interval);
			}
		}
	}

}
