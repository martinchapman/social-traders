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
import java.util.Calendar;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.server.GameClock;
import edu.cuny.cat.server.GameController;
import edu.cuny.util.Resetable;

/**
 * A game view displaying properties of and activities at a specialist.
 * 
 * TODO: The legend may become very crowdy as shouts are placed in the market.
 * The legend needs periodic cleanup as traders no longer hold active shouts.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.24 $
 */

@SuppressWarnings("unchecked")
public final class SpecialistView extends GameView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(SpecialistView.class);

	ShoutDataset1 dataset;

	XYPlot xyplot;

	Specialist specialist;

	GameClock clock;

	public SpecialistView(final String specialistId) {
		super(specialistId);
		setSize(500, 350);
		specialist = GameController.getInstance().getRegistry().getSpecialist(
				specialistId);
		clock = GameController.getInstance().getClock();

		getContentPane().setLayout(new BorderLayout());

		setupDataset();
		setupShoutPlots();
	}

	private void setupDataset() {
		dataset = new ShoutDataset1();
	}

	private void setupShoutPlots() {
		final JFreeChart chart = ChartFactory.createTimeSeriesChart("", "Time",
				"Price", dataset, true, true, false);
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(getContentPane().getBackground());
		xyplot = (XYPlot) chart.getPlot();
		xyplot.setNoDataMessage("NO DATA");
		xyplot.setRenderer(new XYLineAndShapeRenderer());
		final NumberAxis numberaxis1 = (NumberAxis) xyplot.getRangeAxis();
		numberaxis1.setTickMarkInsideLength(2.0F);
		numberaxis1.setTickMarkOutsideLength(0.0F);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setDomainZoomable(true);
		chartPanel.setRangeZoomable(true);

		chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(20, 5, 5, 20), BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Shouts"), BorderFactory
						.createEmptyBorder(5, 5, 5, 5))));

		getContentPane().add(chartPanel, BorderLayout.CENTER);

		pack();
	}

	@Override
	protected void processShoutPlaced(final ShoutPlacedEvent event) {
		if (event.getShout().getSpecialist() == specialist) {
			dataset.newShout(event.getShout(), event.getTime());
		}
	}

	@Override
	protected void processTransactionExecuted(final TransactionExecutedEvent event) {
		if (event.getTransaction().getSpecialist() == specialist) {
			dataset.newTransaction(event.getTransaction(), event.getTime());
		}
	}

	@Override
	protected void processDayOpened(final DayOpenedEvent event) {
		dataset.reset();
	}

	class ShoutDataset1 extends TimeSeriesCollection implements Resetable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		protected SortedMap<Comparable, TimeSeries> seriesMap;

		protected TimeSeries transeries;

		public ShoutDataset1() {
			seriesMap = Collections
					.synchronizedSortedMap(new TreeMap<Comparable, TimeSeries>());
			transeries = new TimeSeries("transactions");
			seriesMap.put("transactions", transeries);
			addSeries(transeries);
		}

		/*
		 * routines adding data
		 */
		public synchronized void newShout(final Shout shout, final int[] time) {
			TimeSeries series = seriesMap.get(shout.getTrader().getId());
			if (series == null) {
				series = new TimeSeries(shout.getTrader().getId());
				seriesMap.put(series.getKey(), series);
				addSeries(series);
			}

			series.addOrUpdate(new TradingTick(time), shout.getPrice());
		}

		public synchronized void newTransaction(final Transaction transaction,
				final int[] time) {
			final Shout ask = transaction.getAsk();
			final Shout bid = transaction.getBid();

			transeries.addOrUpdate(new TradingTick(time), transaction.getPrice());
			TimeSeries series = getSeries(ask.getTrader().getId());
			if (series != null) {
				series.clear();
			}

			series = getSeries(bid.getTrader().getId());
			if (series != null) {
				series.clear();
			}
		}

		public synchronized TimeSeries getSeries(final String name) {
			return seriesMap.get(name);
		}

		public synchronized void reset() {
			for (int i = 0; i < getSeriesCount(); i++) {
				getSeries(i).clear();
			}
		}
	}

	class TradingDay extends TradingTimePeriod {

		private final int day;

		public TradingDay(final int day) {
			this.day = day;
		}

		@Override
		public int getStartTick() {
			return day
					* (clock.getDayLen() * clock.getRoundLen() + clock.getDayBreak());
		}

		@Override
		public int getEndTick() {
			return getStartTick() + clock.getDayLen() * clock.getRoundLen();
		}

		public int getDay() {
			return day;
		}

		public int compareTo(final Object arg0) {
			return getDay() - ((TradingDay) arg0).getDay();
		}

		@Override
		public long getFirstMillisecond(final Calendar arg0) {
			return getStartTick();
		}

		@Override
		public long getLastMillisecond(final Calendar arg0) {
			return getEndTick();
		}

		@Override
		public long getSerialIndex() {
			return getFirstMillisecond();
		}

		@Override
		public RegularTimePeriod next() {
			return new TradingDay(day + 1);
		}

		@Override
		public RegularTimePeriod previous() {
			return new TradingDay(day - 1);
		}
	}

	class TradingRound extends TradingTimePeriod {

		private final TradingDay day;

		private final int round;

		public TradingRound(final int day, final int round) {
			this.day = new TradingDay(day);
			this.round = round;
		}

		@Override
		public int getStartTick() {
			return day.getStartTick() + clock.getRoundLen() * round;
		}

		@Override
		public int getEndTick() {
			return getStartTick() + clock.getRoundLen();
		}

		public TradingDay getDay() {
			return day;
		}

		public int getRound() {
			return round;
		}

		public int compareTo(final Object arg0) {
			final int i = getDay().compareTo(((TradingRound) arg0).getDay());
			if (i == 0) {
				return getRound() - ((TradingRound) arg0).getRound();
			} else {
				return i;
			}
		}

		@Override
		public RegularTimePeriod next() {
			if (round == clock.getDayLen() - 1) {
				return new TradingRound(day.getDay() + 1, 0);
			} else {
				return new TradingRound(day.getDay(), round + 1);
			}
		}

		@Override
		public RegularTimePeriod previous() {
			if (round == 0) {
				return new TradingRound(day.getDay() - 1, clock.getDayLen() - 1);
			} else {
				return new TradingRound(day.getDay(), round - 1);
			}
		}
	}

	class TradingTick extends TradingTimePeriod {

		private final TradingRound round;

		private final int tick;

		public TradingTick(final int[] time) {
			this(time[0], time[1], time[2]);
		}

		public TradingTick(final int day, final int round, final int tick) {
			this.round = new TradingRound(day, round);
			this.tick = tick;
		}

		@Override
		public int getStartTick() {
			return round.getStartTick() + tick;
		}

		@Override
		public int getEndTick() {
			return getStartTick() + 1;
		}

		public TradingRound getRound() {
			return round;
		}

		public int getTick() {
			return tick;
		}

		public int compareTo(final Object arg0) {
			final int i = getRound().compareTo(((TradingTick) arg0).getRound());
			if (i == 0) {
				return getTick() - ((TradingTick) arg0).getTick();
			} else {
				return i;
			}
		}

		@Override
		public RegularTimePeriod next() {
			if (tick == clock.getRoundLen() - 1) {
				if (round.getRound() == clock.getDayLen() - 1) {
					return new TradingTick(round.getDay().getDay() + 1, 0, 0);
				} else {
					return new TradingTick(round.getDay().getDay(), round.getRound() + 1,
							0);
				}
			} else {
				return new TradingTick(round.getDay().getDay(), round.getRound(),
						tick + 1);
			}
		}

		@Override
		public RegularTimePeriod previous() {
			if (tick == 0) {
				if (round.getRound() == 0) {
					return new TradingTick(round.getDay().getDay() - 1,
							clock.getDayLen() - 1, clock.getRoundLen() - 1);
				} else {
					return new TradingTick(round.getDay().getDay(), round.getRound() - 1,
							clock.getRoundLen() - 1);
				}
			} else {
				return new TradingTick(round.getDay().getDay(), round.getRound(),
						tick - 1);
			}
		}
	}

	abstract class TradingTimePeriod extends RegularTimePeriod {

		public abstract int getStartTick();

		public abstract int getEndTick();

		@Override
		public long getFirstMillisecond(final Calendar arg0) {
			return getFirstMillisecond();
		}

		@Override
		public long getLastMillisecond(final Calendar arg0) {
			return getLastMillisecond();
		}

		@Override
		public long getFirstMillisecond() {
			return getStartTick();
		}

		@Override
		public long getLastMillisecond() {
			return getEndTick();
		}

		@Override
		public void peg(Calendar arg0) {
			// do nothing
		}

		@Override
		public long getSerialIndex() {
			return getFirstMillisecond();
		}
	}
}
