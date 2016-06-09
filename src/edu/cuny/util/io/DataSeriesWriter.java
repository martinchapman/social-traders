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
 * JASA Java Auction Simulator API
 * Copyright (C) 2001-2005 Steve Phelps
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

package edu.cuny.util.io;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

/**
 * <p>
 * A data writer that stores data in a memory-resident data structure that can
 * also be used as a data series model for a JSci graph, or a table model for a
 * swing JTable component.
 * </p>
 * 
 * <p>
 * Each datum written to the {@link DataWriter} is one half a 2-dimensional
 * coordinate. The first datum is typically a time value.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * DataSeriesWriter timeSeries = new DataSeriesWriter();
 * for (int t = 0; t &lt; 1000; t++) {
 * 	timeSeries.newData(t);
 * 	timeSeries.newData(getValue(t));
 * }
 * </pre>
 * 
 * 
 * @author Steve Phelps
 * @version $Revision: 1.10 $
 */

public class DataSeriesWriter extends AbstractTableModel implements DataWriter,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected boolean isVisible = true;

	protected boolean isXCoordinate = true;

	protected double xCoord;

	protected Vector<SeriesDatum> data = new Vector<SeriesDatum>();

	static Logger logger = Logger.getLogger(DataSeriesWriter.class);

	public DataSeriesWriter() {
		super();
	}

	public void newData(final int datum) {
		newData((double) datum);
	}

	public void newData(final long datum) {
		newData((double) datum);
	}

	public void newData(final double datum) {
		if (isXCoordinate) {
			xCoord = datum;
		} else {
			final SeriesDatum d = new SeriesDatum(xCoord, datum);
			data.add(d);
		}
		isXCoordinate = !isXCoordinate;
	}

	public void newData(final float datum) {
		newData((double) datum);
	}

	public void clear() {
		data.clear();
	}

	public float getValue(final int datum) {
		return (float) getDatum(datum);
	}

	public float getCoord(final int datum, final int dimension) {
		switch (dimension) {
		case 0:
			return getXCoord(datum);
		case 1:
			return getYCoord(datum);
		default:
			throw new Error("Invalid dimension- " + dimension);
		}
	}

	public float getXCoord(final int datum) {
		if (datum > data.size() - 1) {
			return 0f;
		} else {
			return (float) data.get(datum).getX();
		}
	}

	public float getYCoord(final int datum) {
		return (float) getDatum(datum);
	}

	public double getDatum(final int i) {
		final SeriesDatum datum = data.get(i);
		double value = 0;
		if (datum != null) {
			value = data.get(i).getY();
		}
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			return 0;
		} else {
			return value;
		}
	}

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return data.size();
	}

	public Object getValueAt(final int rowIndex, final int columnIndex) {
		if (columnIndex == 0) {
			return new Integer(rowIndex);
		} else {
			return new Double(getDatum(rowIndex));
		}
	}

	public void flush() {
	}

	public void close() {
	}

	public int length() {
		return data.size();
	}

	public void newData(final Iterator<?> i) {
		/** @TODO: Implement this edu.cuny.util.io.DataWriter method */
		throw new java.lang.UnsupportedOperationException(
				"Method newData() not yet implemented.");
	}

	public void newData(final Object[] data) {
		/** @TODO: Implement this edu.cuny.util.io.DataWriter method */
		throw new java.lang.UnsupportedOperationException(
				"Method newData() not yet implemented.");
	}

	public void newData(final Object data) {
		/** @TODO: Implement this edu.cuny.util.io.DataWriter method */
		throw new java.lang.UnsupportedOperationException(
				"Method newData() not yet implemented.");
	}

	public void newData(final boolean data) {
		/** @TODO: Implement this edu.cuny.util.io.DataWriter method */
		throw new java.lang.UnsupportedOperationException(
				"Method newData() not yet implemented.");
	}

	/**
	 * @see edu.cuny.util.io.DataWriter#newData(java.lang.Double)
	 */
	public void newData(final Double data) {
		// empty body
	}

	/**
	 * @see edu.cuny.util.io.DataWriter#newData(java.lang.Integer)
	 */
	public void newData(final Integer data) {
		// empty body
	}

	/**
	 * @see edu.cuny.util.io.DataWriter#newData(java.lang.Long)
	 */
	public void newData(final Long data) {
		// empty body
	}

	/**
	 * @see edu.cuny.util.io.DataWriter#newData(java.lang.String)
	 */
	public void newData(final String data) {
		// empty body
	}

	@Override
	public String toString() {
		final StringBuffer out = new StringBuffer("( " + getClass() + " ");
		for (final SeriesDatum datum : data) {
			out.append(datum.toString());
		}
		out.append(")");
		return out.toString();
	}

}

class SeriesDatum {

	double x;

	double y;

	public SeriesDatum(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	public void setX(final double x) {
		this.x = x;
	}

	public void setY(final double y) {
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public String toString() {
		return "(" + getClass() + " x:" + x + " y:" + y + ")";
	}

}