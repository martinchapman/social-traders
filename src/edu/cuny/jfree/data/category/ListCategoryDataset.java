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

import org.jfree.data.category.CategoryDataset;

/**
 * A category dataset that manages a collection of lists of objects in a
 * 2-dimensional categorized space, instead of a collection of single values in
 * such a space in regular CategoryDataset.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

@SuppressWarnings("unchecked")
public interface ListCategoryDataset extends CategoryDataset {

	public abstract List getList(int i, int j);

	public abstract List getList(Comparable rowKey, Comparable columnKey);

	public abstract void remove(Comparable rowKey, Comparable columnKey);

	public abstract void removeAll();
}
