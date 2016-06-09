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

package edu.cuny.jfree.chart.plot;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.data.general.ValueDataset;

/**
 * A customized <code>org.jfree.chart.plot.MeterPlot</code>, which do not show
 * the bound labels of <code>org.jfree.chart.plot.MeterInterval</code>s.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class MyMeterPlot extends MeterPlot {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyMeterPlot() {
		super(null);
	}

	public MyMeterPlot(final ValueDataset dataset) {
		super(dataset);
	}

	/**
	 * Draws the arc to represent an interval.
	 * 
	 * @param g2
	 *          the graphics device.
	 * @param meterArea
	 *          the drawing area.
	 * @param interval
	 *          the interval.
	 */
	@Override
	protected void drawArcForInterval(final Graphics2D g2,
			final Rectangle2D meterArea, final MeterInterval interval) {

		final double minValue = interval.getRange().getLowerBound();
		final double maxValue = interval.getRange().getUpperBound();
		final Paint outlinePaint = interval.getOutlinePaint();
		final Stroke outlineStroke = interval.getOutlineStroke();
		final Paint backgroundPaint = interval.getBackgroundPaint();

		if (backgroundPaint != null) {
			fillArc(g2, meterArea, minValue, maxValue, backgroundPaint, false);
		}
		if (outlinePaint != null) {
			if (outlineStroke != null) {
				drawArc(g2, meterArea, minValue, maxValue, outlinePaint, outlineStroke);
			}

			final boolean isOutlineInterval = interval.getOutlinePaint() == getDialOutlinePaint();
			drawTick(g2, meterArea, minValue, isOutlineInterval);
			drawTick(g2, meterArea, maxValue, isOutlineInterval);
		}
	}

}
