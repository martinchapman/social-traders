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

import java.util.Arrays;
import java.util.HashSet;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import edu.cuny.cat.MyTestCase;

/**
 * This test aims to test certain tricky operations in Java, which JCAT may use.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

public class UtilsTest extends MyTestCase {

	static Logger logger = Logger.getLogger(UtilsTest.class);

	public UtilsTest(final String name) {
		super(name);
	}

	public void testArrayConversion() {
		System.out.println("\n>>>>>>>>>\t " + "testArrayConversion() \n");

		final Object objects[] = new String[] { "Monday", "Tuesday" };

		final String strings[] = Utils.convert(objects, String.class);

		UtilsTest.logger.info("Array successfully converted: "
				+ Arrays.asList(strings));

		Assert.assertTrue(true);
	}

	public void testDoubles() {
		System.out.println("\n>>>>>>>>>\t " + "testDoubles() \n");

		final double ni = Double.NEGATIVE_INFINITY;
		final double pi = Double.POSITIVE_INFINITY;

		double value = ni + pi;
		UtilsTest.logger.info(Double.toString(ni) + " + " + Double.toString(pi)
				+ " = " + Double.toString(value));

		value = pi + pi;
		UtilsTest.logger.info(Double.toString(pi) + " + " + Double.toString(pi)
				+ " = " + Double.toString(value));

		value = ni + ni;
		UtilsTest.logger.info(Double.toString(ni) + " + " + Double.toString(ni)
				+ " = " + Double.toString(value));

		value = 2 * pi + pi;
		UtilsTest.logger.info(" 2 * " + Double.toString(pi) + " + "
				+ Double.toString(pi) + " = " + Double.toString(value));

		value = Math.max(pi, pi);
		UtilsTest.logger.info("Math.max(" + Double.toString(pi) + ", "
				+ Double.toString(pi) + ") = " + Double.toString(value));

		value = Math.min(pi, pi);
		UtilsTest.logger.info("Math.min(" + Double.toString(pi) + ", "
				+ Double.toString(pi) + ") = " + Double.toString(value));

		Assert.assertTrue(true);
	}

	public void testArrayType() {
		System.out.println("\n>>>>>>>>>\t " + "testArrayType() \n");

		final String[] stringArrays = new String[] { "hello" };
		Assert.assertTrue(stringArrays instanceof String[]);
	}

	public void testSetMembership() {
		System.out.println("\n>>>>>>>>>\t " + "testSetMembership() \n");

		final HashSet<String> set = new HashSet<String>();
		set.add("hello");
		set.add("world");
		Assert.assertTrue(set.contains("hello"));
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(UtilsTest.suite());
	}

	public static Test suite() {
		return new TestSuite(UtilsTest.class);
	}
}