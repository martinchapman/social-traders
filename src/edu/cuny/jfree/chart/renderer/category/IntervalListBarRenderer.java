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
/*
 * JAF - Java Application Framework
 * Copyright (C) 1999-2006 Jinzhong Niu
 */

package edu.cuny.jfree.chart.renderer.category;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;

import edu.cuny.jfree.data.category.DefaultIntervalListCategoryDataset;
import edu.cuny.jfree.data.general.Interval;

/**
 * A renderer for
 * {@link edu.cuny.jfree.data.category.DefaultIntervalListCategoryDataset}.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

@SuppressWarnings("unchecked")
public class IntervalListBarRenderer extends BarRenderer implements
		CategoryItemRenderer, Cloneable, PublicCloneable, Serializable {

	/** For serialization. */
	private static final long serialVersionUID = -5068857361615528725L;

	static Logger logger = Logger.getLogger(IntervalListBarRenderer.class);

	/**
	 * Constructs a new renderer.
	 */
	public IntervalListBarRenderer() {
		super();
	}

	/**
	 * Draws the bar for a single (series, category) data item.
	 * 
	 * @param g2
	 *          the graphics device.
	 * @param state
	 *          the renderer state.
	 * @param dataArea
	 *          the data area.
	 * @param plot
	 *          the plot.
	 * @param domainAxis
	 *          the domain axis.
	 * @param rangeAxis
	 *          the range axis.
	 * @param dataset
	 *          the dataset.
	 * @param row
	 *          the row index (zero-based).
	 * @param column
	 *          the column index (zero-based).
	 * @param pass
	 *          the pass index.
	 */
	@Override
	public void drawItem(final Graphics2D g2,
			final CategoryItemRendererState state, final Rectangle2D dataArea,
			final CategoryPlot plot, final CategoryAxis domainAxis,
			final ValueAxis rangeAxis, final CategoryDataset dataset, final int row,
			final int column, final int pass) {

		if (dataset instanceof DefaultIntervalListCategoryDataset) {
			final DefaultIntervalListCategoryDataset d = (DefaultIntervalListCategoryDataset) dataset;
			drawInterval(g2, state, dataArea, plot, domainAxis, rangeAxis, d, row,
					column);
		} else {
			super.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset,
					row, column, pass);
		}
	}

	/**
	 * Draws a single interval.
	 * 
	 * @param g2
	 *          the graphics device.
	 * @param state
	 *          the renderer state.
	 * @param dataArea
	 *          the data plot area.
	 * @param plot
	 *          the plot.
	 * @param domainAxis
	 *          the domain axis.
	 * @param rangeAxis
	 *          the range axis.
	 * @param dataset
	 *          the data.
	 * @param row
	 *          the row index (zero-based).
	 * @param column
	 *          the column index (zero-based).
	 */
	protected void drawInterval(final Graphics2D g2,
			final CategoryItemRendererState state, final Rectangle2D dataArea,
			final CategoryPlot plot, final CategoryAxis domainAxis,
			final ValueAxis rangeAxis,
			final DefaultIntervalListCategoryDataset dataset, final int row,
			final int column) {

		final int seriesCount = getRowCount();
		final int categoryCount = getColumnCount();

		final PlotOrientation orientation = plot.getOrientation();

		double rectX = 0.0;
		double rectY = 0.0;

		final RectangleEdge domainAxisLocation = plot.getDomainAxisEdge();
		final RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();

		final List list = dataset.getList(row, column);

		if (list == null) {
			return;
		}

		Interval interval = null;
		for (int i = 0; i < list.size(); i++) {

			interval = (Interval) list.get(i);

			if (!interval.isMeaningful()) {
				continue;
			}

			// Y0
			double java2dValue0 = rangeAxis.valueToJava2D(interval.low, dataArea,
					rangeAxisLocation);

			// Y1
			double java2dValue1 = rangeAxis.valueToJava2D(interval.high, dataArea,
					rangeAxisLocation);

			if (java2dValue1 < java2dValue0) {
				final double temp = java2dValue1;
				java2dValue1 = java2dValue0;
				java2dValue0 = temp;
			}

			// BAR WIDTH
			double rectWidth = state.getBarWidth();

			// BAR HEIGHT
			double rectHeight = Math.abs(java2dValue1 - java2dValue0);

			if (orientation == PlotOrientation.HORIZONTAL) {
				// BAR Y
				rectY = domainAxis.getCategoryStart(column, getColumnCount(), dataArea,
						domainAxisLocation);
				if (seriesCount > 1) {
					final double seriesGap = dataArea.getHeight() * getItemMargin()
							/ (categoryCount * (seriesCount - 1));
					rectY = rectY + row * (state.getBarWidth() + seriesGap);
				} else {
					rectY = rectY + row * state.getBarWidth();
				}

				rectX = java2dValue0;

				rectHeight = state.getBarWidth();
				rectWidth = Math.abs(java2dValue1 - java2dValue0);

			} else if (orientation == PlotOrientation.VERTICAL) {
				// BAR X
				rectX = domainAxis.getCategoryStart(column, getColumnCount(), dataArea,
						domainAxisLocation);

				if (seriesCount > 1) {
					final double seriesGap = dataArea.getWidth() * getItemMargin()
							/ (categoryCount * (seriesCount - 1));
					rectX = rectX + row * (state.getBarWidth() + seriesGap);
				} else {
					rectX = rectX + row * state.getBarWidth();
				}

				rectY = java2dValue0;

			}
			final Rectangle2D bar = new Rectangle2D.Double(rectX, rectY, rectWidth,
					rectHeight);
			final Paint seriesPaint = getItemPaint(row, column);
			g2.setPaint(seriesPaint);
			g2.fill(bar);

			// draw the outline...
			if (state.getBarWidth() > BarRenderer.BAR_OUTLINE_WIDTH_THRESHOLD) {
				final Stroke stroke = getItemOutlineStroke(row, column);
				final Paint paint = getItemOutlinePaint(row, column);
				if ((stroke != null) && (paint != null)) {
					g2.setStroke(stroke);
					g2.setPaint(paint);
					g2.draw(bar);
				}
			}

			final CategoryItemLabelGenerator generator = getItemLabelGenerator(row,
					column);
			if ((generator != null) && isItemLabelVisible(row, column)) {
				drawItemLabel(g2, dataset, row, column, plot, generator, bar, false);
			}

			// collect entity and tool tip information...
			if (state.getInfo() != null) {
				final EntityCollection entities = state.getInfo().getOwner()
						.getEntityCollection();
				if (entities != null) {
					String tip = null;
					final CategoryToolTipGenerator tipster = getToolTipGenerator(row,
							column);
					if (tipster != null) {
						tip = tipster.generateToolTip(dataset, row, column);
					}
					String url = null;
					if (getItemURLGenerator(row, column) != null) {
						url = getItemURLGenerator(row, column).generateURL(dataset, row,
								column);
					}
					final CategoryItemEntity entity = new CategoryItemEntity(bar, tip,
							url, dataset, dataset.getRowKey(row), dataset
									.getColumnKey(column));
					entities.add(entity);
				}
			}
		}
	}

}
