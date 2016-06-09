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

/**
 * extends {@link AbstractListCategoryDataset} to manipulate list of values.
 * 
 * @see DefaultIntervalListCategoryDataset
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

@SuppressWarnings("unchecked")
public class DefaultValueListCategoryDataset extends
		AbstractListCategoryDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger
			.getLogger(DefaultValueListCategoryDataset.class);

	public void add(final double value, final Comparable rowKey,
			final Comparable columnKey) {
		add(new Double(value), rowKey, columnKey);
	}

	public void add(final Number value, final Comparable rowKey,
			final Comparable columnKey) {
		List<Number> list = getList(rowKey, columnKey);
		if (list == null) {
			list = new ArrayList<Number>();
			data.addObject(list, rowKey, columnKey);
		}
		list.add(value);

		final double d = value.doubleValue();
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
