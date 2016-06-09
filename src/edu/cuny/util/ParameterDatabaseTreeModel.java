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
 Copyright 2006 by Sean Luke
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */

/*
 * Created on Apr 6, 2005 7:12:32 PM
 * 
 * By: spaus
 */
package edu.cuny.util;

import java.util.Comparator;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * <p>
 * This is a modified version of the class from the original ECJ package by Sean
 * Luke, et al..
 * </p>
 * 
 * @author spaus
 * @version $Revision: 1.8 $
 */
public class ParameterDatabaseTreeModel extends DefaultTreeModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean visibleLeaves;

	/**
	 * @param root
	 */
	public ParameterDatabaseTreeModel(final TreeNode root) {
		super(root);
		visibleLeaves = true;
	}

	/**
	 * @param root
	 * @param asksAllowsChildren
	 */
	public ParameterDatabaseTreeModel(final TreeNode root,
			final boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
		visibleLeaves = true;
	}

	/**
	 * @param visibleLeaves
	 */
	public void setVisibleLeaves(final boolean visibleLeaves) {
		this.visibleLeaves = visibleLeaves;
	}

	/**
	 * @return visibleLeaves
	 */
	public boolean getVisibleLeaves() {
		return visibleLeaves;
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(final Object parent, final int index) {
		if (!visibleLeaves) {
			if (parent instanceof ParameterDatabaseTreeNode) {
				return ((ParameterDatabaseTreeNode) parent).getChildAt(index,
						visibleLeaves);
			}
		}

		return ((TreeNode) parent).getChildAt(index);
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(final Object parent) {
		if (!visibleLeaves) {
			if (parent instanceof ParameterDatabaseTreeNode) {
				return ((ParameterDatabaseTreeNode) parent)
						.getChildCount(visibleLeaves);
			}
		}

		return ((TreeNode) parent).getChildCount();
	}

	/**
	 * @param parent
	 * @param comp
	 */
	public void sort(final Object parent,
			final Comparator<ParameterDatabaseTreeNode> comp) {
		((ParameterDatabaseTreeNode) parent).sort(comp);
	}
}
