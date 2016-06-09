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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cuny.jfree.data.general.Interval;

/**
 * extends {@link AbstractListCategoryDataset} to manipulate list of
 * {@link edu.cuny.jfree.data.general.Interval}s.
 * 
 * @see DefaultValueListCategoryDataset .
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

@SuppressWarnings("unchecked")
public class DefaultIntervalListCategoryDataset extends
		AbstractListCategoryDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger
			.getLogger(DefaultIntervalListCategoryDataset.class);

	public void setStartValue(final double value, final int index,
			final Comparable rowKey, final Comparable columnKey) {
		setValue(true, value, index, rowKey, columnKey);
	}

	public void setStartValue(final Number value, final int index,
			final Comparable rowKey, final Comparable columnKey) {
		setStartValue(value, index, rowKey, columnKey);
	}

	public void setEndValue(final double value, final int index,
			final Comparable rowKey, final Comparable columnKey) {
		setValue(false, value, index, rowKey, columnKey);
	}

	public void setEndValue(final Number value, final int index,
			final Comparable rowKey, final Comparable columnKey) {
		setEndValue(value.doubleValue(), index, rowKey, columnKey);
	}

	protected void setValue(final boolean isStart, final double value,
			final int index, final Comparable rowKey, final Comparable columnKey) {
		List list = getList(rowKey, columnKey);
		if (list == null) {
			list = new ArrayList();
			data.addObject(list, rowKey, columnKey);
		}

		Interval interval = null;
		if (index == list.size()) {
			interval = new Interval();
			list.add(interval);
		} else if ((index < list.size()) && (index >= 0)) {
			interval = (Interval) list.get(index);
		} else {
			DefaultIntervalListCategoryDataset.logger
					.error("Invalid index during accessing list !");
			return;
		}

		if (isStart) {
			interval.low = value;
		} else {
			interval.high = value;
		}

		final double d = value;
		if (!Double.isNaN(d)
				&& (Double.isNaN(maximumRangeValue) || (d > maximumRangeValue))) {
			maximumRangeValue = d;
		}

		if (!Double.isNaN(d)
				&& (Double.isNaN(minimumRangeValue) || (d < minimumRangeValue))) {
			minimumRangeValue = d;
		}

		if (automaticChangedEvent) {
			fireDatasetChanged();
		}
	}
}
