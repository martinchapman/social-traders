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

package edu.cuny.cat.valuation;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import edu.cuny.cat.MyTestCase;

/**
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public class FixedValuerTest extends MyTestCase {

	static Logger logger = Logger.getLogger(FixedValuerTest.class);

	public FixedValuerTest(final String name) {
		super(name);
	}

	public void testValuations() {
		System.out.println("\n>>>>>>>>>\t " + "testValuations() \n");

		final double value = 50;
		final double delta = 0.000001;
		final FixedValuerGenerator generator = new FixedValuerGenerator(value);
		for (int i = 0; i < 5; i++) {
			final ValuationPolicy valuer = generator.createValuer();
			Assert
					.assertEquals(
							"The value configured for fixed valuation is not the valuation generated !",
							valuer.getValue(), value, delta);

		}
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(FixedValuerTest.suite());
	}

	public static Test suite() {
		return new TestSuite(FixedValuerTest.class);
	}
}