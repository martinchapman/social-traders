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
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;

import edu.cuny.jfree.data.category.ListCategoryDataset;

/**
 * A renderer for
 * {@link edu.cuny.jfree.data.category.DefaultValueListCategoryDataset}.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

@SuppressWarnings("unchecked")
public class ValueListShapeRenderer extends LineAndShapeRenderer implements
		Cloneable, PublicCloneable, Serializable {

	static Logger logger = Logger.getLogger(ValueListShapeRenderer.class);

	public ValueListShapeRenderer() {
		super(false, true);
	}

	@Override
	public void drawItem(final Graphics2D g2,
			final CategoryItemRendererState state, final Rectangle2D dataArea,
			final CategoryPlot plot, final CategoryAxis domainAxis,
			final ValueAxis rangeAxis, final CategoryDataset dataset, final int row,
			final int column, final int pass) {

		final ListCategoryDataset setData = (ListCategoryDataset) dataset;

		final List list = setData.getList(row, column);
		if (list == null) {
			return;
		}

		final PlotOrientation orientation = plot.getOrientation();
		final double x = domainAxis.getCategoryMiddle(column, getColumnCount(),
				dataArea, plot.getDomainAxisEdge());

		final Iterator iterator = list.iterator();
		while (iterator.hasNext()) {
			final Number value = (Number) iterator.next();
			final double y = rangeAxis.valueToJava2D(value.doubleValue(), dataArea,
					plot.getRangeAxisEdge());

			Shape shape = getItemShape(row, column);

			if (orientation == PlotOrientation.HORIZONTAL) {
				shape = ShapeUtilities.createTranslatedShape(shape, y, x);
			} else if (orientation == PlotOrientation.VERTICAL) {
				shape = ShapeUtilities.createTranslatedShape(shape, x, y);
			}
			if (getItemShapeVisible(row, column)) {
				if (getItemShapeFilled(row, column)) {
					g2.setPaint(getItemPaint(row, column));
					g2.fill(shape);
				} else {
					if (getUseOutlinePaint()) {
						g2.setPaint(getItemOutlinePaint(row, column));
					} else {
						g2.setPaint(getItemPaint(row, column));
					}
					g2.setStroke(getItemOutlineStroke(row, column));
					g2.draw(shape);
				}
			}
			g2.setPaint(getItemPaint(row, column));

			if (isItemLabelVisible(row, column)) {
				if (orientation == PlotOrientation.HORIZONTAL) {
					drawItemLabel(g2, orientation, dataset, row, column, y, x, value
							.doubleValue() < 0.0D);
				} else if (orientation == PlotOrientation.VERTICAL) {
					drawItemLabel(g2, orientation, dataset, row, column, x, y, value
							.doubleValue() < 0.0D);
				}
			}

			if (state.getInfo() != null) {
				final EntityCollection entities = state.getEntityCollection();
				if ((entities != null) && (shape != null)) {
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
					final CategoryItemEntity entity = new CategoryItemEntity(shape, tip,
							url, dataset, dataset.getRowKey(row), dataset
									.getColumnKey(column));
					entities.add(entity);
				}
			}
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ValueListShapeRenderer)) {
			return false;
		}
		return super.equals(obj);
	}

	private void writeObject(final ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
	}

	private void readObject(final ObjectInputStream stream) throws IOException,
			ClassNotFoundException {
		stream.defaultReadObject();
	}

	private static final long serialVersionUID = 0xcea128faa3647055L;
}
