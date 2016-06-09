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
 * Created on Apr 5, 2005 8:24:19 PM
 * 
 * By: spaus
 */
package edu.cuny.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * <p>
 * This is a modified version of the class from the original ECJ package by Sean
 * Luke, et al..
 * </p>
 * 
 * @author spaus
 * @version $Revision: 1.7 $
 */
class ParameterDatabaseTreeNode extends DefaultMutableTreeNode implements
		Comparable<ParameterDatabaseTreeNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ParameterDatabaseTreeNode() {
		super();
	}

	/**
	 * @param userObject
	 */
	public ParameterDatabaseTreeNode(final Object userObject) {
		super(userObject);
	}

	/**
	 * @param userObject
	 * @param allowsChildren
	 */
	public ParameterDatabaseTreeNode(final Object userObject,
			final boolean allowsChildren) {
		super(userObject, allowsChildren);
	}

	/**
	 * @param index
	 * @param visibleLeaves
	 * @return
	 */
	public Object getChildAt(final int index, final boolean visibleLeaves) {
		if (children == null) {
			throw new ArrayIndexOutOfBoundsException("node has no children");
		}

		if (!visibleLeaves) {
			int nonLeafIndex = -1;
			final Enumeration<?> e = children.elements();
			while (e.hasMoreElements()) {
				final TreeNode n = (TreeNode) e.nextElement();
				if (!n.isLeaf()) {
					if (++nonLeafIndex == index) {
						return n;
					}
				}
			}

			throw new ArrayIndexOutOfBoundsException("index = " + index
					+ ", children = " + getChildCount(visibleLeaves));
		}

		return super.getChildAt(index);
	}

	/**
	 * @param visibleLeaves
	 * @return
	 */
	public int getChildCount(final boolean visibleLeaves) {
		if (!visibleLeaves) {
			int nonLeafCount = 0;
			final Enumeration<?> e = children.elements();
			while (e.hasMoreElements()) {
				final TreeNode n = (TreeNode) e.nextElement();
				if (!n.isLeaf()) {
					++nonLeafCount;
				}
			}

			return nonLeafCount;
		}

		return super.getChildCount();
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(final ParameterDatabaseTreeNode o) {
		return ((Comparable) userObject).compareTo(o.userObject);
	}

	/**
	 * @param comp
	 */
	public void sort(final Comparator<ParameterDatabaseTreeNode> comp) {
		if (children == null) {
			return;
		}

		final ParameterDatabaseTreeNode[] childArr = Utils.convert(children
				.toArray(), ParameterDatabaseTreeNode.class);
		Arrays.sort(childArr, comp);
		children = new Vector<MutableTreeNode>(Arrays.asList(childArr));

		for (final ParameterDatabaseTreeNode n : childArr) {
			n.sort(comp);
		}
	}
}
