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
import java.util.ConcurrentModificationException;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;

import edu.cuny.cat.core.AccountHolder;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AvailableMarketsAnnouncedEvent;
import edu.cuny.cat.event.AvailableTradersAnnouncedEvent;
import edu.cuny.cat.event.ClientStateUpdatedEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.FeesAnnouncedEvent;
import edu.cuny.cat.event.GameOverEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.ProfitAnnouncedEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.event.RoundClosingEvent;
import edu.cuny.cat.event.RoundOpenedEvent;
import edu.cuny.cat.registry.Registry;
import edu.cuny.cat.server.ClientState;
import edu.cuny.cat.server.GameClock;
import edu.cuny.cat.server.GameController;
import edu.cuny.jfree.chart.renderer.category.IntervalListBarRenderer;
import edu.cuny.jfree.chart.renderer.category.ValueListShapeRenderer;
import edu.cuny.jfree.data.category.DefaultIntervalListCategoryDataset;
import edu.cuny.jfree.data.category.DefaultValueListCategoryDataset;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * displays the status of clients, which may be specialist clients only, trader
 * clients only, or clients of both types.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.21 $
 */
public class ClientStatePanel extends ViewPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(ClientStatePanel.class);

	private static final String P_DAYS = "days";

	Registry registry;

	GameClock clock;

	DefaultValueListCategoryDataset eventDataset;

	DefaultIntervalListCategoryDataset progressDataset;

	CategoryPlot categoryPlot;

	NumberAxis yAxis;

	int days = 25;

	int step = 0;

	public ClientStatePanel() {

		registry = GameController.getInstance().getRegistry();
		clock = GameController.getInstance().getClock();

		setTitledBorder("Client Status");

		final CategoryAxis xAxis = new CategoryAxis();
		yAxis = new NumberAxis();
		yAxis.setTickUnit(new NumberTickUnit(1));

		// TODO: to change the colors for different kinds of events
		final ValueListShapeRenderer eventRenderer = new ValueListShapeRenderer();
		eventRenderer.setBaseOutlinePaint(Color.black);
		eventRenderer.setUseOutlinePaint(true);
		eventRenderer.setDrawOutlines(true);
		final IntervalListBarRenderer progressRenderer = new IntervalListBarRenderer();

		categoryPlot = new CategoryPlot();
		categoryPlot.setOrientation(PlotOrientation.HORIZONTAL);
		categoryPlot.setRenderer(0, eventRenderer);
		categoryPlot.setRenderer(1, progressRenderer);

		categoryPlot.setDomainAxis(xAxis);
		categoryPlot.setRangeAxis(yAxis);

		final JFreeChart chart = new JFreeChart(categoryPlot);
		chart.setAntiAlias(false);
		chart.setBackgroundPaint(getBackground());

		categoryPlot.setForegroundAlpha(0.5F);
		categoryPlot.getDomainAxis().setMaximumCategoryLabelWidthRatio(10.0f);
		categoryPlot.setBackgroundPaint(Color.lightGray);
		categoryPlot.setRangeGridlinePaint(Color.white);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setRangeZoomable(false);
		add(chartPanel, BorderLayout.CENTER);
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		if (days > clock.getGameLen()) {
			days = clock.getGameLen() + 1;
		}

		days = parameters.getIntWithDefault(base.push(ClientStatePanel.P_DAYS),
				null, days);
		if (days < 0) {
			days = clock.getGameLen() + 1;
		} else if (days > 50) {
			ClientStatePanel.logger.warn("The number of days shown in "
					+ getClass().getSimpleName() + " is too large !");
		}

		step = days * 2 / 3;
	}

	protected void updateAxisRange(final int day) {
		if (day + 1 >= yAxis.getUpperBound()) {
			yAxis.setLowerBound(yAxis.getLowerBound() + step);
			yAxis.setUpperBound(yAxis.getUpperBound() + step);
		}
	}

	@Override
	protected synchronized void processGameStarting(final GameStartingEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				yAxis.setLowerBound(-1);
				yAxis.setUpperBound(days);

				eventDataset = new DefaultValueListCategoryDataset();
				eventDataset.setAutomaticChangedEvent(false);
				progressDataset = new DefaultIntervalListCategoryDataset();
				progressDataset.setAutomaticChangedEvent(false);

				categoryPlot.setDataset(0, eventDataset);
				categoryPlot.setDataset(1, progressDataset);

				// useful only to keep specialists in the same order in the plot
				final String specialistIds[] = registry.getSpecialistIds();
				for (final String specialistId : specialistIds) {
					eventDataset.add(Double.NaN, event.getClass().getSimpleName(),
							specialistId);
					progressDataset.setStartValue(Double.NaN, 0, ClientState
							.getCodeDesc(ClientState.OK), specialistId);
					progressDataset.setEndValue(Double.NaN, 0, ClientState
							.getCodeDesc(ClientState.OK), specialistId);
				}
			}
		});
	}

	@Override
	protected void processFeesAnnounced(final FeesAnnouncedEvent event) {
		final double time = event.getDay();
		eventDataset.add(time, event.getClass().getSimpleName(), event
				.getSpecialist().getId());
	}

	@Override
	protected void processDayOpening(final DayOpeningEvent event) {
		updateAxisRange(event.getDay());
	}

	@Override
	protected void processDayClosed(final DayClosedEvent event) {
		updateGUI();
	}

	@Override
	protected void processGameOver(final GameOverEvent event) {
		updateGUI();
	}

	@Override
	protected void processRoundClosed(final RoundClosedEvent event) {
		updateGUI();
	}

	protected void updateGUI() {
		eventDataset.setChanged();
		progressDataset.setChanged();
	}

	@Override
	protected void processClientStatusUpdated(final ClientStateUpdatedEvent event) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					if ((eventDataset == null) || (progressDataset == null)) {
						// before the game starts, overlook all events
						return;
					}

					AuctionEvent triggeringEvent = event.getTriggeringEvent();
					if (triggeringEvent == null) {
						triggeringEvent = event;
					}

					final AccountHolder client = event.getClient();
					final ClientState prevState = event.getPreviousState();
					final ClientState curState = event.getCurrentState();

					// consider only specialist
					if (client instanceof Trader) {
						return;
					}

					// (others) -> OK: setStartValue
					// OK -> OK: setEndValue
					// OK -> (others): setEndValue
					// (others): addValue

					double time = Double.NaN;
					double delta = 0;
					if (triggeringEvent instanceof GameStartingEvent) {
						time = 0;
						delta = -0.3;
					} else if (triggeringEvent instanceof GameStartedEvent) {
						time = 0;
						delta = -0.1;
					} else if (triggeringEvent instanceof GameOverEvent) {
						time = triggeringEvent.getDay();
						delta = 0.3;
					} else if ((triggeringEvent instanceof DayOpeningEvent)
							|| (triggeringEvent instanceof DayOpenedEvent)
							|| (triggeringEvent instanceof FeesAnnouncedEvent)) {
						time = triggeringEvent.getDay();
					} else if ((triggeringEvent instanceof DayClosedEvent)
							|| (triggeringEvent instanceof ProfitAnnouncedEvent)) {
						time = (double) triggeringEvent.getDay() + 1;
					} else if (triggeringEvent instanceof RoundOpenedEvent) {
						time = triggeringEvent.getDay()
								+ (triggeringEvent.getRound() / (double) clock.getDayLen());
					} else if ((triggeringEvent instanceof RoundClosingEvent)
							|| (triggeringEvent instanceof RoundClosedEvent)) {
						time = triggeringEvent.getDay()
								+ ((triggeringEvent.getRound() + 1) / (double) clock
										.getDayLen());
					} else if ((triggeringEvent instanceof AvailableTradersAnnouncedEvent)
							|| (triggeringEvent instanceof AvailableMarketsAnnouncedEvent)) {
						time = 0;
						delta = -0.2;
					} else {
						time = triggeringEvent.getDay()
								+ ((triggeringEvent.getRound() + triggeringEvent.getTick()
										/ (double) clock.getRoundLen()) / clock.getDayLen());
						// if (triggeringEvent instanceof ClientStateUpdatedEvent) {
						// logger.info(client.getId() + " : "
						// + CatpMessage.concatenate(triggeringEvent.getTime()));
						// }
					}

					if (!Double.isNaN(time)) {
						switch (curState.getCode()) {
						case ClientState.OK:

							if ((triggeringEvent instanceof GameStartingEvent)
									|| (triggeringEvent instanceof GameOverEvent)) {
								eventDataset.add(time + delta, triggeringEvent.getClass()
										.getSimpleName(), client.getId());
								updateGUI();
							}

							switch (prevState.getCode()) {
							case ClientState.OK:
								progressDataset.setEndValue(time, progressDataset.getList(
										ClientState.getCodeDesc(ClientState.OK), client.getId())
										.size() - 1, ClientState.getCodeDesc(ClientState.OK),
										client.getId());
								// logger.info(client.getId() + " OK till " + time + " upon "
								// + triggeringEvent.getClass().getSimpleName());
								break;

							case ClientState.ERROR:
							case ClientState.READY:
								final List<?> list = progressDataset.getList(ClientState
										.getCodeDesc(ClientState.OK), client.getId());
								int index = 0;
								if (list != null) {
									index = list.size();

								}
								progressDataset.setStartValue(time, index, ClientState
										.getCodeDesc(ClientState.OK), client.getId());
								updateGUI();
								break;

							default:
								ClientStatePanel.logger.error("Invalid client state "
										+ ClientState.getCodeDesc(prevState.getCode())
										+ " prior to becoming "
										+ ClientState.getCodeDesc(curState.getCode()));
								break;
							}

							break;

						case ClientState.ERROR:
						case ClientState.FATAL:
						case ClientState.CONN_CLOSED:

							if (prevState.getCode() == ClientState.OK) {
								// normal up to the exception
								progressDataset.setEndValue(time, progressDataset.getList(
										ClientState.getCodeDesc(ClientState.OK), client.getId())
										.size() - 1, ClientState.getCodeDesc(ClientState.OK),
										client.getId());
								// logger.info(client.getId() + " OK ended " + time);

							}

							if (triggeringEvent instanceof ClientStateUpdatedEvent) {
								eventDataset.add(time + delta, curState.getDescription(),
										client.getId());
							} else {
								eventDataset.add(time + delta, ClientState.getCodeDesc(curState
										.getCode())
										+ " at " + triggeringEvent.getClass().getSimpleName(),
										client.getId());
							}
							break;
						}
					}
				} catch (final ConcurrentModificationException e) {
					ClientStatePanel.logger.error(e);
				}
			}
		});
	}
}
