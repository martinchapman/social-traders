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

package edu.cuny.jfree.data.category;

import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.data.KeyedObjects2D;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.AbstractDataset;

/**
 * An abstract implementation of {@link ListCategoryDataset} using
 * <code>org.jfree.data.KeyedObjects2D</code>.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

@SuppressWarnings("unchecked")
public abstract class AbstractListCategoryDataset extends AbstractDataset
		implements ListCategoryDataset, RangeInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(AbstractListCategoryDataset.class);

	protected KeyedObjects2D data;

	protected double minimumRangeValue;

	protected double maximumRangeValue;

	protected boolean automaticChangedEvent = true;

	public AbstractListCategoryDataset() {
		data = new KeyedObjects2D();
	}

	public boolean getAutomaticChangedEvent() {
		return automaticChangedEvent;
	}

	public void setAutomaticChangedEvent(final boolean automaticChangedEvent) {
		this.automaticChangedEvent = automaticChangedEvent;
	}

	/**
	 * added to catch <code>UnknownKeyException</code>s.
	 * 
	 * @param rowKey
	 * @param columnKey
	 * @return the object in the dataset identified by the row key and the column
	 *         key.
	 */
	protected Object getObject(final Comparable rowKey, final Comparable columnKey) {
		try {
			return data.getObject(rowKey, columnKey);
		} catch (final UnknownKeyException e) {
			return null;
		}
	}

	public Number getValue(final int row, final int column) {
		if (getList(row, column) != null) {
			return new Double(Double.NaN);
		} else {
			return null;
		}
	}

	public Number getValue(final Comparable rowKey, final Comparable columnKey) {
		if (getList(rowKey, columnKey) != null) {
			return new Double(Double.NaN);
		} else {
			return null;
		}
	}

	public List getList(final int row, final int column) {
		final List list = (List) data.getObject(row, column);
		return list;
	}

	public List getList(final Comparable rowKey, final Comparable columnKey) {
		final List list = (List) getObject(rowKey, columnKey);
		return list;
	}

	public void remove(final Comparable rowKey, final Comparable columnKey) {
		data.removeObject(rowKey, columnKey);
	}

	public void removeAll() {
		final int count = data.getRowCount();
		for (int i = 0; i < count; i++) {
			data.removeRow(i);
		}

		if (automaticChangedEvent) {
			fireDatasetChanged();
		}
	}

	public void setChanged() {
		fireDatasetChanged();
	}

	public int getColumnIndex(final Comparable key) {
		return data.getColumnIndex(key);
	}

	public Comparable getColumnKey(final int column) {
		return data.getColumnKey(column);
	}

	public List getColumnKeys() {
		return data.getColumnKeys();
	}

	public int getRowIndex(final Comparable key) {
		return data.getRowIndex(key);
	}

	public Comparable getRowKey(final int row) {
		return data.getRowKey(row);
	}

	public List getRowKeys() {
		return data.getRowKeys();
	}

	public int getRowCount() {
		return data.getRowCount();
	}

	public int getColumnCount() {
		return data.getColumnCount();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof AbstractListCategoryDataset)) {
			return false;
		}
		final AbstractListCategoryDataset that = (AbstractListCategoryDataset) obj;
		return data.equals(that.data);
	}

	public double getRangeLowerBound(final boolean includeInterval) {
		return minimumRangeValue;
	}

	public double getRangeUpperBound(final boolean includeInterval) {
		return maximumRangeValue;
	}

	/**
	 * 
	 */
	public Range getRangeBounds(final boolean includeInterval) {
		Range range = null;

		if (!Double.isNaN(minimumRangeValue) && !Double.isNaN(maximumRangeValue)) {
			range = new Range(minimumRangeValue, maximumRangeValue);
		}

		return range;
	}
}
