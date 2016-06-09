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

package edu.cuny.util;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import edu.cuny.cat.MyTestCase;

/**
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class SortedTreeListTest extends MyTestCase {

	static Logger logger = Logger.getLogger(SortedTreeListTest.class);

	SortedTreeList<Integer> stl;

	public SortedTreeListTest(final String name) {
		super(name);
	}

	@Override
	public void setUp() {
		super.setUp();
		stl = new SortedTreeList<Integer>("tree list");
	}

	@Override
	public void tearDown() {
		stl.clear();
		stl = null;
	}

	public void testIncremental() {
		System.out.println("\n>>>>>>>>>\t " + "testIncremental() \n");

		final int data[] = { 0, 1, 2, 3, 4, 5 };
		final int pos[] = data;
		for (int i = 0; i < data.length; i++) {
			final Integer num = new Integer(data[i]);
			Assert.assertEquals("Element " + data[i]
					+ " is added at the wrong place.", pos[i], stl.indexOfIfAdded(num));
			stl.add(num);
		}

		Assert.assertEquals("The number of elements in the list should be "
				+ data.length + ".", data.length, stl.size());
	}

	public void testDecremental() {
		System.out.println("\n>>>>>>>>>\t " + "testDecremental() \n");

		final int data[] = { 5, 4, 3, 2, 1, 0 };
		final int pos[] = { 0, 0, 0, 0, 0, 0 };
		for (int i = 0; i < data.length; i++) {
			final Integer num = new Integer(data[i]);
			Assert.assertEquals("Element " + data[i]
					+ " is added at the wrong place.", pos[i], stl.indexOfIfAdded(num));
			stl.add(num);
		}

		Assert.assertEquals("The number of elements in the list should be "
				+ data.length + ".", data.length, stl.size());
	}

	public void testUnordered() {
		System.out.println("\n>>>>>>>>>\t " + "testUnordered() \n");

		final int data[] = { 11, 8, 9, 33, -1 };
		final int pos[] = { 0, 0, 1, 3, 0 };
		for (int i = 0; i < data.length; i++) {
			final Integer num = new Integer(data[i]);
			Assert.assertEquals("Element " + data[i]
					+ " is added at the wrong place.", pos[i], stl.indexOfIfAdded(num));
			stl.add(num);
		}

		Assert.assertEquals("The number of elements in the list should be "
				+ data.length + ".", data.length, stl.size());
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(SortedTreeListTest.suite());
	}

	public static Test suite() {
		return new TestSuite(SortedTreeListTest.class);
	}
}