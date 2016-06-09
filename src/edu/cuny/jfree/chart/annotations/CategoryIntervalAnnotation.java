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

package edu.cuny.jfree.chart.annotations;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;

/**
 * An interval annotation that can be placed in <code>CategoryPlot</code>.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

@SuppressWarnings("unchecked")
public class CategoryIntervalAnnotation implements CategoryAnnotation,
		Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Comparable category;

	private double value1;

	private double value2;

	private transient Paint paint;

	private transient Stroke stroke;

	public CategoryIntervalAnnotation(final Comparable category,
			final double value1, final double value2, final Paint paint,
			final Stroke stroke) {
		this.paint = Color.black;
		this.stroke = new BasicStroke(1.0F);
		if (category == null) {
			throw new IllegalArgumentException("Null 'category' argument.");
		}
		if (paint == null) {
			throw new IllegalArgumentException("Null 'paint' argument.");
		}
		if (stroke == null) {
			throw new IllegalArgumentException("Null 'stroke' argument.");
		} else {
			this.category = category;
			this.value1 = value1;
			this.value2 = value2;
			this.paint = paint;
			this.stroke = stroke;
			return;
		}
	}

	public Comparable getCategory() {
		return category;
	}

	public void setCategory(final Comparable category) {
		if (category == null) {
			throw new IllegalArgumentException("Null 'category' argument.");
		} else {
			this.category = category;
			return;
		}
	}

	public double getValue1() {
		return value1;
	}

	public void setValue1(final double value) {
		value1 = value;
	}

	public double getValue2() {
		return value2;
	}

	public void setValue2(final double value) {
		value2 = value;
	}

	public Paint getPaint() {
		return paint;
	}

	public void setPaint(final Paint paint) {
		if (paint == null) {
			throw new IllegalArgumentException("Null 'paint' argument.");
		} else {
			this.paint = paint;
			return;
		}
	}

	public Stroke getStroke() {
		return stroke;
	}

	public void setStroke(final Stroke stroke) {
		if (stroke == null) {
			throw new IllegalArgumentException("Null 'stroke' argument.");
		} else {
			this.stroke = stroke;
			return;
		}
	}

	public void draw(final Graphics2D g2, final CategoryPlot plot,
			final Rectangle2D dataArea, final CategoryAxis domainAxis,
			final ValueAxis rangeAxis) {

		final AlphaComposite alphaComposite = AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, plot.getForegroundAlpha());
		final Composite oldComposite = g2.getComposite();
		g2.setComposite(alphaComposite);

		final CategoryDataset dataset = plot.getDataset();
		final int catIndex = dataset.getColumnIndex(category);
		final int catCount = dataset.getColumnCount();
		double lineX1 = 0.0D;
		double lineY = 0.0D;
		double lineX2 = 0.0D;
		final PlotOrientation orientation = plot.getOrientation();
		final RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot
				.getDomainAxisLocation(), orientation);
		final RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot
				.getRangeAxisLocation(), orientation);
		if (orientation == PlotOrientation.HORIZONTAL) {
			lineY = domainAxis.getCategoryJava2DCoordinate(CategoryAnchor.MIDDLE,
					catIndex, catCount, dataArea, domainEdge);
			lineX1 = rangeAxis.valueToJava2D(value1, dataArea, rangeEdge);
			lineX2 = rangeAxis.valueToJava2D(value2, dataArea, rangeEdge);
		} else if (orientation == PlotOrientation.VERTICAL) {
			lineY = rangeAxis.valueToJava2D(value1, dataArea, rangeEdge);
			lineX1 = domainAxis.getCategoryJava2DCoordinate(CategoryAnchor.MIDDLE,
					catIndex, catCount, dataArea, domainEdge);
			lineX2 = domainAxis.getCategoryJava2DCoordinate(CategoryAnchor.MIDDLE,
					catIndex, catCount, dataArea, domainEdge);
		}
		g2.setPaint(paint);
		g2.setStroke(stroke);
		g2.drawLine((int) lineX1, (int) lineY, (int) lineX2, (int) lineY);

		g2.setComposite(oldComposite);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof CategoryIntervalAnnotation)) {
			return false;
		}
		final CategoryIntervalAnnotation that = (CategoryIntervalAnnotation) obj;
		if (!category.equals(that.getCategory())) {
			return false;
		}
		if (value1 != that.getValue1()) {
			return false;
		}
		if (value2 != that.getValue2()) {
			return false;
		}
		if (!PaintUtilities.equal(paint, that.paint)) {
			return false;
		}
		return ObjectUtilities.equal(stroke, that.stroke);
	}

	@Override
	public int hashCode() {
		return category.hashCode();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	private void writeObject(final ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		SerialUtilities.writePaint(paint, stream);
		SerialUtilities.writeStroke(stroke, stream);
	}

	private void readObject(final ObjectInputStream stream) throws IOException,
			ClassNotFoundException {
		stream.defaultReadObject();
		paint = SerialUtilities.readPaint(stream);
		stroke = SerialUtilities.readStroke(stream);
	}
}
